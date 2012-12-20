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
package de.devboost.buildboost.genext.emf.steps;

import static de.devboost.buildboost.IConstants.NL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
import de.devboost.buildboost.steps.clone.CloneRepositoriesBuildStep;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link GenerateGenModelCodeStep} generates a script that calls the EMF
 * code generators to obtain code from Ecore models.
 */
public class GenerateGenModelCodeStep extends AbstractAntTargetGenerator {

	public final static String MAIN_TASK = "generate-emf-code";

	private final List<Plugin> plugins;
	private final GeneratorModel generatorModel;

	public GenerateGenModelCodeStep(List<Plugin> plugins,
			GeneratorModel generatorModel) {
		this.plugins = plugins;
		this.generatorModel = generatorModel;
	}

	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		Collection<IDependable> dependencies = generatorModel.getDependencies();
		if (dependencies.isEmpty()) {
			throw new BuildException(
					"Generator models are expected to have a dependency to the BuildBoost EMF plug-in.");
		}
		XMLContent classpath = new ClasspathHelper().getClasspath(
				generatorModel, true);

		File genModelFile = generatorModel.getFile();
		String genModelPath = genModelFile.getAbsolutePath();

		XMLContent sb = new XMLContent();
		sb.append("<delete dir=\"temp_eclipse_workspace\" />");
		sb.append("<mkdir dir=\"temp_eclipse_workspace\" />");
		sb.append(NL);

		sb.append("<echo message=\"Generating EMF model code for generator model "
				+ genModelPath + "\" />");
		// TODO use constant here
		sb.append("<java fork=\"true\" classname=\""
				+ IConstants.BUILDEXT_EXECUTABLE + "\" failonerror=\"true\">");
		sb.append("<jvmarg value=\"-XX:MaxPermSize=256m\"/>");
		sb.append("<jvmarg value=\"-Xmx1024m\"/>");
		sb.append("<arg value=\"" + genModelPath + "\"/>");
		sb.append("<arg value=\"" + generatorModel.getProjectDir().getName()
				+ "\"/>");
		sb.append("<arg value=\"" + generatorModel.getProjectDir() + "\"/>");
		String genModelID = genModelPath.toString()
				.replace(File.separator, "-");
		String paraFileName = CloneRepositoriesBuildStep
				.encodeFileOrFolderName(genModelID + ".properties");
		sb.append("<arg value=\"" + paraFileName + "\"/>");
		sb.append("<classpath>");
		sb.append(classpath);
		sb.append("</classpath>");
		sb.append("</java>");
		sb.append(NL);

		writeParaFile(paraFileName, plugins);

		return Collections.singleton(new AntTarget("emf-codegen-" + genModelID,
				sb));
	}

	private void writeParaFile(final String fileName, final List<Plugin> plugins) {
		final File paraPropFile = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(paraPropFile);
			for (Plugin plugin : plugins) {
				pw.println(plugin.getAbsolutePath());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}

	}
}
