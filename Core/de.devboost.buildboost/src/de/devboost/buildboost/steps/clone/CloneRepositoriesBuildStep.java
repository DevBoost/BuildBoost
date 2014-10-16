/*******************************************************************************
 * Copyright (c) 2006-2014
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.RepositoriesFile;
import de.devboost.buildboost.artifacts.RepositoriesFile.Location;
import de.devboost.buildboost.util.AntScriptUtil;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link CloneRepositoriesBuildStep} generates Ant tasks to clone/check 
 * out/update/pull/download repositories.
 */
public class CloneRepositoriesBuildStep extends AbstractAntTargetGenerator {

	private static final String CREDENTIALS_REGEX = "\\{([a-z|A-Z|_]+)\\}:\\{([a-z|A-Z|_]+)\\}@";
	private static final Pattern CREDENTIALS_PATTERN = Pattern.compile(CREDENTIALS_REGEX);

	private File reposFolder;
	private Collection<Location> locations;

	public CloneRepositoriesBuildStep(File reposFolder, Collection<Location> locations) {
		super();
		this.reposFolder = reposFolder;
		this.locations = locations;
	}

	public Collection<AntTarget> generateAntTargets() {
		Collection<AntTarget> result = new ArrayList<AntTarget>();
		result.add(writeRepositoryList());
		
		for (Location location : locations) {
			generateCloneLocationTasks(result, location);
		}
		return result;
	}

	private void generateCloneLocationTasks(Collection<AntTarget> result,
			Location location) {
		
		String locationURL = location.getUrl();
		String localRepositoryFolderName = url2FolderName(locationURL);
		
		String rootName = url2FolderName(locationURL.substring(locationURL.lastIndexOf("/") + 1));

		File localRepo = new File(new File(reposFolder, localRepositoryFolderName), rootName);
		
		XMLContent content = new XMLContent();
		
		content.append("<property environment=\"env\"/>");
		content.appendLineBreak();	

		content.append("<condition property=\"git-executable\" value=\"git.cmd\">");
		content.append("<os family=\"windows\"/>");
		content.append("</condition>");
		content.append("<condition property=\"git-executable\" value=\"git\">");
		content.append("<not>");
		content.append("<os family=\"windows\"/>");
		content.append("</not>");
		content.append("</condition>");
		content.appendLineBreak();	
			
		String locationType = location.getType();
		
		boolean isGit = locationType.equals(RepositoriesFile.GIT);
		boolean isSVN = locationType.equals(RepositoriesFile.SVN);
		boolean isDynamicFile = locationType.equals(RepositoriesFile.DYNAMICFILE);
		
		String localRepositoryPath = getLocalRepositoryPath(location);
		
		if (isGit) {
			addCloneGitRepositoryTasks(location, locationURL, localRepo,
					content, localRepositoryPath);
		} else if (isSVN) {
			addCloneSVNTasks(locationURL, localRepo, content,
					localRepositoryPath);
		} else if (isDynamicFile) {
			addCloneDynamicFileTask(locationURL, content, localRepositoryPath);
		} else /* isGet */ {
			addCloneOtherTasks(location, locationURL, rootName, localRepo,
					content, localRepositoryPath, true);
		}
		
		String taskName = "update-" + localRepositoryFolderName;
		String propertyName = "performed-" + taskName;
		content.append("<echo message=\"Setting property " + propertyName + "\"/>");
		content.append("<property name=\"" + propertyName + "\" value=\"true\"/>");
		AntTarget cloneTarget = new AntTarget(taskName, content);
		cloneTarget.setUnlessConditions(propertyName);
		result.add(cloneTarget);
	}

	private void addCloneDynamicFileTask(String locationURL,
			XMLContent content, String localRepositoryPath) {
		
		content.append("<mkdir dir=\""+ localRepositoryPath + "\"/>");
		AntScriptUtil.addDownloadFileScript(content, locationURL, localRepositoryPath);
		AntScriptUtil.addDownloadFileScript(content, locationURL + ".MD5", localRepositoryPath);
	}

