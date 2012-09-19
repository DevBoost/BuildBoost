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

import java.util.logging.Logger;

public class BuildBoostRepositoryState {
	
	private static final Logger logger = Logger.getLogger(BuildBoostRepositoryState.class.getName());

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
