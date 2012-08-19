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
 *      - initial API and implementation
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.kohsuke.stapler.DataBoundConstructor;

public class BuildBoostSCM extends SCM {
	
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
	private static final String SVN_REVISION_REGEX = "^r[0-9]+ ";

	public static class DescriptorImpl extends SCMDescriptor<BuildBoostSCM> {

		public DescriptorImpl() {
			super(BuildBoostSCM.class, null);
			load();
		}

		@Override
		public String getDisplayName() {
		    return "BuildBoostDisplayName";
		}
	}

    @Extension
    public final static DescriptorImpl descriptor = new DescriptorImpl();

    @Override
    public SCMDescriptor<?> getDescriptor() {
    	return descriptor;
    }
    
    @DataBoundConstructor
    public BuildBoostSCM() {
        super();
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

	private boolean checkForUpdates(List<BuildBoostRepositoryState> states) {
		logger.info("checkForUpdates(" + states + ")");
		boolean foundUpdate = false;
		for (BuildBoostRepositoryState state : states) {
			BuildBoostRepository repository = state.getRepository();
			String newRevision = getRemoteRevision(repository);
			String oldRevision = state.getRevision();

			logger.info("new revision for repository " + repository + " is " + newRevision);
			logger.info("old revision for repository " + repository + " is " + oldRevision);
			
			if (newRevision != null && !newRevision.equals(oldRevision)) {
				foundUpdate = true;
			}
		}
		return foundUpdate;
	}

	private String getLocalRevision(BuildBoostRepository repository) {
		if (repository.isGit()) {
			return getLocalGitRevision(repository.getLocalPath());
		} else if (repository.isSvn()) {
			return getLocalSvnRevision(repository.getLocalPath());
		} else {
			return null;
		}
	}

	private String getLocalGitRevision(String localPath) {
		return exectureGitLog(localPath, false);
	}

	private String getLocalSvnRevision(String localPath) {
		return executeSvnInfo(localPath);
	}

	private String getRemoteRevision(BuildBoostRepository repository) {
		logger.info("getRemoteRevision(" + repository + ")");
		
		if (repository.isGit()) {
			return getRemoteGitRevision(repository.getLocalPath());
		} else if (repository.isSvn()) {
			return executeSvnLog(repository.getRemoteURL());
		} else {
			return null;
		}
	}

	private String getRemoteGitRevision(String localPath) {
		exectureGitFetch(localPath);
		return exectureGitLog(localPath, true);
	}

	private void exectureGitFetch(String localPath) {
		List<String> command = new ArrayList<String>();
		command.add(getGitCommand());
		command.add("fetch");
		//command.add("--dry-run");
		
		executeNativeBinary(localPath, command, null);
	}

	private String getGitCommand() {
		if (System.getProperty("os.name").contains("Windows")) {
			return GIT_CMD_WINDOWS;
		} else {
			return GIT_CMD_OTHER;
		}
	}

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

	private String executeSvnInfo(String localPath) {
		List<String> command = new ArrayList<String>();
		command.add("svn");
		command.add("info");
		
		final String[] revision = new String[1];
		executeNativeBinary(null, command, new IFunction<Boolean, String>() {
			
			public Boolean call(String line) {
            	if (line.startsWith(SVN_REVISION_PREFIX)) {
            		revision[0] = line.substring(0, line.indexOf(" "));
					return false;
				}
				return true;
			}
		});
		return revision[0];
	}

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
		try {
			logger.info("executing " + command + " in " + localPath);
			Process process = pb.start();
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
		}
	}
	
	@Override
	public boolean checkout(
			AbstractBuild<?, ?> build, 
			Launcher launcher,
			FilePath workspace, 
			BuildListener listener, 
			File changelogFile)
			throws IOException, InterruptedException {
		logger.info("checkout()");
		return true;
	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		logger.info("createChangeLogParser()");
		return null;
	}
}
