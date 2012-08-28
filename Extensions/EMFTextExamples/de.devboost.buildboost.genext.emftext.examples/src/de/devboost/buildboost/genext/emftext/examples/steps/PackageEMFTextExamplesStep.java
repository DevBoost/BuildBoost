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
package de.devboost.buildboost.genext.emftext.examples.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link PackageEMFTextExamplesStep} generates a build script the can be
 * used to run the EMFText code generators for a syntax definition.
 */
public class PackageEMFTextExamplesStep extends AbstractAntTargetGenerator {

	private File exampleDir;
	private Plugin plugin;

	public PackageEMFTextExamplesStep(Plugin plugin, File exampleDir) {
		this.plugin = plugin;
		this.exampleDir = exampleDir;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		XMLContent content = new XMLContent();
		content.append("<zip destfile=\"" + plugin.getAbsolutePath() + "/newProject.zip\">");
		content.append("<fileset dir=\"" + exampleDir + "\" excludes=\"**/.svn\"/>");
		content.append("</zip>");

		return Collections.singleton(new AntTarget("zipping-emftext-example-" + exampleDir.getName(), content));
	}
}
