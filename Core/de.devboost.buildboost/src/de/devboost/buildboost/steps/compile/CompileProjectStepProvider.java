/*******************************************************************************
 * Copyright (c) 2006-2016
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
package de.devboost.buildboost.steps.compile;

import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

public class CompileProjectStepProvider extends AbstractAntTargetGeneratorProvider {

	private final String sourceFileEncoding;
	private final JDKVersion jdkVersion;

	public CompileProjectStepProvider() {
		this(null, null);
	}

	public CompileProjectStepProvider(JDKVersion jdkVersion, String sourceFileEncoding) {
		this.jdkVersion = jdkVersion;
		this.sourceFileEncoding = sourceFileEncoding;
	}

	@Override
	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			IAntTargetGenerator step = new CompileProjectStep(plugin, jdkVersion, sourceFileEncoding);
			return Collections.singletonList(step);
		} else {
			return Collections.emptyList();
		}
	}
}
