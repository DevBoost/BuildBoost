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
import de.devboost.buildboost.artifacts.BoostFile.Location;
import de.devboost.buildboost.util.XMLContent;


public class CloneRepositoriesBuildStep extends AbstractAntTargetGenerator {

	private File reposFolder;
	private Location location;

	public CloneRepositoriesBuildStep(File reposFolder, Location location) {
		super();
		this.reposFolder = reposFolder;
		this.location = location;
	}

	public Collection<AntTarget> generateAntTargets() {
		String localRepositoryFolderName = location.getUrl().substring(
				location.getUrl().indexOf("//") + 2);
		localRepositoryFolderName = localRepositoryFolderName.replace("/", "_");
		localRepositoryFolderName = localRepositoryFolderName.replace(".", "-");
		localRepositoryFolderName = localRepositoryFolderName.replace(" ", "-");
		
		String rootName = location.getUrl().substring(location.getUrl().lastIndexOf("/") + 1);

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
		
		boolean isGit = location.getType().equals("git");
		
		if (isGit) {
			if (localRepo.exists()) {
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepo.getAbsolutePath() + "\">");
				content.append("<arg value=\"pull\"/>");
				content.append("</exec>");
			} else {
				content.append("<exec executable=\"${git-executable}\" dir=\"" + reposFolder.getAbsolutePath() + "\">");
				content.append("<arg value=\"clone\"/>");
				content.append("<arg value=\"" + location.getUrl() + "\"/>");
				content.append("<arg value=\"" + localRepo.getAbsolutePath() + "\"/>");
				content.append("</exec>");
			}
			if (!location.getSubDirectories().isEmpty()) {
				//enable sparse checkout
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepo.getAbsolutePath() + "\">");
				content.append("<arg value=\"config\"/>");
				content.append("<arg value=\"core.sparsecheckout\"/>");
				content.append("<arg value=\"true\"/>");
				content.append("</exec>");
				String dirList = "";
				for (String subDir : location.getSubDirectories()) {
					dirList += subDir;
					dirList += "${line.separator}";
				}
				content.append("<echo message=\"" + dirList + "\" file=\"" + localRepo.getAbsolutePath() + "/.git/info/sparse-checkout\"/>");
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepo.getAbsolutePath() + "\">");
				content.append("<arg value=\"read-tree\"/>");
				content.append("<arg value=\"-mu\"/>");
				content.append("<arg value=\"HEAD\"/>");
				content.append("</exec>");
			}
		} else {
			if (localRepo.exists()) {
				content.append("<exec executable=\"svn\" dir=\"" + localRepo.getAbsolutePath() + "\">");
				content.append("<arg value=\"update\"/>");
				content.append("</exec>");
			} else {
				content.append("<exec executable=\"svn\" dir=\"" + reposFolder + "\">");
				content.append("<arg value=\"co\"/>");
				content.append("<arg value=\"" + location.getUrl() + "\"/>");
				content.append("<arg value=\"" + localRepo.getAbsolutePath() + "\"/>");
				content.append("</exec>");
			}
		}
		
		return Collections.singleton(new AntTarget("update-" + localRepositoryFolderName, content));
	}
}
