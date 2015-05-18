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

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.artifacts.RepositoriesFile;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.util.ArtifactUtil;

public class RepositoriesFileFinder extends AbstractFileFinder<RepositoriesFile> {

	public static final String REPOSITORIES_EXTENSION = ".repositories";

	public RepositoriesFileFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {
		Collection<RepositoriesFile> repositoriesFiles = new ArrayList<RepositoriesFile>();
		traverse(context, repositoriesFiles);
		return new ArtifactUtil().getSetOfArtifacts(repositoriesFiles);
	}

	protected RepositoriesFile createArtifactFromFile(File file) throws BuildException {
		return new RepositoriesFile(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(REPOSITORIES_EXTENSION) && file.isFile();
			}
		};
	}
}
