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
 * The {@link CopyPluginsAndFeaturesBuildStep} generates a script that copies the given bundled plug-ins and features to
 * a target directory.
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
		File sourceLocation;
		boolean isExtracted;
		if (pluginOrFeature instanceof CompiledPlugin) {
			CompiledPlugin plugin = (CompiledPlugin) pluginOrFeature;
			sourceLocation = plugin.getFile();
			File targetPlatformPluginsDir = new File(targetPlatformEclipseDir, "plugins");
			isExtracted = sourceLocation.isDirectory();
			if (isExtracted) {
				targetSubDir = new File(targetPlatformPluginsDir, pluginOrFeatureName).getAbsolutePath();
			} else {
				targetSubDir = targetPlatformPluginsDir.getAbsolutePath();
			}
		} else if (pluginOrFeature instanceof EclipseFeature) {
			EclipseFeature eclipseFeature = (EclipseFeature) pluginOrFeature;
			File targetPlatformFeaturesDir = new File(targetPlatformEclipseDir, "features");
			isExtracted = eclipseFeature.isExtracted();
			if (isExtracted) {
				// for extracted features, 'location' is set to the feature.xml
				// file.
				sourceLocation = eclipseFeature.getFile().getParentFile();
				targetSubDir = new File(targetPlatformFeaturesDir, pluginOrFeatureName).getAbsolutePath();
			} else {
				sourceLocation = eclipseFeature.getFile();
				targetSubDir = targetPlatformFeaturesDir.getAbsolutePath();
			}
		} else {
			throw new RuntimeException("Found unknown artifact type " + pluginOrFeatureName + " in "
					+ getClass().getSimpleName());
		}

		XMLContent content = new XMLContent();
		if (isExtracted) {
			content.append("<copy todir=\"" + targetSubDir + "\" includeEmptyDirs=\"true\">");
			content.append("<fileset dir=\"" + sourceLocation.getAbsolutePath() + "\">");
			content.append("<include name=\"**/*\"/>");
			content.append("</fileset>");
			content.append("</copy>");
		} else {
			content.append("<copy file=\"" + sourceLocation.getAbsolutePath() + "\" todir=\"" + targetSubDir + "\" />");
		}
		String artifactType = pluginOrFeature.getClass().getSimpleName().toLowerCase();
		String targetName = "copy-" + artifactType + "-" + pluginOrFeatureName;
		return Collections.singleton(new AntTarget(targetName, content));
	}
}
