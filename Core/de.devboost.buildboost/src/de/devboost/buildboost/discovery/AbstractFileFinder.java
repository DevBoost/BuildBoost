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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildParticipant;

/**
 * An abstract base class for discoverers that search for files in the 
 * workspace that is subject to the current build.
 *
 * @param <ArtifactType> the type of artifact that is searched for
 */
public abstract class AbstractFileFinder<ArtifactType extends IArtifact> 
	extends AbstractArtifactDiscoverer {

	protected File directory;

	public AbstractFileFinder(File directory) {
		super();
		this.directory = directory;
	}

	protected void traverse(
			IBuildContext context, 
			Collection<ArtifactType> artifacts) throws BuildException {
		traverse(context, directory, artifacts);
	}

	/**
	 * Traverses the given directory and all its sub folders recursively to
	 * find artifacts. Using the two template methods {@link #getFileFilter()}
	 * and {@link #createArtifactFromFile(File)} sub classes can determine which
	 * files are considered and how these are converted into artifact objects.
	 * 
	 * @param context the context this build is performed in
	 * @param directory the root directory where to start the traversal
	 * @param artifacts a collection that is used to store the found artifacts
	 * @throws BuildException 
	 */
	protected void traverse(
			IBuildContext context, 
			File directory,
			Collection<ArtifactType> artifacts) throws BuildException {
		
		findFiles(directory, artifacts);
		
		File[] subDirectories = directory.listFiles(getDirectoryFilter());
		if (subDirectories == null) {
			return;
		}
		for (File subDirectory : subDirectories) {
			traverse(context, subDirectory, artifacts);
		}
	}

	private void findFiles(File directory, Collection<ArtifactType> artifacts) throws BuildException {
		File[] files = directory.listFiles(getFileFilter());
		if (files == null) {
			return;
		}
		for (File file : files) {
			artifacts.add(createArtifactFromFile(file));
		}
	}

	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		return false;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + directory.getAbsolutePath() + "]";
	}

	protected abstract ArtifactType createArtifactFromFile(File file) throws BuildException;

	protected FileFilter getDirectoryFilter() {
		return new FileFilter() {	
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
	}

	protected abstract FileFilter getFileFilter();
}
