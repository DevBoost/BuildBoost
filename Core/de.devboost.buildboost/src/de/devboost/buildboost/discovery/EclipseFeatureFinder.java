/*******************************************************************************
 * Copyright (c) 2006-2015
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.util.ArtifactUtil;

/**
 * An {@link EclipseFeatureFinder} can be used to discover Eclipse features (i.e., files with name 'feature.xml'). In
 * contrast to the {@link EclipseTargetPlatformAnalyzer} which does also discover Eclipse features, this class is only
 * used to search for features in projects (i.e., it does not consider packaged features in JAR files).
 * 
 * TODO inherit from {@link AbstractFileFinder}?
 */
public class EclipseFeatureFinder extends AbstractArtifactDiscoverer {

	private final File directory;

	public EclipseFeatureFinder(File directory) {
		super();
		this.directory = directory;
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) {
		Collection<EclipseFeature> features = discoverArtifacts(context, directory);
		return new ArtifactUtil().getSetOfArtifacts(features);
	}

	private Collection<EclipseFeature> discoverArtifacts(IBuildContext context, File directory) {

		Collection<EclipseFeature> features = new ArrayList<EclipseFeature>();
		features.addAll(findFeatureFiles(directory));
		traverseSubDirectories(context, directory, features);
		return features;
	}

	private void traverseSubDirectories(IBuildContext context, File directory, Collection<EclipseFeature> features) {

		File[] subDirectories = directory.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		if (subDirectories == null) {
			return;
		}
		for (File subDirectory : subDirectories) {
			features.addAll(discoverArtifacts(context, subDirectory));
		}
	}

	private Collection<EclipseFeature> findFeatureFiles(File directory) {
		File[] featureXmlFiles = directory.listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().equals("feature.xml");
			}
		});
		if (featureXmlFiles == null) {
			return Collections.emptySet();
		}

		Collection<EclipseFeature> features = new ArrayList<EclipseFeature>(featureXmlFiles.length);
		for (File featureXmlFile : featureXmlFiles) {
			features.add(new EclipseFeature(featureXmlFile, false));
		}
		return features;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + directory + "]";
	}
}
