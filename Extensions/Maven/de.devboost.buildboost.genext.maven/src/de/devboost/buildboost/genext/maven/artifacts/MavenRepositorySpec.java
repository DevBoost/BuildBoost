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
package de.devboost.buildboost.genext.maven.artifacts;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.discovery.reader.PropertyFileReader;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;

public class MavenRepositorySpec extends AbstractArtifact {

	private static final long serialVersionUID = -169241029129248435L;

	private static final String STOP_ON_ERROR = "stop_on_error";
	
	private final PropertyFileReader propertyFileReader;

	public MavenRepositorySpec(File file) {
		propertyFileReader = new PropertyFileReader(file);
		// use parent directory name as identifier
		String identifier = getIdentifier(file);
		setIdentifier(identifier);
		getUnresolvedDependencies().add(new UnresolvedDependency(EclipseUpdateSiteDeploymentSpec.class, identifier, null, true, null, true, false, false));
	}

	private String getIdentifier(File file) {
		String updatesiteid = propertyFileReader.getValue("updatesiteid");
		if (updatesiteid != null) {
			return updatesiteid;
		}
		
		// If not update site is given, we use the parent folder's name as id
		return file.getParentFile().getName();
	}

	public EclipseUpdateSiteDeploymentSpec getUpdateSite() {
		for (IDependable dependency : getDependencies()) {
			if (dependency instanceof EclipseUpdateSiteDeploymentSpec) {
				EclipseUpdateSiteDeploymentSpec eclipseUpdateSite = (EclipseUpdateSiteDeploymentSpec) dependency;
				return eclipseUpdateSite;
			}
		}
		return null;
	}

	public String getUserNameProperty() {
		return propertyFileReader.getValue("usernameProperty");
	}

	public String getPasswordProperty() {
		return propertyFileReader.getValue("passwordProperty");
	}

	public Set<String> getIncludedPlugins() {
		String path = "includes";
		return getList(path);
	}

	public Set<String> getPluginsAssumedAvailable() {
		String path = "assumed_available";
		return getList(path);
	}

	public boolean isStopOnError() {
		return "true".equalsIgnoreCase(propertyFileReader.getValue(STOP_ON_ERROR));
	}
	
	private Set<String> getList(String path) {
		String includes = propertyFileReader.getValue(path);
		if (includes == null) {
			return Collections.emptySet();
		}
		
		String[] pluginIDs = includes.split(";");
		return new LinkedHashSet<String>(Arrays.asList(pluginIDs));
	}

	public String getRepositoryPath() {
		return propertyFileReader.getValue("path");
	}
}
