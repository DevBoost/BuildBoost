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
package de.devboost.buildboost.stages;

import java.io.File;
import java.util.Collection;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.discovery.TargetPlatformZipFinder;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.steps.copy.CopyPluginsAndFeaturesBuildStepProvider;
import de.devboost.buildboost.steps.copy.CopyProjectsBuildStepProvider;
import de.devboost.buildboost.steps.copy.ExtractZipFileBuildStepProvider;

public class CopyProjectsStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String reposFolder;
	private String artifactsFolder;

	public void setReposFolder(String reposFolder) {
		this.reposFolder = reposFolder;
	}

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}
	
	public AntScript getScript() throws BuildException {
		BuildContext context = createContext(true);
		context.addBuildParticipant(new PluginFinder(new File(reposFolder)));
		context.addBuildParticipant(new CopyProjectsBuildStepProvider(new File(artifactsFolder)));
		
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(new File(reposFolder)));	
		context.addBuildParticipant(new CopyPluginsAndFeaturesBuildStepProvider(new File(artifactsFolder)));
		context.addBuildParticipant(new TargetPlatformZipFinder(new File(reposFolder)));
		context.addBuildParticipant(new ExtractZipFileBuildStepProvider(new File(artifactsFolder)));
		
		context.setIgnoreUnresolvedDependencies(true);
		AutoBuilder builder = new AutoBuilder(context);
		Collection<AntTarget> targets = builder.generateAntTargets();

		AntScript copyScript = new AntScript();
		copyScript.setName("Copy plug-in stage");
		copyScript.addTargets(targets);
		
		return copyScript;
	}
	
	@Override
	public int getPriority() {
		return 0;
	}
}
