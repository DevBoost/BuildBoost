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
package de.devboost.buildboost.genext.maven.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.discovery.AbstractFileFinder;
import de.devboost.buildboost.genext.maven.artifacts.MavenRepositorySpec;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.ArtifactUtil;

public class MavenRepositorySpecFinder extends AbstractFileFinder<MavenRepositorySpec> {

	private static final String MAVEN_REPOSITORY_SPEC = "maven-repository.properties";
	
	private IBuildListener buildListener;

	public MavenRepositorySpecFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {
		buildListener = context.getBuildListener();
		Collection<MavenRepositorySpec> updateSites = new ArrayList<MavenRepositorySpec>();
		traverse(context, updateSites);
		return new ArtifactUtil().getSetOfArtifacts(updateSites);
	}

	protected MavenRepositorySpec createArtifactFromFile(File file) {
		if (buildListener != null) {
			buildListener.handleBuildEvent(BuildEventType.INFO, "Discovered maven repository specification: " + file.getAbsolutePath());
		}
		return new MavenRepositorySpec(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {
			
			public boolean accept(File file) {
				return file.getName().equals(MAVEN_REPOSITORY_SPEC) && file.isFile();
			}
		};
	}
}
