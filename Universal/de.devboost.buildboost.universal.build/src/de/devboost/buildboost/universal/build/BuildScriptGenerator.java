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
package de.devboost.buildboost.universal.build;

import static de.devboost.buildboost.IConstants.ARTIFACTS_FOLDER;
import static de.devboost.buildboost.IConstants.BUILD_BOOST_BIN_FOLDER;
import static de.devboost.buildboost.IConstants.BUILD_FOLDER;
import static de.devboost.buildboost.IConstants.REPOS_FOLDER;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.devboost.buildboost.model.IBuildConfiguration;
import de.devboost.buildboost.model.IBuildStage;
import de.devboost.buildboost.model.IUniversalBuildStage;

public class BuildScriptGenerator implements IBuildConfiguration {
	
	@Override
	public List<IBuildStage> getBuildStages(String workspace) {
		Map<String, String> properties = new LinkedHashMap<String, String>();
		
		File buildFolder = new File(workspace, BUILD_FOLDER);
		File buildBoostBinFolder = new File(buildFolder, BUILD_BOOST_BIN_FOLDER);
		
		File reposFolder = new File(workspace, REPOS_FOLDER);
		File artifactsFolder = new File(buildFolder, ARTIFACTS_FOLDER);
		
		properties.put("reposfolder", reposFolder.getAbsolutePath());
		properties.put("artifactsfolder", artifactsFolder.getAbsolutePath());
		
		List<IBuildStage> stages = collectStages(buildBoostBinFolder);
		
		setPropertiesInStages(properties, stages);

		System.out.println("Boost! Universal Build: " + stages);
		
		return stages;
	}
	
	protected void setPropertiesInStages(Map<String, String> properties,
			List<IBuildStage> stages) {
		for (IBuildStage stage : stages) {
			setPropertiesInStage(stage, properties);
		}
	}

	private void setPropertiesInStage(IBuildStage stage,
			Map<String, String> properties) {

		String setMethodPrefix = "set";
		// we examine the concrete stage class for setters and invoke them
		// if we've got a matching property 
		for (Method m : stage.getClass().getMethods()) {
			String methodName = m.getName();
			if (methodName.startsWith(setMethodPrefix)) {
				String propertyName = methodName.substring(setMethodPrefix.length()).toLowerCase();
				String propertyValue = properties.get(propertyName);
				if (propertyValue == null) {
					String message = "WARNING: Can't find value for property '" +
							propertyName + 
							"' in stage " + stage;
					System.out.println(message);
					continue;
				}
				try {
					m.invoke(stage, propertyValue);
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
				}
			}
		}
	}
	
	protected List<IBuildStage> collectStages(File buildBoostBinFolder) {
		List<IBuildStage> result = new ArrayList<IBuildStage>();
		List<String> javaClassNames = new ArrayList<String>();
		collectClasses(javaClassNames, buildBoostBinFolder, null);
		for (String className : javaClassNames) {
			try {
				Class<?> buildStageCand = Class.forName(className);
				if (IUniversalBuildStage.class.isAssignableFrom(buildStageCand) && !buildStageCand.isInterface()) {
					IUniversalBuildStage newInstance = (IUniversalBuildStage) buildStageCand.newInstance();
					result.add(newInstance);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(result, new Comparator<IBuildStage>() {
			
			@Override
			public int compare(IBuildStage bs1, IBuildStage bs2) {
				IUniversalBuildStage ubs1 = (IUniversalBuildStage) bs1;
				IUniversalBuildStage ubs2 = (IUniversalBuildStage) bs2;
				int priority1 = ubs1.getPriority();
				int priority2 = ubs2.getPriority();
				return new Integer(priority1).compareTo(new Integer(priority2));
			}
		});
		
		return result;
	}

	protected void collectClasses(List<String> javaClassNames, File file, String name) {
		if (isHidden(file)) {
			return;
		}
		String fileName = file.getName();
		String classFileSuffix = ".class";
		if (fileName.endsWith(classFileSuffix) && !fileName.contains("$")) {
			name = name + fileName.substring(0, fileName.length() - classFileSuffix.length());
			javaClassNames.add(name);
			return;
		}
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				String nextName;
				if (name == null) {
					nextName = "";
				} else {
					nextName = name + fileName + ".";
				}
				collectClasses(javaClassNames, child, nextName);
			}
		}
	}

	private boolean isHidden(File file) {
		return file.getName().startsWith(".");
	}

}
