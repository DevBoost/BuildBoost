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
package de.devboost.buildboost.genext.cmdlineapp.steps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

public class CommandlineAppPackagingStepProvider extends AbstractAntTargetGeneratorProvider {

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>(1);
		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			File[] sourceFolders = plugin.getSourceFolders();
			// Check whether one of the source folders contains a Main.java in
			// a package that has the same name as the plug-in. If this is the
			// case, we package the plug-in as a executable JAR file.
			for (File sourceFolder : sourceFolders) {
				File mainClass = new File(sourceFolder, plugin.getIdentifier().replace(".", File.separator) + File.separator + "Main.java");
				if (mainClass.exists()) {
					steps.add(new CommandlineAppPackagingStep(plugin));
				}
			}
		}
		return steps;
	}
}
