/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.buildboost.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.devboost.buildboost.artifacts.Package;
import de.devboost.buildboost.model.IDependable;

/**
 * A class to sort artifacts topologically in order to determine a reasonable
 * order in which they must be built.
 */
public class Sorter {

	public List<IDependable> findCycle(IDependable start) {
		return findCycle(start, Collections.<IDependable>emptySet());
	}

	public List<IDependable> findCycle(IDependable start, Set<IDependable> ignore) {
		Set<IDependable> visitedPlugins = new LinkedHashSet<IDependable>();
		visitedPlugins.add(start);

		return findCycle(start, ignore, visitedPlugins);
	}
	
	private List<IDependable> findCycle(IDependable artifact, Set<IDependable> ignore, Set<IDependable> visitedPlugins) {
		Collection<IDependable> dependencies = new LinkedHashSet<IDependable>(artifact.getDependencies());
		dependencies.removeAll(ignore);

		for (IDependable dependency : dependencies) {
			if (visitedPlugins.contains(dependency)) {
				// found cycle
				List<IDependable> cycle = new ArrayList<IDependable>(1);
				cycle.add(dependency);
				return cycle;
			} else {
				Set<IDependable> subVisited = new LinkedHashSet<IDependable>();
				subVisited.addAll(visitedPlugins);
				subVisited.add(dependency);
				List<IDependable> cycle = findCycle(dependency, ignore, subVisited);
				if (cycle != null) {
					cycle.add(dependency);
					return cycle;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the transient hull of dependencies for the given collection of
	 * artifacts.
	 * 
	 * @param artifacts
	 * @return
	 */
	public Set<IDependable> getTransientHull(Collection<IDependable> artifacts) {
		Set<IDependable> toAnalyze = new LinkedHashSet<IDependable>();
		toAnalyze.addAll(artifacts);
		
		Set<IDependable> result = new LinkedHashSet<IDependable>();
		while (!toAnalyze.isEmpty()) {
			IDependable next = toAnalyze.iterator().next();
			result.add(next);
			toAnalyze.remove(next);
			Collection<IDependable> dependencies = next.getDependencies();
			for (IDependable dependency : dependencies) {
				if (!result.contains(dependency)) {
					toAnalyze.add(dependency);
				}
			}
		}
		return result;
	}
	
	public List<IDependable> topologicalSort(List<IDependable> list) {
		return topologicalSort(list, Collections.<IDependable>emptySet());
	}
	
	/**
	 * Sorts the given list of artifacts topologically according to their 
	 * declared dependencies.
	 */
	public List<IDependable> topologicalSort(List<IDependable> artifacts, Set<IDependable> artifactsToIgnore) {
		// check that set of artifacts is complete
		Set<IDependable> transientHull = getTransientHull(artifacts);
		transientHull.removeAll(artifactsToIgnore);
		transientHull.removeAll(artifacts);
		if (!transientHull.isEmpty()) {
			throw new RuntimeException("Can't sort artifacts topologically, some dependencies are missing (" + transientHull + ").");
		}

		int totalListSize = artifacts.size();
		Map<IDependable, Set<IDependable>> graph = new LinkedHashMap<IDependable, Set<IDependable>>();
		Queue<IDependable> queue = new LinkedList<IDependable>();
		IDependable[] sorted = new IDependable[totalListSize];
		// create dependency graph
		for (int i = 0; i < artifacts.size(); i++) {
			graph.put(artifacts.get(i), new LinkedHashSet<IDependable>());
	
			Collection<IDependable> requiredJobs = new LinkedHashSet<IDependable>(); 
			requiredJobs.addAll(artifacts.get(i).getDependencies());
			requiredJobs.removeAll(artifactsToIgnore);
			
			Set<IDependable> dependencySet = (Set<IDependable>) graph.get(artifacts.get(i));
			for (IDependable requiredJob : requiredJobs) {
				dependencySet.add(requiredJob);
			}
			// artifacts that do not depend on other artifacts are the starting
			// point for the creation of the topological order
			if (dependencySet.isEmpty()) {
				queue.add(artifacts.get(i));
			}
		}

		// Getting the nodes in sorted order
		int index = 0;
		while (!queue.isEmpty()) {
			IDependable next = queue.remove();
			sorted[index++] = next;
			Iterator<IDependable> iter = graph.keySet().iterator();
			while (iter.hasNext()) {
				// for each key in graph
				// check if node is not already removed
				IDependable key = iter.next();
				Set<IDependable> dependencySet = (Set<IDependable>) graph.get(key);
				if (!dependencySet.isEmpty()) {
					dependencySet.remove(next);
					if (next instanceof Package) {
						//TODO this is needed because the fairly complex package 
						//     import/export mechanism of OSGi which allows multiple
						//     (re-)exports of the same package
						removeAllSimilarPackages(dependencySet, (Package) next);
					}
					// if this node now has zero incoming edges
					if (dependencySet.isEmpty()) {
						queue.add(key);
					}	
				}
			}
		}

		if (index < totalListSize) {
			throw new RuntimeException("Cycle detected in plug-in dependencies or plug-in is missing from set of all plug-ins.");
		}

		List<IDependable> result = new ArrayList<IDependable>(sorted.length);
		for (IDependable next : sorted) {
			result.add(next);
		}
		return result;
	}

	private void removeAllSimilarPackages(Set<IDependable> dependencySet, Package p) {
		for (Iterator<IDependable> i = dependencySet.iterator(); i.hasNext();) {
			IDependable next = i.next();
			if (next instanceof Package && 
					((Package) next).getIdentifier().endsWith(p.getIdentifier())) {
				i.remove();
			}
		}	
	}

	/**
	 * Sorts the given list of artifacts topologically and creates a bucket for
	 * each set of artifacts where artifacts are on the same topological level.
	 * 
	 * @param artifacts
	 * @return
	 */
	public List<List<IDependable>> sortTopologicallyToBuckets(
			List<IDependable> artifacts) {
		List<IDependable> sorted = topologicalSort(artifacts);
		List<List<IDependable>> buckets = new ArrayList<List<IDependable>>();
		List<IDependable> bucket = new ArrayList<IDependable>();
		for (IDependable next : sorted) {
			// check whether the current bucket is a fresh one
			if (bucket.isEmpty()) {
				bucket.add(next);
				continue;
			}
			// check whether the current bucket contains an artifact the
			// current element depends on
			for (IDependable nextInBucket : bucket) {
				boolean hasDependencyToBucket = next.getDependencies().contains(nextInBucket);
				if (hasDependencyToBucket) {
					// create new bucket
					buckets.add(bucket);
					bucket = new ArrayList<IDependable>();
					break;
				}
			}
			bucket.add(next);
		}
		if (!bucket.isEmpty()) {
			buckets.add(bucket);
		}
		return buckets;
	}
}