	private void addCloneOtherTasks(Location location, String locationURL,
			String rootName, File localRepo, XMLContent content,
			String localRepositoryPath, boolean skipIfRepositoryExists) {
		
		if (skipIfRepositoryExists) {
			// TODO Don't do this here. Add condition to Ant task instead.
			if (localRepo.exists()) {
				return;
			}
		}
		
		content.append("<mkdir dir=\""+ localRepositoryPath + "\"/>");
		if (!location.getSubDirectories().isEmpty()) {
			if (localRepo.getName().endsWith(".zip")) {
				String zipFilePath = new File(localRepo, rootName).getAbsolutePath();
				content.append("<get src=\""+ locationURL + "\" dest=\""+ localRepositoryPath + "\"/>");
				content.append("<unzip src=\"" + zipFilePath + "\" dest=\"" +  localRepositoryPath + "\">");
				content.append("<patternset>");
				for (String zipEntry : location.getSubDirectories()) {
					content.append("<include name=\"" + zipEntry + "\"/>");
				}
				content.append("</patternset>");
				content.append("</unzip>");
				content.append("<delete file=\"" + zipFilePath + "\"/>");
			} else /* folder */ {
				for (String subPath : location.getSubDirectories()) {
					content.append("<get src=\""+ locationURL + "/" + subPath + "\" dest=\""+ localRepositoryPath + "/" + subPath + "\"/>");
				}	
			}
		} else {
			AntScriptUtil.addDownloadFileScript(content, locationURL, localRepositoryPath);
		}
	}

	/**
	 * Adds Ant tasks to check out or update an SVN repository.
	 */
	private void addCloneSVNTasks(String locationURL, File localRepo,
			XMLContent content, String localRepositoryPath) {
		
		String locationURLWithoutCredentials = removeCredentialPlaceholders(locationURL);
		if (localRepo.exists()) {
			// execute update
			content.append("<exec executable=\"svn\" dir=\"" + localRepositoryPath + "\" failonerror=\"false\">");
			content.append("<arg value=\"update\"/>");
			content.append("</exec>");
		} else {
			// execute checkout
			content.append("<exec executable=\"svn\" dir=\"" + reposFolder + "\" failonerror=\"false\">");
			content.append("<arg value=\"co\"/>");
			content.append("<arg value=\"" + locationURLWithoutCredentials + "\"/>");
			content.append("<arg value=\"" + localRepositoryPath + "\"/>");
			if (containsCredentialPlaceholders(locationURL)) {
				String usernameVar = getUsernameVar(locationURL);
				String passwordVar = getPasswordVar(locationURL);
				content.append("<arg value=\"--username\"/>");
				content.append("<arg value=\"${env." + usernameVar + "}\"/>");
				content.append("<arg value=\"--password\"/>");
				content.append("<arg value=\"${env." + passwordVar + "}\"/>");
			}
			content.append("</exec>");
		}
	}

