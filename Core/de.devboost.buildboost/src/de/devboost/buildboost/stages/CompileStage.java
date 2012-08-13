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

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.steps.compile.CompileProjectStepProvider;
import de.devboost.buildboost.steps.compile.ExtractPluginZipStepProvider;
import de.devboost.buildboost.steps.compile.JDKVersion;

public class CompileStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	private String sourceFileEncoding;
	private JDKVersion jdkVersion;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}
	
	public void setSourceFileEncoding(String sourceFileEncoding) {
		this.sourceFileEncoding = sourceFileEncoding;
	}

	public void setJdkVersion(JDKVersion jdkVersion) {
		this.jdkVersion = jdkVersion;
	}

	public AntScript getScript() throws BuildException {
		// TODO check configuration
		File buildDir = new File(artifactsFolder);

		BuildContext context = createContext(false);
		File targetPlatform = new File(artifactsFolder);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(targetPlatform));

		context.addBuildParticipant(new PluginFinder(buildDir));
		
		context.addBuildParticipant(new ExtractPluginZipStepProvider());
		context.addBuildParticipant(new CompileProjectStepProvider(jdkVersion, sourceFileEncoding));
		
		AutoBuilder builder = new AutoBuilder(context);
		
		AntScript script = new AntScript();
		script.setName("Compile all projects");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}
	
	@Override
	public int getPriority() {
		return 8000;
	}
}
