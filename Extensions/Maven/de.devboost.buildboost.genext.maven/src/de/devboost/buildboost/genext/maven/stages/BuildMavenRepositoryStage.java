/*******************************************************************************
 * Copyright (c) 2006-2013
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
package de.devboost.buildboost.genext.maven.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.IConstants;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseFeatureFinder;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.genext.maven.discovery.MavenRepositorySpecFinder;
import de.devboost.buildboost.genext.maven.steps.BuildMavenRepositoryStepProvider;
import de.devboost.buildboost.genext.updatesite.discovery.EclipseUpdateSiteDeploymentSpecFinder;
import de.devboost.buildboost.genext.updatesite.discovery.EclipseUpdateSiteFinder;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class BuildMavenRepositoryStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	@Override
	public AntScript getScript() throws BuildException {
		File buildProjectsDir = new File(new File(artifactsFolder), IConstants.PROJECTS_FOLDER);
		File buildTargetPlatformDir = new File(new File(artifactsFolder), IConstants.TARGET_PLATFORM_FOLDER);

		BuildContext context = createContext(false);
		
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(buildTargetPlatformDir));

		context.addBuildParticipant(new PluginFinder(buildProjectsDir));
		context.addBuildParticipant(new EclipseFeatureFinder(buildProjectsDir));
		context.addBuildParticipant(new EclipseUpdateSiteFinder(buildProjectsDir));
		context.addBuildParticipant(new EclipseUpdateSiteDeploymentSpecFinder(buildProjectsDir));
		context.addBuildParticipant(new MavenRepositorySpecFinder(buildProjectsDir));
		
		context.addBuildParticipant(new BuildMavenRepositoryStepProvider(
				buildProjectsDir.getParentFile()));
		
		AutoBuilder builder = new AutoBuilder(context);
		
		AntScript script = new AntScript();
		script.setName("Build maven repository");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}
	
	@Override
	public int getPriority() {
		return 10100;
	}
}
