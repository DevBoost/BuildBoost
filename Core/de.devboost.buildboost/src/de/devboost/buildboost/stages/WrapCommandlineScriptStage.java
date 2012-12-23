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
 * The {@link WrapCommandlineScriptStage} can be used to integrate existing
 * command line script into a build process. The stage produces a Ant target
 * that calls the given script.
 */
public class WrapCommandlineScriptStage extends AbstractBuildStage {

	private File workingDir;
	private String executable;
	
	/**
	 * Creates a new {@link WrapCommandlineScriptStage}.
	 * 
	 * @param workingDir the working directory for the command line script
	 * @param executable the path to the executable (using absolute paths is 
	 *        recommended here, because this parameter is independent of the 
	 *        'workingDir' parameter)
	 */
	public WrapCommandlineScriptStage(File workingDir, String executable) {
		super();
		this.workingDir = workingDir;
		this.executable = executable;
	}

	@Override
	public AntScript getScript() throws BuildException {
		XMLContent content = new XMLContent();
		content.append("<exec executable=\"" + executable + "\" dir=\"" + workingDir.getAbsolutePath() + "\" failonerror=\"true\" />");
		
		AntScript script = new AntScript();
		AntTarget target = new AntTarget("execute-cmd-line-script", content);
		script.addTarget(target);
		return script;
	}
}
