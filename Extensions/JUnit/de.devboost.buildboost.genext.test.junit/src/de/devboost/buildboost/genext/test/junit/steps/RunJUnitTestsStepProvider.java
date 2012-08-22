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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * The {@link RunJUnitTestsStepProvider} add a {@link RunJUnitTestsStep} for
 * every plug-in project that contains JUnit test classes.
 */
public class RunJUnitTestsStepProvider extends AbstractAntTargetGeneratorProvider {
	
	public static final Collection<String> DEFAULT_TEST_CLASS_SUFFIXES = new LinkedHashSet<String>();
	static {
		DEFAULT_TEST_CLASS_SUFFIXES.add("Test");
		DEFAULT_TEST_CLASS_SUFFIXES.add("Tests");
		DEFAULT_TEST_CLASS_SUFFIXES.add("TestSuite");
	}

	private File testResultDir;
	private Collection<String> testClassSuffixes;
	private List<String> excludedTestClasses;
	
	public RunJUnitTestsStepProvider(File testResultDir) {
		this(testResultDir, Collections.<String>emptyList());
	}
	
	// TODO use test class patterns instead of test class suffixes
	public RunJUnitTestsStepProvider(File testResultDir, List<String> excludedTestClasses) {
		this(testResultDir, excludedTestClasses, DEFAULT_TEST_CLASS_SUFFIXES);
	}
	
	public RunJUnitTestsStepProvider(File testResultDir, List<String> excludedTestClasses, Collection<String> testClassSuffixes) {
		this.testResultDir = testResultDir;
		this.testClassSuffixes = testClassSuffixes;
		this.excludedTestClasses = excludedTestClasses;
	}
	
	public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context, IArtifact artifact) {
		JUnitTestProjectDetector helper = new JUnitTestProjectDetector(testClassSuffixes);

		if (artifact instanceof Plugin) {
			Plugin plugin = (Plugin) artifact;
			// check if this plug-in contains JUnit test cases
			if (helper.containsTests(plugin)) {
				List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>(1);
				steps.add(new RunJUnitTestsStep(plugin, testResultDir, testClassSuffixes, excludedTestClasses));
				return steps;
			}
		}
		return Collections.emptyList();
	}
}
