/*******************************************************************************
 * Copyright (c) 2006-2015
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.buildboost.genext.updatesite.artifacts;

import java.io.File;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.discovery.reader.PropertyFileReader;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;

@SuppressWarnings("serial")
public class EclipseUpdateSiteDeploymentSpec extends AbstractArtifact {

	private File file;
	private PropertyFileReader propertyFileReader;

	public EclipseUpdateSiteDeploymentSpec(File file) {
		this.file = file;
		propertyFileReader = new PropertyFileReader(file);
		// use parent directory name as identifier
		String identifier = file.getParentFile().getName();
		setIdentifier(identifier);
		getUnresolvedDependencies().add(new UnresolvedDependency(EclipseUpdateSite.class, identifier, null, true, null, true, false, false));
	}
	
	public File getFile() {
		return file;
	}

	public EclipseUpdateSite getUpdateSite() {
		for (IDependable dependency : getDependencies()) {
			if (dependency instanceof EclipseUpdateSite) {
				EclipseUpdateSite eclipseUpdateSite = (EclipseUpdateSite) dependency;
				return eclipseUpdateSite;
			}
		}
		return null;
	}

	public String getSiteVendor() {
		return propertyFileReader.getValue("site", "vendor");
	}

	public String getSiteVersion() {
		return propertyFileReader.getValue("site", "version");
	}

	public String getFeatureVendor(String featureID) {
		String featureVendor = propertyFileReader.getValue("feature", featureID, "vendor");
		if (featureVendor == null) {
			featureVendor = getSiteVendor();
		}
		if (featureVendor == null) {
			featureVendor = "Unknown vendor";
		}
		return featureVendor;
	}

	@Override
	public long getTimestamp() {
		return file.lastModified();
	}

	public String getPluginVersion(String pluginID) {
		return propertyFileReader.getValue("plugin", pluginID, "version");
	}

	public String getPluginVendor(String pluginID) {
		return propertyFileReader.getValue("plugin", pluginID, "vendor");
	}

	public String getPluginName(String pluginID) {
		return propertyFileReader.getValue("plugin", pluginID, "name");
	}

	public String getSiteUploadPath() {
		return propertyFileReader.getValue("site", "uploadPath");
	}

	public String getExcludeSources() {
		return propertyFileReader.getValue("site", "excludeSources");
	}

	public String getSiteUsernameProperty() {
		return propertyFileReader.getValue("site", "usernameProperty");
	}

	public String getSitePasswordProperty() {
		return propertyFileReader.getValue("site", "passwordProperty");
	}

	public String getConfigs() {
		return propertyFileReader.getValue("site", "configs");
	}

	public String getSiteDependencies() {
		return propertyFileReader.getValue("site", "dependencies");
	}
}