	/**
	 * Adds Ant tasks to clone or pull a GIT repository.
	 */
	private void addCloneGitRepositoryTasks(Location location,
			String locationURL, File localRepo, XMLContent content,
			String localRepositoryPath) {
		if (localRepo.exists()) {
			content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" failonerror=\"false\">");
			content.append("<arg value=\"pull\"/>");
			content.append("</exec>");
		} else {
			content.append("<mkdir dir=\"" + localRepositoryPath + "\">");
			content.append("</mkdir>");
			content.append("<echo message=\"Git executable ${git-executable}\"/>");
			content.append("<echo message=\"LocationURL " + locationURL + "\"/>");
			content.append("<echo message=\"LocalRepositoryPath " + localRepositoryPath + "\"/>");
			content.append("<exec executable=\"${git-executable}\" dir=\"" + reposFolder.getAbsolutePath() + "\" failonerror=\"false\">");
			content.append("<arg value=\"clone\"/>");
			content.append("<arg value=\"" + locationURL + "\"/>");
			content.append("<arg value=\"" + localRepositoryPath + "\"/>");
			content.append("</exec>");
		}
		if (!location.getSubDirectories().isEmpty()) {
			//enable sparse checkout
			content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" failonerror=\"false\">");
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
			content.append("<exec executable=\"${git-executable}\" dir=\"" + localRepositoryPath + "\" failonerror=\"false\">");
			content.append("<arg value=\"read-tree\"/>");
			content.append("<arg value=\"-mu\"/>");
			content.append("<arg value=\"HEAD\"/>");
			content.append("</exec>");
		}
	}

	private AntTarget writeRepositoryList() {
		XMLContent content = new XMLContent();
		File repositoryListFile = new File(reposFolder, "buildboost_repository_list.txt");
		String repositoryListPath = repositoryListFile.getAbsolutePath();
		content.append("<echo message=\"\" file=\"" + repositoryListPath + "\" append=\"false\">");
		content.append("</echo>");
		for (Location location : locations) {
			String locationType = location.getType();
			
			boolean isGit = locationType.equals("git");
			boolean isSVN = locationType.equals("svn");
			boolean isDynamicFile = locationType.equals("dynamicfile");
			
			String locationURL = location.getUrl();
			
			String localRepositoryPath = getLocalRepositoryPath(location); 
			
			if (isSVN || isGit || isDynamicFile) {
				content.append("<echo message=\"BuildBoost-Repository-Type: " + locationType + "\" file=\"" + repositoryListPath + "\" append=\"true\">");
				content.append("</echo>");
				content.append("<echo message=\"BuildBoost-Repository-URL: " + locationURL + "\" file=\"" + repositoryListPath + "\" append=\"true\">");
				content.append("</echo>");
				content.append("<echo message=\"BuildBoost-Repository-Local: " + localRepositoryPath + "\" file=\"" + repositoryListPath + "\" append=\"true\">");
				content.append("</echo>");
			}
		}
		
		return new AntTarget("write-repository-list", content);
	}

	protected String getLocalRepositoryPath(Location location) {
		String locationURL = location.getUrl();
		String localRepositoryFolderName = url2FolderName(locationURL);
		String rootName = url2FolderName(locationURL.substring(locationURL.lastIndexOf("/") + 1));
		return new File(new File(reposFolder, localRepositoryFolderName), rootName).getAbsolutePath();
	}

	/**
	 * Converts the given URL to a folder names. This is used to compute the
	 * names for the folder where to checkout the repository with the given URL 
	 * to.
	 * 
	 * @param url a URL of a repository
	 * @return a folder name
	 */
	protected String url2FolderName(String url) {
		int idx;
		String folderName = url;
		
		// Cut leading protocol
		idx = folderName.indexOf("//");
		if (idx != -1) {
			folderName = folderName.substring(idx + 2);
		}
		
		// Cut arguments
		idx = folderName.indexOf("?");
		if (idx != -1) {
			folderName = folderName.substring(0, idx);
		}
		
		// Remove credential place holders
		folderName = removeCredentialPlaceholders(folderName);
		
		// Replace special character that are not allows in folder names
		folderName = folderName.replace(":", "");
		folderName = folderName.replace("~", "_");
		folderName = folderName.replace("@", "_");
		folderName = folderName.replace("/", "_");
		folderName = folderName.replace("\\", "_");
		folderName = folderName.replace(" ", "-");
		return folderName;
	}

	protected String removeCredentialPlaceholders(String path) {
		return path.replaceAll(CREDENTIALS_REGEX, "");
	}

	protected boolean containsCredentialPlaceholders(String path) {
		return !path.equals(removeCredentialPlaceholders(path));
	}

	protected String getUsernameVar(String path) {
		return getCredentialVar(path, 1);
	}

	protected String getPasswordVar(String path) {
		return getCredentialVar(path, 2);
	}
	
	private String getCredentialVar(String path, int group) {
		Matcher matcher = CREDENTIALS_PATTERN.matcher(path);
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}
}
