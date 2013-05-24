/*******************************************************************************
 * Copyright (c) 2006-2013
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
package de.devboost.buildboost.genext.maven.steps;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;

public class PackagePluginTask {

	private Plugin plugin;
	private String featureVersion;
	private String featureVendor;
	private EclipseUpdateSiteDeploymentSpec deploymentSpec;
	
	public PackagePluginTask(Plugin plugin, String featureVersion,
			String featureVendor, EclipseUpdateSiteDeploymentSpec deploymentSpec) {
		super();
		this.plugin = plugin;
		this.featureVersion = featureVersion;
		this.featureVendor = featureVendor;
		this.deploymentSpec = deploymentSpec;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public String getFeatureVersion() {
		return featureVersion;
	}

	public String getFeatureVendor() {
		return featureVendor;
	}

	public EclipseUpdateSiteDeploymentSpec getDeploymentSpec() {
		return deploymentSpec;
	}
}
