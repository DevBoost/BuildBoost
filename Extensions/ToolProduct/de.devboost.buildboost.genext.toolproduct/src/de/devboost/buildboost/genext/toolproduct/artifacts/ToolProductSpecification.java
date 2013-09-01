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
package de.devboost.buildboost.genext.toolproduct.artifacts;

import java.io.File;
import java.util.Map;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.discovery.reader.PropertyFileReader;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;

public class ToolProductSpecification extends AbstractArtifact {

	private static final long serialVersionUID = -756727502003553869L;

	private PropertyFileReader propertyFileReader;

	private File file;

	public ToolProductSpecification(File file) {
		super();
		this.file = file;
		this.propertyFileReader = new PropertyFileReader(file);
		
		// use parent directory name as identifier
		File parentFile = file.getParentFile();
		String identifier = parentFile.getName();
		setIdentifier(identifier);
		
		String updateSiteID = propertyFileReader.getValue("updatesite");
		getUnresolvedDependencies().add(new UnresolvedDependency(EclipseUpdateSite.class, updateSiteID, null, true, null, true, false, false));
	}

	public String getProductName() {
		return propertyFileReader.getValue("name");
	}

	public String getEclipseMirror() {
		return propertyFileReader.getValue("eclipsemirror");
	}

	public String getProductFeature() {
		return propertyFileReader.getValue("feature");
	}

	/**
	 * Returns are list of associated update sites that will be used when
	 * installing the tool product feature into Eclipse.
	 */
	public String getAssociateSites() {
		return propertyFileReader.getValue("associatesites");
	}

	public Map<String, String> getProductTypes() {
		return propertyFileReader.getValues("type");
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

	public File getFile() {
		return file;
	}

	public String getPlatformPluginProperty(String property) {
		return propertyFileReader.getValue("platform", "plugin.xml", property);
	}
}
