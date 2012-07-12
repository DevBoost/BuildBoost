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
package de.devboost.buildboost.genext.emftext.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseFeatureFinder;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.filters.IdentifierFilter;
import de.devboost.buildboost.filters.NegatingFilter;
import de.devboost.buildboost.genext.emftext.discovery.CsFinder;
import de.devboost.buildboost.genext.emftext.steps.GenerateResourcePluginsStepProvider;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class GenerateEMFTextCodeStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	public AntScript getScript() throws BuildException {
		File buildDir = new File(artifactsFolder);

		BuildContext context = createContext(true);

		addCSFilters(context);
		
		context.addBuildParticipant(new CsFinder(buildDir));
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(buildDir));
		context.addBuildParticipant(new EclipseFeatureFinder(buildDir));
		context.addBuildParticipant(new PluginFinder(buildDir));
		
		context.addBuildParticipant(new GenerateResourcePluginsStepProvider());

		AutoBuilder builder = new AutoBuilder(context);
		
		AntScript script = new AntScript();
		script.setName("Generate code from concrete syntax definitions");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}
	
	protected void addCSFilters(BuildContext context) {
		NegatingFilter filter1 = new NegatingFilter(new IdentifierFilter("concretesyntax.genmodel"));
		NegatingFilter filter2 = new NegatingFilter(new IdentifierFilter("concretesyntax.cs"));
		NegatingFilter filter3 = new NegatingFilter(new IdentifierFilter("concretesyntax.newfile.cs"));
		context.addBuildParticipant(filter1);
		context.addBuildParticipant(filter2);
		context.addBuildParticipant(filter3);
	}
	
	@Override
	public int getPriority() {
		return 1001;
	}
}
