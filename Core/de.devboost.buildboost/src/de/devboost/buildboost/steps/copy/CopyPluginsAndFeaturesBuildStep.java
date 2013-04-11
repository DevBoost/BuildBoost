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
package de.devboost.buildboost.steps.copy;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link CopyPluginsAndFeaturesBuildStep} generates a script that copies
 * the given bundled plug-ins and features to a target directory.
 */
public class CopyPluginsAndFeaturesBuildStep extends AbstractAntTargetGenerator {

	private IArtifact pluginOrFeature;
	private File targetPlatformEclipseDir;
	
	public CopyPluginsAndFeaturesBuildStep(IArtifact pluginOrFeature, File targetDir) {
		super();
		this.pluginOrFeature = pluginOrFeature;
		File targetPlatformDir = new File(targetDir, "target-platform");
		this.targetPlatformEclipseDir = new File(targetPlatformDir, "eclipse");
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		String pluginOrFeatureName = pluginOrFeature.getIdentifier();

		String targetSubDir;
		File location;
		if (pluginOrFeature instanceof CompiledPlugin) {
			CompiledPlugin plugin = (CompiledPlugin) pluginOrFeature;
			location = plugin.getFile();
			File targetPlatformPluginsDir = new File(targetPlatformEclipseDir, "plugins");
			if (location.isDirectory()) {
				targetSubDir = new File(targetPlatformPluginsDir, pluginOrFeatureName).getAbsolutePath();
			} else {
				targetSubDir = targetPlatformPluginsDir.getAbsolutePath();
			}
		} else if (pluginOrFeature instanceof EclipseFeature) {
			EclipseFeature eclipseFeature = (EclipseFeature) pluginOrFeature;
			location = eclipseFeature.getFile();
			File targetPlatformFeaturesDir = new File(targetPlatformEclipseDir, "features");
			if (eclipseFeature.isExtracted()) {
				targetSubDir = new File(targetPlatformFeaturesDir, pluginOrFeatureName).getAbsolutePath();
			} else {
				targetSubDir = targetPlatformFeaturesDir.getAbsolutePath();
			}
		} else {
			throw new RuntimeException("Found unknown artifact type " + pluginOrFeatureName + " in " + getClass().getSimpleName());
		}
		
		XMLContent content = new XMLContent();
		if (location.isFile()) {
			content.append("<copy file=\"" + location.getAbsolutePath() + "\" todir=\"" + targetSubDir + "\" />");
		} else {
			content.append("<copy todir=\"" + targetSubDir + "\" includeEmptyDirs=\"true\">");
			content.append("<fileset dir=\"" + location.getAbsolutePath() + "\">");
			content.append("<include name=\"**/*\"/>");
			content.append("</fileset>");
			content.append("</copy>");
		}
		String artifactType = pluginOrFeature.getClass().getSimpleName().toLowerCase();
		String targetName = "copy-" + artifactType + "-" + pluginOrFeatureName;
		return Collections.singleton(new AntTarget(targetName, content));
	}
}
