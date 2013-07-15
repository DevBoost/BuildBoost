/*******************************************************************************
 * Copyright (c) 2006-2013
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
package de.devboost.buildboost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import de.devboost.buildboost.model.IBuildConfiguration;
import de.devboost.buildboost.model.IBuildStage;
import de.devboost.buildboost.util.ScriptSaver;
import de.devboost.buildboost.util.StreamUtil;

public class BuildScriptGeneratorRunner {

	public static void main(String[] args) throws BuildException {
		if (args.length < 2) {
			System.out.println("Wrong call to BuildScriptGeneratorRunner.main().");
			System.out.println("Usage: BuildScriptGeneratorRunner.main(pathToWorkspace, classToRun).");
			System.exit(1);
			return;
		}
		new BuildScriptGeneratorRunner().run(args);
	}

	private void run(String[] args) throws BuildException {
		String workspace = args[0];
		String classToRun = args[1];
		File stageFile = getStageFile(workspace);
		String stage = readLastStage(stageFile);
		int stageNumber = Integer.parseInt(stage);
		writeNextStage(stageFile, stageNumber);
		
		Class<?> clazzToRun = loadClassToRun(classToRun);

		System.out.println("INFO: BuildScriptGeneratorRunner: Generating script for stage " + stageNumber);
		System.out.println("INFO: BuildScriptGeneratorRunner: Generator class " + clazzToRun.getName());

		boolean isBuildConfiguration = IBuildConfiguration.class.isAssignableFrom(clazzToRun);
		System.out.println("INFO: BuildScriptGeneratorRunner: Generator class implements " + IBuildConfiguration.class.getName());
		if (isBuildConfiguration) {
			// instantiate and call via interface
			callViaInterface(clazzToRun, workspace, stageNumber);
		} else {
			// call main method (for backward compatibility)
			callMainMethod(clazzToRun, args);
		}
	}

	private File getStageFile(String workspace) {
		return new File(new File(workspace, "build"), "current_stage");
	}

	private Class<?> loadClassToRun(String classToRun) throws BuildException {
		Class<?> clazzToRun;
		try {
			clazzToRun = Class.forName(classToRun);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new BuildException("Can't find class " + classToRun);
		}
		return clazzToRun;
	}

	private void callViaInterface(Class<?> clazzToRun, String workspace, int stageNumber) throws BuildException {
		try {
			Object newInstance = clazzToRun.newInstance();
			IBuildConfiguration configuration = (IBuildConfiguration) newInstance;
			List<IBuildStage> buildStages = configuration.getBuildStages(workspace);
			IBuildStage currentStage = buildStages.get(stageNumber - 1);
			String buildDir = workspace + File.separator + "build";
			ScriptSaver scriptSaver = new ScriptSaver();
			File targetDir = new File(buildDir);
			scriptSaver.saveStage(targetDir, currentStage, scriptSaver.getStageFileName(stageNumber));
			scriptSaver.saveMasterScript(targetDir, buildStages);
			// if this is the last stage, we must remove the 'current_stage' stage file, because
			// otherwise subsequent builds will not start at the first stage. this particularly
			// applies to the bootstrapping build.
			if (stageNumber == buildStages.size()) {
				File stageFile = getStageFile(workspace);
				stageFile.delete();
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new BuildException("Can't instantiate class " + clazzToRun.getName());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new BuildException("Can't access class " + clazzToRun.getName());
		}
	}

	private void callMainMethod(Class<?> clazzToRun, String[] args) throws BuildException {
		Method mainMethod;
		try {
			mainMethod = clazzToRun.getMethod("main", String[].class);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new BuildException("Can't access method main() in class " + clazzToRun.getName());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BuildException("Can't find method main() in class " + clazzToRun.getName());
		}
		
		try {
			mainMethod.invoke(null, (Object) args);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new BuildException("Can't invoke main method in class " + clazzToRun.getName());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new BuildException("Can't invoke main method in class " + clazzToRun.getName());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new BuildException("Can't invoke main method in class " + clazzToRun.getName());
		}
	}

	private void writeNextStage(File stageFile, int stageNumber) throws BuildException {
		try {
			FileWriter writer = new FileWriter(stageFile);
			writer.write(Integer.toString(stageNumber + 1));
			writer.close();
		} catch (IOException ioe) {
			throw new BuildException("Can't write stage file (" + ioe.getMessage() + ").");
		}
	}

	private String readLastStage(File stageFile) throws BuildException {
		if (stageFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(stageFile);
				String stage = new StreamUtil().getContentAsString(fis);
				fis.close();
				return stage;
			} catch (IOException ioe) {
				throw new BuildException("Can't read stage file (" + ioe.getMessage() + ").");
			}
		}
		return "1";
	}
}
