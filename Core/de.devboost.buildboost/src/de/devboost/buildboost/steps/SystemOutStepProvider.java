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
package de.devboost.buildboost.steps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link SystemOutStepProvider} generates a script the prints all artifacts
 * to the console.
 */
public class SystemOutStepProvider extends AbstractAntTargetGeneratorProvider {

	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, final IArtifact artifact) {
		IAntTargetGenerator step = new AbstractAntTargetGenerator() {
			
			public Collection<AntTarget> generateAntTargets() {
				XMLContent content = new XMLContent();
				content.append("<echo message=\"" + artifact.getClass().getSimpleName() + ": " + artifact.getIdentifier() + "\" />");
				return Collections.singleton(new AntTarget("output", content));
			}
		};
		return Collections.singletonList(step);
	}
}
