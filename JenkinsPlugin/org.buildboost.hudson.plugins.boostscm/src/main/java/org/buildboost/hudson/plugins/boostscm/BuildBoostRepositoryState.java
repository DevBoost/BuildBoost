package org.buildboost.hudson.plugins.boostscm;

import java.util.logging.Logger;

public class BuildBoostRepositoryState {
	
	private final Logger logger = Logger.getLogger(BuildBoostRepositoryState.class.getName());

	private BuildBoostRepository repository;
	private String revision;
	
	public BuildBoostRepositoryState(BuildBoostRepository repository, String revision) {
		super();
		logger.info("repository = " + repository);
		logger.info("revision = " + revision);
		this.repository = repository;
		this.revision = revision;
	}

	public BuildBoostRepository getRepository() {
		return repository;
	}

	public String getRevision() {
		return revision;
	}

	@Override
	public String toString() {
		return "BuildBoostRepositoryState [repository=" + repository
				+ ", revision=" + revision + "]";
	}
}
