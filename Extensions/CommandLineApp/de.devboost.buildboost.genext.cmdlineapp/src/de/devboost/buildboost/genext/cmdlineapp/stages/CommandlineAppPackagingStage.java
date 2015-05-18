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
package de.devboost.buildboost.genext.cmdlineapp.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.genext.cmdlineapp.steps.CommandlineAppPackagingStepProvider;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class CommandlineAppPackagingStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}
	
	@Override
	public AntScript getScript() throws BuildException {
		File artifactsDir = new File(artifactsFolder);

		BuildContext context = createContext(false);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(artifactsDir));
		context.addBuildParticipant(new PluginFinder(artifactsDir));
		context.addBuildParticipant(new CommandlineAppPackagingStepProvider());
		
		AutoBuilder builder = new AutoBuilder(context);
		AntScript script = new AntScript();
		script.setName("Create executable JARs for command line applications");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}

	@Override
	public int getPriority() {
		return 9998; // Used this because WebAppPackagingStage uses 11000 and
		// CustomZipFileStage uses 9999.
	}
}
