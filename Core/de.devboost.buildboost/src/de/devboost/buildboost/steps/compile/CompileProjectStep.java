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
package de.devboost.buildboost.steps.compile;

import static de.devboost.buildboost.IConstants.NL;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link CompileProjectStep} generates a script that compiles a plug-in
 * project.
 */
public class CompileProjectStep extends AbstractAntTargetGenerator {

	private Plugin project;
	private String sourceFileEncoding;
	private JDKVersion targetVersion;

	public CompileProjectStep(Plugin plugin, JDKVersion targetVersion, String sourceFileEncoding) {
		this.project = plugin;
		this.sourceFileEncoding = sourceFileEncoding;
		this.targetVersion = targetVersion;
	}

	/**
	 * Returns the ANT build instructions for a single plug-in projects. The 
	 * returned script compiles the content of all source folders and copies 
	 * all resource files to the bin folder.
	 */
	public Collection<AntTarget> generateAntTargets() {
		File[] sourceFolders = project.getSourceFolders();
		if (sourceFolders.length == 0 || !project.hasManifest()) {
			// nothing to do for projects without source folders or manifest
			return Collections.emptySet();
		}
		XMLContent content = new XMLContent();
		String projectName = project.getIdentifier();
		
		content.append("<!-- Compile instructions for plug-in project " + projectName + " -->");
		String binPath = new ClasspathHelper().getBinPath(project);
		content.append(NL);
		String target = "";
		if (targetVersion != null) {
			target = " target=\"" + targetVersion.getNumber() + "\"";
		}
		String javacTag = "<javac" + target + " destdir=\"" + binPath + "\" debug=\"on\" includeantruntime=\"false\"";
		if (sourceFileEncoding != null) {
			javacTag += " encoding=\"" + sourceFileEncoding + "\"";
		}
		javacTag += ">";
		content.append(javacTag);
		// add all source folders
		for (File sourceFolder : sourceFolders) {
			content.append("<src path=\"" + getSourceFolderPath(sourceFolder) + "\"/>");
		}
		content.append(NL);
		content.append("<classpath>");
		content.append(new ClasspathHelper().getClasspath(project, false));
		
		content.append("</classpath>");
		content.append("</javac>");
		content.append(NL);
		// copy resource files from source folder to binary folder
		for (File sourceFolder : sourceFolders) {
			content.append("<copy todir=\"" + binPath + "\">");
			content.append("<fileset dir=\"" + getSourceFolderPath(sourceFolder) + "\">");
			content.append("<exclude name=\"**/*.java\"/>");
			content.append("</fileset>");
			content.append("</copy>");
		}
		content.append(NL);
		return Collections.singleton(new AntTarget("compile-" + projectName, content));
	}

	private String getSourceFolderPath(File sourceFolder) {
		// TODO this does not work for source folders that are not directly contained in the project root directory
		return new File(project.getFile(), sourceFolder.getName()).getAbsolutePath();
	}
}
