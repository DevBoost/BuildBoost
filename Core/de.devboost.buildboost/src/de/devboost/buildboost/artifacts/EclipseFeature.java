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
package de.devboost.buildboost.artifacts;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.AbstractXMLReader;

public class EclipseFeature extends AbstractArtifact {

	private File file;

	public EclipseFeature(File file) {
		super();
		this.file = file;
		if (file.getName().equals("feature.xml")) {
			readFeatureFile(file);
		} else {
			// this is JAR
			throw new RuntimeException("Can't handle packaged features (yet). Path is " + file.getAbsolutePath());
		}
	}
	
	private void readFeatureFile(File file) {
		AbstractXMLReader xmlUtil = new AbstractXMLReader() {

			@Override
			protected void process(Document document, XPath xpath)
					throws XPathExpressionException {
				findIdentifier(document, xpath);
				findContainedPluginDependencies(document, xpath);
				findFeatureDependencies(document, xpath);
				findPluginDependencies(document, xpath);
			}

			@Override
			protected void addUnresolvedDependencies(Element element,
					UnresolvedDependency unresolvedDependency) {
				// we exclude dependencies that are specific to a particular OS
				// or windowing system
				boolean isOsIndependent = isAttributeNotSet(element, "os");
				isOsIndependent &= isAttributeNotSet(element, "ws");
				if (isOsIndependent) {
					getUnresolvedDependencies().add(unresolvedDependency);
				}
			}

			private boolean isAttributeNotSet(Element element, String attributeName) {
				String value = element.getAttribute(attributeName);
				boolean isNotSet = value == null || "".equals(value.trim());
				return isNotSet;
			}

			private void findIdentifier(Document document, XPath xpath) throws XPathExpressionException {
				Element element = (Element) xpath.evaluate("//feature", document, XPathConstants.NODE);
				setIdentifier(element.getAttribute("id"));
			}

			private void findContainedPluginDependencies(Document document, XPath xpath)
					throws XPathExpressionException {
				findDependencies(document, xpath, "//plugin", "id", "fragment", Plugin.class);
			}

			private void findFeatureDependencies(Document document, XPath xpath)
					throws XPathExpressionException {
				findDependencies(document, xpath, "//import", "feature", null, EclipseFeature.class);
			}

			private void findPluginDependencies(Document document, XPath xpath)
					throws XPathExpressionException {
				findDependencies(document, xpath, "//import", "plugin", null, Plugin.class);
			}
		};
		
		xmlUtil.readXMLFile(file);
	}

	public Collection<Plugin> getPlugins() {
		Set<Plugin> plugins = new LinkedHashSet<Plugin>();
		Collection<IDependable> dependencies = getDependencies();
		for (IDependable dependency : dependencies) {
			if (dependency instanceof Plugin) {
				Plugin plugin = (Plugin) dependency;
				plugins.add(plugin);
			}
		}
		return Collections.unmodifiableSet(plugins);
	}

	public File getFile() {
		return file;
	}

	@Override
	public long getTimestamp() {
		return file.lastModified();
	}
}
