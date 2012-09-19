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
