/*******************************************************************************
 * Copyright (c) 2012
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   DevBoost GmbH - Berlin, Germany
 *	  - initial API and implementation
 ******************************************************************************/
package org.buildboost.hudson.plugins.boostscm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The BuildBoostSCM extends Jenkins with the ability to poll all repositories
 * that are referenced by a BuildBoost build. This includes the root repository
 * and all transitively referenced repositories.
 * 
 * To poll the repositories a list of all used repositories is created by the
 * BuildBoost CloneStep.
 */
public class BuildBoostSCM extends SCM {
	
	private static final String ROOT_REPOSITORIES_FILE = "repos/root.repositories";

	private static final String MAIN_BUILD_SCRIPT = "build.xml";

	private static final String UNIVERSAL_BUILD_SCRIPT_URL = "https://raw.github.com/DevBoost/BuildBoost/master/Universal/de.devboost.buildboost.universal.build/boost/build.xml";

	private static final Logger logger = Logger.getLogger(BuildBoostSCM.class.getName());

	private static final String GIT_CMD_WINDOWS = "git.cmd";
	private static final String GIT_CMD_OTHER = "git";
	
	private static final String REPOSITORY_FOLDER_NAME = "repos";
	private static final String BUILDBOOST_REVISIONS_FILE_NAME = "buildboost_repository_list.txt";
	
	private static final String URL_PREFIX = "BuildBoost-Repository-URL: ";
	private static final String TYPE_PREFIX = "BuildBoost-Repository-Type: ";
	private static final String LOCAL_PREFIX = "BuildBoost-Repository-Local: ";

	private static final String COMMIT_PREFIX = "commit ";
	private static final String SVN_REVISION_PREFIX = "Revision: ";
	private static final String SVN_REVISION_REGEX = "^r[0-9]+ .*";

	public static class DescriptorImpl extends SCMDescriptor<BuildBoostSCM> {

		public DescriptorImpl() {
			super(BuildBoostSCM.class, null);
			load();
		}

		@Override
		public String getDisplayName() {
			return "BuildBoostSCM";
		}
	}

	@Extension
	public final static DescriptorImpl descriptor = new DescriptorImpl();

	private String rootRepositoryURL;

	@Override
	public SCMDescriptor<?> getDescriptor() {
		return descriptor;
	}
	
	@DataBoundConstructor
	public BuildBoostSCM(String rootRepositoryURL) {
		super();
		this.rootRepositoryURL = rootRepositoryURL;
	}
	
	public String getRootRepositoryURL() {
		return rootRepositoryURL;
	}

	@Override
	public SCMRevisionState calcRevisionsFromBuild(
			AbstractBuild<?, ?> build,
			Launcher launcher, 
			TaskListener listener) 
			throws IOException, InterruptedException {
		logger.info("calcRevisionsFromBuild()");
		return getBuildState(build);
	}

	/**
	 * Creates the build state using the list of repositories. Each of the
	 * repository working copies is examined for its revision.
	 */
	private SCMRevisionState getBuildState(AbstractBuild<?, ?> build) 
			throws IOException, InterruptedException {
		
		BuildBoostRevisionState state = new BuildBoostRevisionState();
		List<BuildBoostRepository> repositories = getRepositories(build);
		for (BuildBoostRepository repository : repositories) {
			String revision = getLocalRevision(repository);
			state.addRepositoryState(repository, revision);
		}
		return state;
	}

