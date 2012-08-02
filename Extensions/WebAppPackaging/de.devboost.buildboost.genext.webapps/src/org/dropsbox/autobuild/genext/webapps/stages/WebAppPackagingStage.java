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
package org.dropsbox.autobuild.genext.webapps.stages;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.dropsbox.autobuild.genext.webapps.discovery.WebAppFinder;
import org.dropsbox.autobuild.genext.webapps.steps.WebAppPackagingStepProvider;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class WebAppPackagingStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	@Override
	public AntScript getScript() throws BuildException {
		Collection<UnresolvedDependency> webAppDendencies = getWebAppDendencies();

		BuildContext context = createContext(false);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(new File(artifactsFolder)));
		context.addBuildParticipant(new PluginFinder(new File(artifactsFolder)));
		context.addBuildParticipant(new WebAppFinder(webAppDendencies));
		
		context.addBuildParticipant(new WebAppPackagingStepProvider(webAppDendencies));
		
		AutoBuilder builder = new AutoBuilder(context);
		AntScript script = new AntScript();
		script.setName("Package web application as WAR files");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}

	private Collection<UnresolvedDependency> getWebAppDendencies() {
		UnresolvedDependency dependency = new UnresolvedDependency(
				Plugin.class, 
				"org.apache.tomcat_6_0_32", // TODO make this configurable 
				null,
				true,
				null, 
				true,
				false, 
				false);
		return Collections.singletonList(dependency);
	}

	@Override
	public int getPriority() {
		return 11000;
	}
}
