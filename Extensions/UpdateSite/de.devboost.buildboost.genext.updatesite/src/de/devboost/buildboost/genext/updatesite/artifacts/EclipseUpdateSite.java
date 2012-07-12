/*******************************************************************************
 * Copyright (c) 2006-2012
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
package de.devboost.buildboost.genext.updatesite.artifacts;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.AbstractXMLReader;

public class EclipseUpdateSite extends AbstractArtifact {

	private File file;

	public EclipseUpdateSite(File file) {
		this.file = file;
		readUpdateSiteFile();
		// use parent directory name as identifier
		setIdentifier(file.getParentFile().getName());
	}

	private void readUpdateSiteFile() {
		AbstractXMLReader xmlUtil = new AbstractXMLReader() {

			@Override
			protected void process(Document document, XPath xpath)
					throws XPathExpressionException {
				findFeatureDependencies(document, xpath);
			}

			@Override
			protected void addUnresolvedDependencies(Element element,
					UnresolvedDependency unresolvedDependency) {
				getUnresolvedDependencies().add(unresolvedDependency);
			}

			private void findFeatureDependencies(Document document, XPath xpath)
					throws XPathExpressionException {
				findDependencies(document, xpath, "//feature", "id", null, EclipseFeature.class);
			}
		};
		
		xmlUtil.readXMLFile(file);
	}

	public Collection<EclipseFeature> getFeatures() {
		Set<EclipseFeature> features = new LinkedHashSet<EclipseFeature>();
		Collection<IDependable> dependencies = getDependencies();
		for (IDependable dependency : dependencies) {
			if (dependency instanceof EclipseFeature) {
				EclipseFeature feature = (EclipseFeature) dependency;
				features.add(feature);
			}
		}
		return Collections.unmodifiableSet(features);
	}

	public File getFile() {
		return file;
	}
	
	@Override
	public long getTimestamp() {
		return file.lastModified();
	}
}
