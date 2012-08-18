package org.buildboost.hudson.plugins.boostscm;

public class BuildBoostRepositoryState {
	
	private BuildBoostRepository repository;
	private String revision;
	
	public BuildBoostRepositoryState(BuildBoostRepository repository, String revision) {
		super();
		System.out.println("BuildBoostRepositoryState() repository = " + repository);
		System.out.println("BuildBoostRepositoryState() revision = " + revision);
		this.repository = repository;
		this.revision = revision;
	}

	public BuildBoostRepository getRepository() {
		return repository;
	}

	public String getRevision() {
		return revision;
	}
}
