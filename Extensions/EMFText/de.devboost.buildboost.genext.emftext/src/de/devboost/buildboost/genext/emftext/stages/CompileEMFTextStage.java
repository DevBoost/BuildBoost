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
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.filters.IdentifierFilter;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;
import de.devboost.buildboost.steps.compile.CompileProjectStepProvider;

public class CompileEMFTextStage extends AbstractBuildStage implements IUniversalBuildStage {

	private static final Set<String> EMFTEXT_SDK_PLUGIN_IDENTIFIERS = new LinkedHashSet<String>();
	
	static {
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.access");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.ant");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.antlr3_4_0");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.automaton");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.antlr");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.newproject");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.resource");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.codegen.resource.ui");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax.edit");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax.resource.cs");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.concretesyntax.resource.cs.ui");
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("org.emftext.sdk.ui");
		// also compile BuildBoost EMF plug-in
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("de.devboost.buildboost.buildext.emf");
		// also compile BuildBoost EMFText plug-in
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("de.devboost.buildboost.buildext.emftext");
		// also compile BuildBoost webapps plug-in
		EMFTEXT_SDK_PLUGIN_IDENTIFIERS.add("de.devboost.buildboost.genext.webapps");
	}

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}

	public AntScript getScript() throws BuildException {
		BuildContext context = createContext(false);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(new File(artifactsFolder)));
		context.addBuildParticipant(new PluginFinder(new File(artifactsFolder)));
		
		context.addBuildParticipant(new CompileProjectStepProvider());
		
		context.addBuildParticipant(new IdentifierFilter(EMFTEXT_SDK_PLUGIN_IDENTIFIERS));
		
		AutoBuilder builder = new AutoBuilder(context);

		AntScript script = new AntScript();
		script.setName("Compile EMFText SDK plug-ins");
		script.addTargets(builder.generateAntTargets());
		return script;
	}
	
	@Override
	public int getPriority() {
		return 100;
	}
}