	/**
	 * Determines the list of repositories the was created by the BuildBoost
	 * CloneStage.
	 */
	private List<BuildBoostRepository> getRepositories(AbstractBuild<?, ?> build) 
			throws IOException, InterruptedException {
		
		Map<String, BuildBoostRepository> repositories = new LinkedHashMap<String, BuildBoostRepository>();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FilePath workspace = build.getWorkspace();
		FilePath revisionsFile = workspace.child(REPOSITORY_FOLDER_NAME).child(BUILDBOOST_REVISIONS_FILE_NAME);
		
		if (revisionsFile.exists()) {
			revisionsFile.copyTo(baos);
			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
			try {
				String remoteURL = null;
				String type = null;
				String localPath = null;
				String line;
				while((line=br.readLine()) != null) {
					if (line.startsWith(TYPE_PREFIX)) {
						type = line.substring(TYPE_PREFIX.length());
						logger.info("found type");
					}
					if (line.startsWith(URL_PREFIX)) {
						remoteURL = line.substring(URL_PREFIX.length());
						logger.info("found currentURL");
					}
					if (line.startsWith(LOCAL_PREFIX) && type != null && remoteURL != null) {
						localPath = line.substring(LOCAL_PREFIX.length());
						logger.info("found localPath");
						repositories.put(remoteURL, new BuildBoostRepository(type, remoteURL, localPath));
					}
				}
			} finally {
				br.close();
			}
		} else {
			System.out.println("BuildBoostSCM.getRepositories() Can't find " + revisionsFile);
		}
		return new ArrayList<BuildBoostRepository>(repositories.values());
	}

	@Override
	protected PollingResult compareRemoteRevisionWith(
			AbstractProject<?, ?> project, 
			Launcher launcher,
			FilePath workspace, 
			TaskListener listener, 
			SCMRevisionState baseline)
			throws IOException, InterruptedException {
		
		logger.info("compareRemoteRevisionWith(" + baseline + ")");
		if (baseline instanceof BuildBoostRevisionState) {
			BuildBoostRevisionState state = (BuildBoostRevisionState) baseline;
			List<BuildBoostRepositoryState> states = state.getRepositoryStates();
			boolean foundUpdates = checkForUpdates(states);
			if (foundUpdates) {
				return new PollingResult(Change.SIGNIFICANT);
			}
		}
		return new PollingResult(Change.NONE);
	}

	/**
	 * Checks whether one of the given repositories is not up-to-date.
	 * 
	 * @param states the repositories to check
	 * @return true if an update is available, false if not
	 */
	private boolean checkForUpdates(List<BuildBoostRepositoryState> states) {
		logger.info("checkForUpdates(" + states + ")");
		boolean foundUpdate = false;
		for (BuildBoostRepositoryState state : states) {
			BuildBoostRepository repository = state.getRepository();
			String newRevision = getRemoteRevision(repository);
			String oldRevision = state.getRevision();

			logger.info("new revision for repository " + repository + " is " + newRevision);
			logger.info("old revision for repository " + repository + " is " + oldRevision);
			
			if (repository.isSvn()) {
				if (oldRevision == null && newRevision != null) {
					foundUpdate = true;
					continue;
				}
				if (oldRevision == null && newRevision == null) {
					continue;
				}
				if (oldRevision.startsWith("r")) {
					oldRevision = oldRevision.substring(1);
				}
				int newRev;
				try {
					newRev = Integer.parseInt(newRevision);
				} catch (NumberFormatException nfe) {
					logger.warning("Can't parse SVN revision '" + newRevision + "'");
					continue;
				}
				
				int oldRev;
				try {
					oldRev = Integer.parseInt(oldRevision);
				} catch (NumberFormatException nfe) {
					logger.warning("Can't parse SVN revision '" + oldRevision + "'");
					continue;
				}
				if (newRev > oldRev) {
					logger.info("found update for SVN repository " + repository + " (" + oldRevision + " => " + newRevision + ")");
					foundUpdate = true;
				}
			} else {
				if (newRevision != null && !newRevision.equals(oldRevision)) {
					logger.info("found update for repository " + repository + " (" + oldRevision + " => " + newRevision + ")");
					foundUpdate = true;
				}
			}
		}
		return foundUpdate;
	}

