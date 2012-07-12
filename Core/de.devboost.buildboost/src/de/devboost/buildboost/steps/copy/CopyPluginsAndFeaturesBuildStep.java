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
 * The {@link CopyPluginsAndFeaturesBuildStep} generates a script the copies the given
 * bundled plug-in (JAR file) to a target directory.
 */
public class CopyPluginsAndFeaturesBuildStep extends AbstractAntTargetGenerator {

	private IArtifact pluginOrFeature;
	private File targetDir;
	
	public CopyPluginsAndFeaturesBuildStep(IArtifact pluginOrFeature, File targetDir) {
		super();
		this.pluginOrFeature = pluginOrFeature;
		this.targetDir = targetDir;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		String pluginOrFeatureName = pluginOrFeature.getIdentifier();

		String targetSubDir;
		File location;
		if (pluginOrFeature instanceof CompiledPlugin) {
			location = ((CompiledPlugin) pluginOrFeature).getFile();
			File pluginsFolder = new File(targetDir, "plugins");
			if (location.isDirectory()) {
				targetSubDir = new File(pluginsFolder, pluginOrFeatureName).getAbsolutePath();
			} else {
				targetSubDir = pluginsFolder.getAbsolutePath();
			}
		} else {
			location = ((EclipseFeature) pluginOrFeature).getFile();
			targetSubDir = new File(new File(targetDir, "features"), pluginOrFeatureName).getAbsolutePath();
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
		return Collections.singleton(new AntTarget("copy-"
				+ pluginOrFeature.getClass().getSimpleName().toLowerCase() + "-" + pluginOrFeatureName, content));
	}
}
