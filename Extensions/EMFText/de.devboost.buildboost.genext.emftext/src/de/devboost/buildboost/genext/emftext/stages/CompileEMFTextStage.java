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
package de.devboost.buildboost.genext.emftext.stages;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.filters.ArtifactTypeFilter;
import de.devboost.buildboost.filters.IdentifierFilter;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;
import de.devboost.buildboost.steps.compile.CompileProjectStepProvider;

public class CompileEMFTextStage extends AbstractBuildStage implements IUniversalBuildStage {

	private static final Set<String> REQUIRED_PLUGIN_IDENTIFIERS = new LinkedHashSet<String>();
	
	static {
		// Code Composers
		REQUIRED_PLUGIN_IDENTIFIERS.add("de.devboost.codecomposers");
		// EMFText core
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.access");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.ant");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.antlr3_4_0");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.automaton");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.antlr");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.newproject");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.resource");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.resource.ui");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax.edit");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax.resource.cs");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax.resource.cs.ui");
		REQUIRED_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.ui");
		// BuildBoost EMFText plug-in
		REQUIRED_PLUGIN_IDENTIFIERS.add("de.devboost.buildboost.buildext.emftext");
	}

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	public AntScript getScript() throws BuildException {
		File artifactsFolderFile = new File(artifactsFolder);

		BuildContext context = createContext(false);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(artifactsFolderFile));
		context.addBuildParticipant(new PluginFinder(artifactsFolderFile));
		
		context.addBuildParticipant(new CompileProjectStepProvider());
		
		context.addBuildParticipant(new IdentifierFilter(REQUIRED_PLUGIN_IDENTIFIERS).or(
				new ArtifactTypeFilter(CompiledPlugin.class)));
		
		AutoBuilder builder = new AutoBuilder(context);

		AntScript script = new AntScript();
		script.setName("Compile EMFText SDK plug-ins and its dependencies");
		script.addTargets(builder.generateAntTargets());
		return script;
	}
	
	@Override
	public int getPriority() {
		return 100;
	}
}