	/**
	 * Returns the revision of the working copy of the given repository.
	 */
	private String getLocalRevision(BuildBoostRepository repository) {
		if (repository.isGit()) {
			return getLocalGitRevision(repository.getLocalPath());
		} else if (repository.isSvn()) {
			return getLocalSvnRevision(repository.getLocalPath());
		} else if (repository.isDynamicFile()) {
			return getLocalMD5(repository.getLocalPath());
		} else {
			return null;
		}
	}

	private String getLocalMD5(String localPath) {
		FileInputStream fis;
		try {
			File directory = new File(localPath);
			fis = new FileInputStream(new File(directory, directory.getName() + ".MD5"));
			return getContent(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the hash code of the GIT working tree at 'localPath'.
	 */
	private String getLocalGitRevision(String localPath) {
		return exectureGitLog(localPath, false);
	}

	/**
	 * Returns the revision of the SVN working copy at 'localPath'.
	 */
	private String getLocalSvnRevision(String localPath) {
		return executeSvnInfo(localPath);
	}

	/**
	 * Returns the latest revision/hash code of the given repository.
	 */
	private String getRemoteRevision(BuildBoostRepository repository) {
		logger.info("getRemoteRevision(" + repository + ")");
		
		if (repository.isGit()) {
			return getRemoteGitRevision(repository.getLocalPath());
		} else if (repository.isSvn()) {
			return executeSvnLog(repository.getRemoteURL());
		} else if (repository.isDynamicFile()) {
			return getRemoteMD5(repository.getRemoteURL());
		} else {
			return null;
		}
	}

	private String getRemoteMD5(String remoteURL) {
		String md5URL = remoteURL + ".MD5";
		return getContent(md5URL);
	}

	public String getContent(String urlString) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			InputStream inputStream = conn.getInputStream();
			return getContent(inputStream);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getContent(InputStream inputStream) {
		try {
			StringBuilder result = new StringBuilder();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader rd = new BufferedReader(inputStreamReader);
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the hash code of the latest commit at the origin.
	 */
	private String getRemoteGitRevision(String localPath) {
		exectureGitFetch(localPath);
		return exectureGitLog(localPath, true);
	}

	/**
	 * Executes the Git fetch command to retrieve the changes from the origin
	 * this repository was cloned from.
	 */
	private void exectureGitFetch(String localPath) {
		List<String> command = new ArrayList<String>();
		command.add(getGitCommand());
		command.add("fetch");
		//command.add("--dry-run");
		
		executeNativeBinary(localPath, command, null);
	}

	/**
	 * Returns the name of the Git executable for the OS we're running on.
	 */
	private String getGitCommand() {
		if (System.getProperty("os.name").contains("Windows")) {
			return GIT_CMD_WINDOWS;
		} else {
			return GIT_CMD_OTHER;
		}
	}

	/**
	 * Executes the Git log command and returns the hash code of the last
	 * commit.
	 */
	private String exectureGitLog(String localPath, boolean useOrigin) {
		List<String> command = new ArrayList<String>();
		command.add(getGitCommand());
		command.add("log");
		command.add("-1");
		if (useOrigin) {
			command.add("origin");
		}
		
		final String[] revision = new String[1];
		executeNativeBinary(localPath, command, new IFunction<Boolean, String>() {
			
			public Boolean call(String line) {
				if (line.startsWith(COMMIT_PREFIX)) {
					revision[0] = line.substring(COMMIT_PREFIX.length());
					return false;
				}
				return true;
			}
		});
		return revision[0];
	}

	/**
	 * Executes the SVN info command and returns the revision of the working
	 * copy.
	 */
	private String executeSvnInfo(String localPath) {
		List<String> command = new ArrayList<String>();
		command.add("svn");
		command.add("info");
		
		final String[] revision = new String[1];
		executeNativeBinary(localPath, command, new IFunction<Boolean, String>() {
			
			public Boolean call(String line) {
				if (line.contains(SVN_REVISION_PREFIX)) {
					revision[0] = line.substring(line.indexOf(SVN_REVISION_PREFIX) + SVN_REVISION_PREFIX.length());
					return false;
				}
				return true;
			}
		});
		return revision[0];
	}

	/**
	 * Executes the SVN log command and returns the revision of the last commit.
	 */
	private String executeSvnLog(String remotePath) {
		List<String> command = new ArrayList<String>();
		command.add("svn");
		command.add("log");
		command.add("--limit");
		command.add("1");
		command.add(remotePath);
		
		final String[] revision = new String[1];
		executeNativeBinary(null, command, new IFunction<Boolean, String>() {
			
			public Boolean call(String line) {
				if (line.matches(SVN_REVISION_REGEX)) {
					revision[0] = line.substring(0, line.indexOf(" "));
					if (revision[0].startsWith("r")) {
						revision[0] = revision[0].substring(1);
					}
					return false;
				}
				return true;
			}
		});
		return revision[0];
	}

	private void executeNativeBinary(String localPath, 
			List<String> command, IFunction<Boolean, String> readerCallback) {
		ProcessBuilder pb = new ProcessBuilder(command);
		if (localPath != null) {
			pb.directory(new File(localPath));
		}
		Process process = null;
		try {
			logger.info("executing " + command + " in " + localPath);
			process = pb.start();
			InputStream inputStream = process.getInputStream();
			BufferedReader isr = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while((line = isr.readLine()) != null) {
				logger.info("output: " + line);
				if (readerCallback == null) {
					continue;
				}
				boolean continueReading = readerCallback.call(line);
				if (!continueReading) {
					break;
				}
			}
			int exitCode = process.waitFor();
			logger.info("exitCode " + exitCode);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeStreams(process);
		}
	}
	
    private void closeStreams(Process process) {
    	if (process == null) {
			return;
		}
    	closeStream(process.getInputStream());
    	closeStream(process.getOutputStream());
    	closeStream(process.getErrorStream());
    }
    
    private void closeStream(Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * This method does very little as the actual check out of projects is 
	 * performed by BuildBoost during the CloneStage. 
	 * 
	 * We simply write the URL of the root repository to a file in the workspace 
	 * to allow the universal build script to use it. Also, we download the
	 * latest version of the universal build script.
	 */
	@Override
	public boolean checkout(
			AbstractBuild<?, ?> build, 
			Launcher launcher,
			FilePath workspace, 
			BuildListener listener, 
			File changelogFile)
			throws IOException, InterruptedException {
		
		logger.info("checkout()");
		
		if (rootRepositoryURL != null) {
			FilePath rootRepositoryFile = workspace.child(ROOT_REPOSITORIES_FILE);
			// put multiple repositories into multiple lines
			rootRepositoryURL = rootRepositoryURL.replace(",", System.getProperty("line.separator"));
			rootRepositoryFile.write(rootRepositoryURL, "UTF-8");
		}
		
		downloadUniversalBuildScript(workspace);
        
		return true;
	}

	private void downloadUniversalBuildScript(FilePath workspace) throws IOException, InterruptedException {
		Exception e = null;
		StringBuilder scriptContent = new StringBuilder();
		try {
			URL url;
			url = new URL(UNIVERSAL_BUILD_SCRIPT_URL);
			InputStream stream = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	        String inputLine;
	        while ((inputLine = reader.readLine()) != null) {
	            scriptContent.append(inputLine);
	        }
	        reader.close();
	        FilePath mainBuildFile = workspace.child(MAIN_BUILD_SCRIPT);
	        mainBuildFile.write(scriptContent.toString(), "UTF-8");
		} catch (MalformedURLException mue) {
			// ignore
			e = mue;
		} catch (IOException ioe) {
			// ignore
			e = ioe;
		} catch (InterruptedException ie) {
			// ignore
			e = ie;
		}

        FilePath mainBuildFile = workspace.child(MAIN_BUILD_SCRIPT);
        if (!mainBuildFile.exists()) {
			throw new IOException("Could not download universal build script: " + e.getMessage());
		}
	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		logger.info("createChangeLogParser()");
		// TODO implement this
		return null;
	}
}
