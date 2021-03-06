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
package de.devboost.buildboost.genext.updatesite.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.IConstants;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseFeatureFinder;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.genext.updatesite.discovery.EclipseUpdateSiteDeploymentSpecFinder;
import de.devboost.buildboost.genext.updatesite.discovery.EclipseUpdateSiteFinder;
import de.devboost.buildboost.genext.updatesite.steps.BuildUpdateSiteStepProvider;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class BuildUpdateSiteStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolderPath;

	public void setArtifactsFolder(String artifactsFolderPath) {
		this.artifactsFolderPath = artifactsFolderPath;
	}

	@Override
	public AntScript getScript() throws BuildException {
		File artifactsFolder = new File(artifactsFolderPath);
		File distFolder = new File(new File(artifactsFolderPath).getParentFile(), "dist");
		File projectsFolder = new File(artifactsFolder, IConstants.PROJECTS_FOLDER);
		File targetPlatformFolder = new File(artifactsFolder, IConstants.TARGET_PLATFORM_FOLDER);

		BuildContext context = createContext(false);
		
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(targetPlatformFolder));

		context.addBuildParticipant(new PluginFinder(projectsFolder));
		context.addBuildParticipant(new EclipseFeatureFinder(projectsFolder));
		context.addBuildParticipant(new EclipseUpdateSiteFinder(projectsFolder));
		context.addBuildParticipant(new EclipseUpdateSiteDeploymentSpecFinder(projectsFolder));
		
		context.addBuildParticipant(new BuildUpdateSiteStepProvider(distFolder));
		
		AutoBuilder builder = new AutoBuilder(context);
		
		AntScript script = new AntScript();
		script.setName("Build update site(s)");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}
	
	@Override
	public int getPriority() {
		return 10000;
	}
}
