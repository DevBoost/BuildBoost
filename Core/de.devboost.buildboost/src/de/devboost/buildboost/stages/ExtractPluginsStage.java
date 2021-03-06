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
package de.devboost.buildboost.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.steps.compile.ExtractPluginZipStepProvider;

public class ExtractPluginsStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	public AntScript getScript() throws BuildException {
		File buildDir = new File(artifactsFolder);
		BuildContext context = createContext(false);
		File targetPlatform = new File(artifactsFolder);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(targetPlatform));
		context.addBuildParticipant(new ExtractPluginZipStepProvider(new File(buildDir,
				EclipseTargetPlatformAnalyzer.ARTIFACT_CACHE_FILE_NAME)));

		context.setIgnoreUnresolvedDependencies(true);

		AutoBuilder builder = new AutoBuilder(context);

		AntScript script = new AntScript();
		script.setName("Extract plugins with libs");
		script.addTargets(builder.generateAntTargets());

		return script;
	}

	@Override
	public int getPriority() {
		return 20;
	}
}
