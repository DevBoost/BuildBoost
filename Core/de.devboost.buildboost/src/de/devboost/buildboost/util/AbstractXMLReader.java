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
package de.devboost.buildboost.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.UnresolvedDependency;

public abstract class AbstractXMLReader {

	public void readXMLFile(File file) {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			readXMLStrem(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void readXMLStrem(InputStream inputStream) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    
		try {
		    DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			XPath xpath = XPathFactory.newInstance().newXPath();
			process(document, xpath);
			inputStream.close();
		} catch (ParserConfigurationException e) {
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (SAXException e) {
		} catch (XPathExpressionException e) {
		}
		// TODO handle exceptions?
	}

	protected void findDependencies(Document document, XPath xpath,
			String pathExpression, String idAttribute, String optionalAttribute,
			Class<? extends IArtifact> dependencyType)
			throws XPathExpressionException {
		NodeList nodelist = (NodeList) xpath.evaluate(pathExpression, document, XPathConstants.NODESET);
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String idAttributeValue = element.getAttribute(idAttribute);
				// ignore elements where the ID attribute is not set
				if (idAttributeValue == null || "".equals(idAttributeValue.trim())) {
					continue;
				}
				
				boolean optional = false;
				if (optionalAttribute != null) {
					String optionalValue = element.getAttribute(optionalAttribute);
					if ("true".equals(optionalValue)) {
						optional = true;
					}
				}
				
				// TODO add version?
				addUnresolvedDependencies(element, new UnresolvedDependency(dependencyType, idAttributeValue, null, true, null, true, optional, false));
			}
		}
	}

	protected abstract void process(Document document, XPath xpath) throws XPathExpressionException;

	protected abstract void addUnresolvedDependencies(Element element, UnresolvedDependency unresolvedDependency);
}
