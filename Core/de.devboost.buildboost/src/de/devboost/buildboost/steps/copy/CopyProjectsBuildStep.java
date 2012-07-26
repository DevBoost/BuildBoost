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

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link CopyProjectsBuildStep} generates a script that copies plug-in
 * projects from one directory (typically a SVN working copy) to another
 * directory (typically a directory where the actual build is performed). The
 * {@link CopyProjectsBuildStep} uses synchronization instead of pure copying
 * to avoid unnecessary copy operations.
 */
public class CopyProjectsBuildStep extends AbstractAntTargetGenerator {

	private File targetDir;
	private Plugin plugin;

	public CopyProjectsBuildStep(File targetDir, Plugin plugin) {
		super();
		this.targetDir = new File(targetDir, "projects");
		this.plugin = plugin;
	}

	public Collection<AntTarget> generateAntTargets() {
		String pluginName = plugin.getIdentifier();

		XMLContent content = new XMLContent();
		content.append("<sync todir=\"" + targetDir.getAbsolutePath() + File.separator + pluginName + "\" includeEmptyDirs=\"true\">");
		content.append("<fileset dir=\"" + plugin.getAbsolutePath() + "\">");
		content.append("<exclude name=\"bin/**/*\"/>"); //! in local build, Eclipse may compile classes here in addition
		content.append("</fileset>");
		content.append("<preserveintarget>");
		// TODO make this configurable / how does this interact with clean and with incremental build?
		content.append("<include name=\"**/*\"/>");
		content.append("</preserveintarget>");
		content.append("</sync>");
		return Collections.singleton(new AntTarget("copy-" + pluginName, content));
	}
}
