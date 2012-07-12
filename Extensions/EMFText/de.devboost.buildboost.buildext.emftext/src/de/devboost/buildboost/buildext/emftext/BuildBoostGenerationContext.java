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
package de.devboost.buildboost.buildext.emftext;

import java.io.File;

import org.emftext.sdk.codegen.IFileSystemConnector;
import org.emftext.sdk.codegen.resource.GenerationContext;
import org.emftext.sdk.concretesyntax.ConcreteSyntax;

public class BuildBoostGenerationContext extends GenerationContext {

	private String projectName;
	private File rootFolder;
	private String pathToCsFile;

	public BuildBoostGenerationContext(
			IFileSystemConnector folderConnector,
			BuildBoostProblemCollector problemCollector,
			ConcreteSyntax syntax, 
			File rootFolder, 
			String pathToCsFile,
			String projectName) {

		super(folderConnector, problemCollector, syntax);
		this.projectName = projectName;
		this.rootFolder = rootFolder;
		this.pathToCsFile = pathToCsFile;
	}

	@Override
	public String getSyntaxProjectName() {
		return projectName;
	}

	@Override
	public String getProjectRelativePathToSyntaxFile() {
		String rootPath = rootFolder.getAbsolutePath();
		String relativePath = pathToCsFile.substring(rootPath.length());
		return relativePath;
	}

	@Override
	public boolean getGenerateANTLRPlugin() {
		return true;
	}
}
