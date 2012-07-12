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
package de.devboost.buildboost.steps.clone;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link CloneRepositoriesBuildStep} generates a script that copies plug-in
 * projects from one directory (typically a SVN working copy) to another
 * directory (typically a directory where the actual build is performed). The
 * {@link CloneRepositoriesBuildStep} uses synchronization instead of pure copying
 * to avoid unnecessary copy operations.
 */
public class CloneRepositoriesBuildStep extends AbstractAntTargetGenerator {

	private File reposFolder;
	private String location;

	public CloneRepositoriesBuildStep(File reposFolder, String location) {
		super();
		this.reposFolder = reposFolder;
		this.location = location;
	}

	public Collection<AntTarget> generateAntTargets() {
		String localRepositoryFolderName = location.substring(location.indexOf("//") + 2);
		localRepositoryFolderName = localRepositoryFolderName.replace("/", "_");
		localRepositoryFolderName = localRepositoryFolderName.replace(".", "-");
		File localRepo = new File(reposFolder, localRepositoryFolderName);

		XMLContent content = new XMLContent();
		content.append("<echo message=\"TODO CLONE/UPDATE/PULL/DOWNLOAD from " + location + " to " + localRepo + "\"/>");
		content.append("<mkdir dir=\"" + localRepo + "\"/>");
		return Collections.singleton(new AntTarget("update-" + localRepositoryFolderName, content));
	}
}
