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
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * The {@link PackageEMFTextExamplesStepProvider} add a 
 * {@link PackageEMFTextExamplesStep} for each example project found.
 */
public class PackageEMFTextExamplesStepProvider extends AbstractAntTargetGeneratorProvider {

	private String[] exampleExtensions = new String[] {".example", ".examples"};
	
	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			String languageID = plugin.getIdentifier();
			int idx = languageID.lastIndexOf(".resource.");
			if (idx != -1) {
				languageID = languageID.substring(0, idx);
				for (String exampleExtension : exampleExtensions) {
					String examplePlugin = languageID + exampleExtension;
					File examplePluginDir = new File(plugin.getFile().getParent(), examplePlugin);
					if (examplePluginDir.exists()) {
						return Collections.<IAntTargetGenerator>singletonList(
								new PackageEMFTextExamplesStep(plugin, examplePluginDir));
					}
				}
			}
		}
		return Collections.emptyList();
	}
}
