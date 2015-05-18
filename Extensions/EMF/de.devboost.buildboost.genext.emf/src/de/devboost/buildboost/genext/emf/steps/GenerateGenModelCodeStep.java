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
package de.devboost.buildboost.genext.emf.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emf.IConstants;
import de.devboost.buildboost.genext.emf.artifacts.GeneratorModel;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link GenerateGenModelCodeStep} generates a script that calls the EMF
 * code generators to obtain code from Ecore models. 
 */
public class GenerateGenModelCodeStep extends AbstractAntTargetGenerator {

	private List<Plugin> plugins;
	private GeneratorModel generatorModel;

	/**
	 * Creates a new build step that provides a script to generate code from an
	 * EMF generator model.
	 * 
	 * @param plugins
	 *            all plug-in available in this build (used to resolved
	 *            references in the generator model to other generator models)
	 * @param generatorModel
	 *            the model to generate code from
	 */
	public GenerateGenModelCodeStep(List<Plugin> plugins, GeneratorModel generatorModel) {
		this.plugins = plugins;
		this.generatorModel = generatorModel;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		Collection<IDependable> dependencies = generatorModel.getDependencies();
		if (dependencies.isEmpty()) {
			throw new BuildException("Generator models are expected to have a dependency to the BuildBoost EMF plug-in.");
		}
		
		XMLContent classpath = new ClasspathHelper().getClasspath(generatorModel, true);

		File genModelProject = generatorModel.getProjectDir();
		File genModelFile = generatorModel.getFile();
		String genModelPath = genModelFile.getAbsolutePath();

		XMLContent sb = new XMLContent();
		
		sb.append("<echo message=\"Generating EMF model code for generator model " + genModelPath + "\" />");
		sb.append("<java fork=\"true\" classname=\"" + IConstants.BUILDEXT_EXECUTABLE + "\" failonerror=\"true\">");
		sb.append("<jvmarg value=\"-XX:MaxPermSize=256m\"/>");
		sb.append("<jvmarg value=\"-Xmx2048m\"/>");
		sb.append("<arg value=\"" + genModelPath + "\"/>");
		sb.append("<arg value=\"" + genModelProject.getName() + "\"/>");
		sb.append("<arg value=\"" + genModelProject + "\"/>");
		for (Plugin plugin : plugins) {
			sb.append("<arg value=\"" + plugin.getAbsolutePath()+ "\"/>");
		}
		sb.append("<classpath>");
		sb.append(classpath);
		sb.append("</classpath>");
		sb.append("</java>");
		sb.appendLineBreak();
		
		String genModelID = genModelPath.replace(File.separator, "-");
		String targetName = "emf-codegen-" + genModelID;
		
		return Collections.singleton(new AntTarget(targetName, sb));
	}
}
