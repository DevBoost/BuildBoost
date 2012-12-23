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
package de.devboost.buildboost.genext.webapps.steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.webapps.util.WebAppUtil;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.UnresolvedDependency;

public class WebAppPackagingStepProvider extends AbstractAntTargetGeneratorProvider {

	private Collection<UnresolvedDependency> webAppDependencies;
	
	public WebAppPackagingStepProvider(Collection<UnresolvedDependency> webAppDependencies) {
		super();
		this.webAppDependencies = webAppDependencies;
	}

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>(1);
		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			if (plugin.isProject()) {
				if (new WebAppUtil().isWebApp(plugin)) {
					steps.add(new WebAppPackagingStep(plugin, webAppDependencies));
				}
			}
		}
		return steps;
	}
}
