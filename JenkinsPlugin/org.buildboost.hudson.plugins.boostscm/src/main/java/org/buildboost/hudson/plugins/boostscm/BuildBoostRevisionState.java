package org.buildboost.hudson.plugins.boostscm;

import hudson.scm.SCMRevisionState;

import java.util.ArrayList;
import java.util.List;

public class BuildBoostRevisionState extends SCMRevisionState {
	
	private List<BuildBoostRepositoryState> states = new ArrayList<BuildBoostRepositoryState>();

	public void addRepositoryState(BuildBoostRepository repository, String revision) {
		String remoteURL = repository.getRemoteURL();
		// do not add duplicate states
		BuildBoostRepositoryState state = getRepositoryState(remoteURL);
		if (state == null) {
			states.add(new BuildBoostRepositoryState(repository, revision));
		}
	}

	private BuildBoostRepositoryState getRepositoryState(String remoteURL) {
		for (BuildBoostRepositoryState state : states) {
			String nextRemoteURL = state.getRepository().getRemoteURL();
			if (nextRemoteURL != null && nextRemoteURL.equals(remoteURL)) {
				return state;
			}
		}
		return null;
	}

	public List<BuildBoostRepositoryState> getRepositoryStates() {
		return states;
	}

	@Override
	public String toString() {
		return "BuildBoostRevisionState [states=" + states + "]";
	}
}
