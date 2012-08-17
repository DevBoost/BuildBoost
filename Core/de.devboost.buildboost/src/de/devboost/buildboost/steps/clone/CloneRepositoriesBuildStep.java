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
import de.devboost.buildboost.artifacts.RepositoriesFile.Location;
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
		String localRepositoryFolderName = url2FolderName(location.getUrl());
		
		String rootName = url2FolderName(location.getUrl().substring(location.getUrl().lastIndexOf("/") + 1));

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
		boolean isSVN = location.getType().equals("svn");
		
		String localRepositoryPath = localRepo.getAbsolutePath();
		String revisionFile = localRepositoryPath + "/buildboost_revision.txt";
		if (isGit) {
			if (localRepo.exists()) {
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" failonerror=\"true\">");
				content.append("<arg value=\"pull\"/>");
				content.append("</exec>");
			} else {
				content.append("<exec executable=\"${git-executable}\" dir=\"" + reposFolder.getAbsolutePath() + "\" failonerror=\"true\">");
				content.append("<arg value=\"clone\"/>");
				content.append("<arg value=\"" + location.getUrl() + "\"/>");
				content.append("<arg value=\"" + localRepositoryPath + "\"/>");
				content.append("</exec>");
			}
			if (!location.getSubDirectories().isEmpty()) {
				//enable sparse checkout
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" failonerror=\"true\">");
				content.append("<arg value=\"config\"/>");
				content.append("<arg value=\"core.sparsecheckout\"/>");
				content.append("<arg value=\"true\"/>");
				content.append("</exec>");
				String dirList = ".gitignore${line.separator}";
				for (String subDir : location.getSubDirectories()) {
					dirList += subDir;
					dirList += "${line.separator}";
				}
				content.append("<echo message=\"" + dirList + "\" file=\"" + localRepositoryPath + "/.git/info/sparse-checkout\"/>");
				content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" failonerror=\"true\">");
				content.append("<arg value=\"read-tree\"/>");
				content.append("<arg value=\"-mu\"/>");
				content.append("<arg value=\"HEAD\"/>");
				content.append("</exec>");
			}
			content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" output=\"" + revisionFile + "\" failonerror=\"true\">");
			content.append("<arg value=\"log\"/>");
			content.append("<arg value=\"-1\"/>");
			content.append("</exec>");
		} else if (isSVN) {
			// execute log command to remember revision (used for build polling in Jenkins)
			content.append("<exec executable=\"svn\" dir=\"" + localRepositoryPath + "\" output=\"" + revisionFile + "\" failonerror=\"true\">");
			content.append("<arg value=\"log\"/>");
			content.append("<arg value=\"--limit\"/>");
			content.append("<arg value=\"1\"/>");
			content.append("<arg value=\"" + location.getUrl() + "\"/>");
			content.append("</exec>");
			if (localRepo.exists()) {
				// execute update
				content.append("<exec executable=\"svn\" dir=\"" + localRepositoryPath + "\" failonerror=\"true\">");
				content.append("<arg value=\"update\"/>");
				content.append("</exec>");
			} else {
				// execute checkout
				content.append("<exec executable=\"svn\" dir=\"" + reposFolder + "\" failonerror=\"true\">");
				content.append("<arg value=\"co\"/>");
				content.append("<arg value=\"" + location.getUrl() + "\"/>");
				content.append("<arg value=\"" + localRepositoryPath + "\"/>");
				content.append("</exec>");
			}
		} else /* isGet */ {
			if (!localRepo.exists()) {
				content.append("<mkdir dir=\""+ localRepositoryPath + "\"/>");
				content.append("<get src=\""+ location.getUrl() + "\" dest=\""+ localRepositoryPath + "\"/>");
				if (localRepo.getName().endsWith(".zip") && !location.getSubDirectories().isEmpty()) {
					String zipFilePath = new File(localRepo, rootName).getAbsolutePath()  ;
					content.append("<unzip src=\"" + zipFilePath + "\" dest=\"" +  localRepositoryPath + "\">");
					content.append("<patternset>");
					for (String zipEntry : location.getSubDirectories()) {
						content.append("<include name=\"" + zipEntry + "\"/>");
					}
					content.append("</patternset>");
					content.append("</unzip>");
					content.append("<delete file=\"" + zipFilePath + "\"/>");
				}
			}
		}
		
		return Collections.singleton(new AntTarget("update-" + localRepositoryFolderName, content));
	}

	protected String url2FolderName(String url) {
		int idx;
		String folderName = url;
		//cut leading protocol
		idx = folderName.indexOf("//");
		if (idx != -1) {
			folderName = folderName.substring(idx + 2);
		}
		//cut arguments
		idx = folderName.indexOf("?");
		if (idx != -1) {
			folderName = folderName.substring(0, idx);
		}
		folderName = folderName.replace("/", "_");
		folderName = folderName.replace(" ", "-");
		return folderName;
	}
}
