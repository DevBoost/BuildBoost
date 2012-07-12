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
package de.devboost.buildboost.steps.compile;

import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

public class CompileProjectStepProvider extends AbstractAntTargetGeneratorProvider {
	
	private String sourceFileEncoding;
	private JDKVersion jdkVersion;

	public CompileProjectStepProvider() {
		this(null, null);
	}

	public CompileProjectStepProvider(JDKVersion jdkVersion, String sourceFileEncoding) {
		super();
		this.jdkVersion = jdkVersion;
		this.sourceFileEncoding = sourceFileEncoding;
	}

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
