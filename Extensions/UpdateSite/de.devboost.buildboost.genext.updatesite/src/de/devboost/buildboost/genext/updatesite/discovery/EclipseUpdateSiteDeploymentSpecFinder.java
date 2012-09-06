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
package de.devboost.buildboost.genext.updatesite.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.discovery.AbstractFileFinder;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.ArtifactUtil;

public class EclipseUpdateSiteDeploymentSpecFinder extends AbstractFileFinder<EclipseUpdateSiteDeploymentSpec> {

	private IBuildListener buildListener;

	public EclipseUpdateSiteDeploymentSpecFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {
		buildListener = context.getBuildListener();
		Collection<EclipseUpdateSiteDeploymentSpec> updateSites = new ArrayList<EclipseUpdateSiteDeploymentSpec>();
		traverse(context, updateSites);
		return new ArtifactUtil().getSetOfArtifacts(updateSites);
	}

	protected EclipseUpdateSiteDeploymentSpec createArtifactFromFile(File file) {
		if (buildListener != null) {
			buildListener.handleBuildEvent(BuildEventType.INFO, "Discovered update site specification: " + file.getAbsolutePath());
		}
		return new EclipseUpdateSiteDeploymentSpec(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {
			
			public boolean accept(File file) {
				return file.getName().equals("site.deployment") && file.isFile();
			}
		};
	}
}
