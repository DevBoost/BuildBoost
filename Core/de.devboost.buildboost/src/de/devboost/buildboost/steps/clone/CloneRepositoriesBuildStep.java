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
		localRepositoryFolderName = localRepositoryFolderName.replace(" ", "-");
		
		String rootName = location.substring(location.lastIndexOf("/") + 1);

		File localRepo = new File(new File(reposFolder, localRepositoryFolderName), rootName);
		
		XMLContent content = new XMLContent();
		
		content.append("<condition property=\"git-executable\" value=\"git.cmd\">");
		content.append("<os family=\"windows\"/>");
		content.append("</condition>");
		content.append("<condition property=\"git-executable\" value=\"git\">");
		content.append("<not>");
		content.append("<os family=\"windows\"/>");
		content.append("</not>");
		content.append("</condition>");
		
		//TODO improve this check
		boolean isGit = location.contains("git");
		
		if (isGit) {
			if (localRepo.exists()) {
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepo.getAbsolutePath() + "\">");
				content.append("<arg value=\"pull\"/>");
			} else {
				content.append("<exec executable=\"${git-executable}\" dir=\"" + reposFolder.getAbsolutePath() + "\">");
				content.append("<arg value=\"clone\"/>");
				content.append("<arg value=\"" + location + "\"/>");
				content.append("<arg value=\"" + localRepo.getAbsolutePath() + "\"/>");
			}
		} else {
			if (localRepo.exists()) {
				content.append("<exec executable=\"svn\" dir=\"" + localRepo.getAbsolutePath() + "\">");
				content.append("<arg value=\"update\"/>");
			} else {
				content.append("<exec executable=\"svn\" dir=\"" + reposFolder + "\">");
				content.append("<arg value=\"co\"/>");
				content.append("<arg value=\"" + location + "\"/>");
				content.append("<arg value=\"" + localRepo.getAbsolutePath() + "\"/>");
			}
		}
		content.append("</exec>");
		
		return Collections.singleton(new AntTarget("update-" + localRepositoryFolderName, content));
	}
}
