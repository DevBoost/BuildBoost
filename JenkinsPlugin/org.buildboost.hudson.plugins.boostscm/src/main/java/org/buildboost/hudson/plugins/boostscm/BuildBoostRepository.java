package org.buildboost.hudson.plugins.boostscm;

public class BuildBoostRepository {
	
	private String remoteURL;
	private String localPath;
	private String type;
	
	public BuildBoostRepository(String type, String remoteURL, String localPath) {
		super();
		System.out.println("BuildBoostRepository() type = " + type);
		System.out.println("BuildBoostRepository() remoteURL = " + remoteURL);
		System.out.println("BuildBoostRepository() localPath = " + localPath);
		this.type = type;
		this.remoteURL = remoteURL;
		this.localPath = localPath;
	}
	
	public String getRemoteURL() {
		return remoteURL;
	}
	
	public String getLocalPath() {
		return localPath;
	}
	
	public boolean isGit() {
		return "git".equals(type);
	}
	
	public boolean isSvn() {
		return "svn".equals(type);
	}
}
