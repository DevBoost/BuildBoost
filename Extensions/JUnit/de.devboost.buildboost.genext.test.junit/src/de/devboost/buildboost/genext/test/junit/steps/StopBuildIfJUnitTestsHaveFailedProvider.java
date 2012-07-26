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
package de.devboost.buildboost.genext.test.junit.steps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.util.XMLContent;

public class StopBuildIfJUnitTestsHaveFailedProvider extends AbstractAntTargetGeneratorProvider {
	
	private JUnitTestProjectDetector helper = new JUnitTestProjectDetector(RunJUnitTestsStepProvider.DEFAULT_TEST_CLASS_SUFFIXES);

	@Override
	public List<IAntTargetGenerator> getAntTargetGenerators(
			IBuildContext context, IArtifact artifact) {
		
		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			if (helper.containsTests(plugin)) {
				XMLContent content = new XMLContent();
				content.append("<fail message=\"Stopping build, because tests from plug-in '" + plugin.getIdentifier() + "' have failed.\" if=\"test-failed-" + plugin.getIdentifier() + "\" />");
				
				final AntTarget target = new AntTarget("check-junit-failures-" + plugin.getIdentifier(), content);
				target.getRequiredTargets().add(RunJUnitTestsStep.PREFIX + plugin.getIdentifier());
				
				IAntTargetGenerator generator = new AbstractAntTargetGenerator() {
					
					@Override
					public Collection<AntTarget> generateAntTargets() throws BuildException {
						return Collections.singletonList(target);
					}
				};
				return Collections.singletonList(generator);
			}
		}
		return Collections.emptyList();
	}
}
