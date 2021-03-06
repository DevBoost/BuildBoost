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
package de.devboost.buildboost.genext.test.junit.stages;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.genext.test.junit.steps.RunJUnitTestsStepProvider;
import de.devboost.buildboost.genext.test.junit.steps.StopBuildIfJUnitTestsHaveFailedProvider;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class JUnitTestStage extends AbstractBuildStage implements IUniversalBuildStage {

	public static final String TEST_RESULTS_FOLDER = "test-results";

	private String artifactsFolder;

	private Set<String> excludedTestClasses = new LinkedHashSet<String>();
	
	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}
	
	public void addExcludedTestClass(String classToExclude) {
		this.excludedTestClasses.add(classToExclude);
	}

	public AntScript getScript() throws BuildException {
		checkConfiguration();
		
		File artifactsDir = new File(artifactsFolder);
		File testResultDir = new File(artifactsDir.getParentFile(), TEST_RESULTS_FOLDER);

		BuildContext context = createContext(false);
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(artifactsDir));
		context.addBuildParticipant(new PluginFinder(artifactsDir));
		
		context.addBuildParticipant(new RunJUnitTestsStepProvider(testResultDir, new ArrayList<String>(excludedTestClasses)));
		context.addBuildParticipant(new StopBuildIfJUnitTestsHaveFailedProvider());
		
		AutoBuilder builder = new AutoBuilder(context);
		
		AntScript script = new AntScript();
		script.setName("Run JUnit tests");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}

	private void checkConfiguration() throws BuildException {
		if (artifactsFolder == null) {
			throw new BuildException("artifactsFolder is not set in " + getClass().getSimpleName());
		}
	}
	
	@Override
	public int getPriority() {
		return 9000;
	}
}
