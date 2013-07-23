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
package de.devboost.buildboost.genext.toolproduct.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseFeatureFinder;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.genext.toolproduct.discovery.ToolProductSpecificationFinder;
import de.devboost.buildboost.genext.toolproduct.steps.BuildToolProductStepProvider;
import de.devboost.buildboost.genext.updatesite.discovery.EclipseUpdateSiteFinder;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

/**
 * The {@link BuildToolProductStage} can be used to build distributions for
 * tools which are based on Eclipse. The stage searches for tool product
 * specifications (toolproduct.spec files) and installs the specified Eclipse
 * feature into plain Eclipse distributions. In addition, custom splash screens
 * and icons can be provided as well as an example workspace that is packaged
 * with the distribution and used when the custom distribution is started.
 */
public class BuildToolProductStage extends AbstractBuildStage implements
		IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	@Override
	public AntScript getScript() throws BuildException {
		File artifactsDir = new File(artifactsFolder);
		File distDir = new File(artifactsDir, "dist");

		BuildContext context = createContext(false);

		// Add finders
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(artifactsDir));
		context.addBuildParticipant(new PluginFinder(artifactsDir));
		context.addBuildParticipant(new EclipseFeatureFinder(artifactsDir));
		context.addBuildParticipant(new ToolProductSpecificationFinder(artifactsDir));
		context.addBuildParticipant(new EclipseUpdateSiteFinder(distDir));
		// Add build step to create tool products
		context.addBuildParticipant(new BuildToolProductStepProvider());
		
		AutoBuilder builder = new AutoBuilder(context);
		
		AntScript script = new AntScript();
		script.setName("Build Eclipse tool product(s)");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}
	
	@Override
	public int getPriority() {
		return 15000;
	}
}
