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
