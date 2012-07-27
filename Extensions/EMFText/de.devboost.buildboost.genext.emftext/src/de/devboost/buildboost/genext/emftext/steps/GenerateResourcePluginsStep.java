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
package de.devboost.buildboost.genext.emftext.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static de.devboost.buildboost.genext.emftext.IConstants.*;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.IConstants;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emftext.artifacts.ConcreteSyntaxDefinition;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link GenerateResourcePluginsStep} generates a build script the can be
 * used to run the EMFText code generators for a syntax definition.
 */
public class GenerateResourcePluginsStep extends AbstractAntTargetGenerator {

	private ConcreteSyntaxDefinition syntaxDefinition;
	private List<Plugin> plugins;

	public GenerateResourcePluginsStep(List<Plugin> plugins, ConcreteSyntaxDefinition syntaxDefinition) {
		this.plugins = plugins;
		this.syntaxDefinition = syntaxDefinition;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		Collection<IDependable> dependencies = syntaxDefinition.getDependencies();
		if (dependencies.isEmpty()) {
			throw new BuildException("Concrete syntax definitions are expected to have a dependency to the EMFText SDK.");
		}
		XMLContent classpath = new ClasspathHelper().getClasspath(syntaxDefinition, true);

		String csFilePath = syntaxDefinition.getFile().getAbsolutePath();

		XMLContent content = new XMLContent();
		content.append("<echo message=\"Generating text resource plug-ins for concrete syntax definition " + csFilePath + "\" />");
		content.append("<java fork=\"true\" classname=\""+ BUILDEXT_EXECUTABLE + "\" failonerror=\"true\">");
		content.append("<jvmarg value=\"-XX:MaxPermSize=256m\"/>");
		content.append("<jvmarg value=\"-Xmx2048m\"/>");
		content.append("<arg value=\"${ECLIPSE_HOME}\"/>");
		content.append("<arg value=\"" + csFilePath + "\"/>");
		content.append("<arg value=\"" + syntaxDefinition.getProjectDir().getName() + "\"/>");
		content.append("<arg value=\"" + syntaxDefinition.getProjectDir().getParentFile().getAbsolutePath() + "\"/>");
		for (Plugin plugin : plugins) {
			content.append("<arg value=\"" + plugin.getAbsolutePath()+ "\"/>");
		}
		content.append("<classpath>");
		content.append(classpath);
		content.append("</classpath>");
		content.append("</java>");
		content.append(IConstants.NL);
		
		String csID = csFilePath.replace(File.separator, "-");
		return Collections.singleton(new AntTarget("emftext-codegen-" + csID, content));
	}
}
