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
package de.devboost.buildboost.buildext.test.junit.steps;

import static de.devboost.buildboost.IConstants.NL;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.util.XMLContent;

/**
 * A utility class to generate scripts that run JUnit test.
 */
public class JUnitTestScriptGenerator {

	public String getTestRunnerScript(File buildDir, Set<Plugin> testPlugins, String[] excludedTestClasses) {
		final String prefix = RunJUnitTestsStep.PREFIX;

		XMLContent content = new XMLContent();
		content.append("<project default=\"run-all-tests\">");
		content.append("<property name=\"test_result_dir\" value=\"test_results\"/>");
		// TODO this does not belong here
		/*
		content.append("<property file=\"org.dropsbox/local.build.properties\"/>");
		content.append(NL);
		*/
		
		// TODO this does not belong here
		Set<String> excludedPlugins = new LinkedHashSet<String>();
		/*
		excludedPlugins.add("org.emftext.refactoring.rolemodelmatching.long.test");
		excludedPlugins.add("org.emftext.refactoring.rolemodelmatching.test");
		excludedPlugins.add("org.emftext.language.java.test.bulk");
		excludedPlugins.add("org.reuseware.application.ticketshop.test");
		*/
		
		String allNames = getCommaSeparatedList(testPlugins, prefix, excludedPlugins);
		content.append("<target name=\"run-all-tests\" depends=\"" + allNames + "\">");
		
		for (Plugin testPlugin : testPlugins) {
			if (excludedPlugins.contains(testPlugin.getIdentifier())) {
				continue;
			}
			content.append("<fail message=\"Stopping build, because tests from plug-in '" + testPlugin.getIdentifier() + "' have failed.\" if=\"test-failed-" + testPlugin.getIdentifier() + "\" />");
		}
		content.append("</target>");
		content.append(NL);

		for (Plugin testPlugin : testPlugins) {
			content.append("<target name=\"" + prefix + testPlugin.getIdentifier() + "\">");
			content.append(new RunJUnitTestsStep(testPlugin, new File(buildDir, "test_results"), RunJUnitTestsStepProvider.DEFAULT_TEST_CLASS_SUFFIXES).generate(excludedTestClasses));
			content.append("</target>");
		}
		content.append("</project>");
		return content.toString();
	}

	protected String getCommaSeparatedList(Set<Plugin> pluginSet, String prefix, Set<String> excludedPlugins) {
		List<Plugin> pluginList = new ArrayList<Plugin>();
		pluginList.addAll(pluginSet);
		
		List<String> pluginNames = new ArrayList<String>();
		for (Plugin plugin : pluginList) {
			String name = plugin.getIdentifier();
			if (excludedPlugins.contains(name)) {
				continue;
			}
			pluginNames.add(name);
		}
		
		StringBuffer nameList = new StringBuffer();
		int size = pluginNames.size();
		for (int i = 0; i < size; i++) {
			nameList.append(prefix);
			String name = pluginNames.get(i);
			nameList.append(name);
			if (i < size - 1) {
				nameList.append(",");
			}
		}
		return nameList.toString();
	}
}
