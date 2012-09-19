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

import static de.devboost.buildboost.IConstants.NL;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link RunJUnitTestsStep} creates a script that runs all JUnit tests in
 * a given plug-in project. The results of the test are collected in a result
 * directory.
 */
public class RunJUnitTestsStep extends AbstractAntTargetGenerator {

	public static final String PREFIX = "run-tests-";

	private Plugin testPlugin;
	private File testResultDir;

	private Collection<String> testClassSuffixes;

	private List<String> excludedTestClasses;
	
	// TODO use inclusion patterns instead of test class suffixes
	public RunJUnitTestsStep(Plugin testPlugin, File testResultDir, Collection<String> testClassSuffixes) {
		this(testPlugin, testResultDir, testClassSuffixes, Collections.<String>emptyList());
	}

	public RunJUnitTestsStep(Plugin testPlugin, File testResultDir, Collection<String> testClassSuffixes, List<String> excludedTestClasses) {
		super();
		this.testPlugin = testPlugin;
		this.testResultDir = testResultDir;
		this.testClassSuffixes = testClassSuffixes;
		this.excludedTestClasses = excludedTestClasses;
	}

	public Collection<AntTarget> generateAntTargets() {
		return Collections.singleton(new AntTarget(PREFIX + testPlugin.getIdentifier(), generate(excludedTestClasses)));
	}

	public XMLContent generate(String[] excludedTestClasses) {
		return generate(Arrays.asList(excludedTestClasses));
	}
	
	public XMLContent generate(List<String> excludedTestClasses) {
		XMLContent sb = new XMLContent();
		File[] sourceFolders = testPlugin.getSourceFolders();
		String name = testPlugin.getIdentifier();
		String projectTestResultDir = getTestResultDir(testPlugin);
		String pathName = "test-classpath-" + testPlugin.getIdentifier();

		sb.append("<delete file=\"test-classpath.jar\" />");
		// remove results directory
		sb.append("<mkdir dir=\"" + projectTestResultDir + "\" />");
		sb.append("<path id=\"" + pathName + "\">");
		sb.append(new ClasspathHelper().getClasspath(testPlugin, true));
		sb.append("</path>");
		sb.append(NL);
		String propertyName = "prop-" + pathName; 
		sb.append("<manifestclasspath property=\"" + propertyName + "\" jarfile=\"test-classpath.jar\" maxparentlevels=\"10\">");
		sb.append("<classpath refid=\"" + pathName + "\" />");
		sb.append("</manifestclasspath>");
		sb.append(NL);
		sb.append("<jar destfile=\"test-classpath.jar\">");
		sb.append("<manifest>");
		sb.append("<attribute name=\"Class-Path\" value=\"${" + propertyName + "}\"/>");
		sb.append("</manifest>");
		sb.append("</jar>");
		sb.append(NL);
		sb.append("<property environment=\"env\" />");
		sb.append("<mkdir dir=\"" + projectTestResultDir + "\" />");
		sb.append(NL);
		sb.append("<junit errorproperty=\"test-failed-" + testPlugin.getIdentifier() + "\" failureproperty=\"test-failed-" + testPlugin.getIdentifier() + "\" haltonfailure=\"false\" haltonerror=\"false\" fork=\"true\" dir=\"" + testPlugin.getAbsolutePath() + "\" maxmemory=\"2048m\">");
		sb.append("<jvmarg value=\"-ea\" />");
		sb.append("<jvmarg value=\"-XX:MaxPermSize=256m\" />");
		sb.append("<jvmarg value=\"-Dfile.encoding=UTF-8\"/>");
		sb.append("<classpath>");
		sb.append("<path path=\"test-classpath.jar\"/>");
		sb.append("</classpath>");
		sb.append("<formatter type=\"xml\"/>");
		sb.append("<batchtest todir=\"" + projectTestResultDir + "\">");
		for (File sourceFolder : sourceFolders) {
			sb.append("<fileset dir=\"" + sourceFolder.getAbsolutePath() + "\">");
			for (String suffix : testClassSuffixes) {
				sb.append("<include name=\"**/**" + suffix + ".java\"/>");
			}
			for (String excludedTestClass : excludedTestClasses) {
				sb.append("<exclude name=\"" + excludedTestClass.replace(".", "/")+ ".java\"/>");
			}
			sb.append("</fileset>");
		}
		sb.append("</batchtest>");
		sb.append("</junit>");
		sb.append(NL);
		sb.append("<junitreport todir=\"" + projectTestResultDir + "\" tofile=\"SUITE-" + name + ".xml\">");
		sb.append("<fileset dir=\"" + projectTestResultDir + "\">");
		sb.append("<include name=\"TEST-*.xml\"/>");
		sb.append("</fileset>" + NL);
		sb.append("<report format=\"frames\" todir=\"" + projectTestResultDir + "\"/>");
		sb.append("</junitreport>");
		sb.append(NL);
		return sb;
	}

	protected String getTestResultDir(Plugin plugin) {
		String testResultsName = getTestResultDirName(plugin);
		String projectTestResultDir = testResultDir.getAbsolutePath() + File.separator + testResultsName;
		return projectTestResultDir;
	}

	private String getTestResultDirName(Plugin plugin) {
		return "results-" + plugin.getIdentifier();
	}
}
