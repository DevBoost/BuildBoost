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
package de.devboost.buildboost.stages;

import java.io.File;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link WrapAntScriptStage} can be used to integrate existing Ant scripts
 * into a build process. The stage produces a Ant target that calls the given 
 * Ant script.
 */
public class WrapAntScriptStage extends AbstractBuildStage {

	private File directory;
	private File antFile;
	private String target;
	
	/**
	 * Create a new {@link WrapAntScriptStage}.
	 * 
	 * @param directory the working directory for the Ant script
	 * @param antFile the script to call (using absolute files is recommended
	 *        here, because this parameter is independent of the 'directory'
	 *        parameter)
	 */
	public WrapAntScriptStage(File directory, File antFile) {
		this(directory, antFile, null);
	}

	public WrapAntScriptStage(File directory, File antFile, String target) {
		super();
		this.directory = directory;
		this.antFile = antFile;
		this.target = target;
	}

	@Override
	public AntScript getScript() throws BuildException {
		XMLContent content = new XMLContent();
		String targetAttribute = "";
		if (this.target != null) {
			targetAttribute = " target=\"" + this.target + "\"";
		}
		content.append("<ant antfile=\"" + antFile.getAbsolutePath() + "\"" + targetAttribute + " dir=\"" + directory.getAbsolutePath() + "\"/>");
		
		AntScript script = new AntScript();
		AntTarget target = new AntTarget("execute-ant-script", content);
		script.addTarget(target);
		return script;
	}

}
