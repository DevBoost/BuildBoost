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
package de.devboost.buildboost.discovery.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.devboost.buildboost.util.StreamUtil;

/**
 * This class can be used to extract information from '.classpath' files.
 */
public class DotClasspathReader {

	private final String content;
	
	private Set<String> libs;
	private Set<String> sourceFolders;

	public DotClasspathReader(InputStream dotClasspathInputStream)
			throws IOException {
		super();
		this.content = new StreamUtil().getContentAsString(dotClasspathInputStream);
	}

	public Set<String> getLibraries() {
		if (libs == null) {
			initialize();
		}
		return libs;
	}

	public Set<String> getSourceFolders() {
		if (sourceFolders == null) {
			initialize();
		}
		return sourceFolders;
	}

	private void initialize() {
		libs = new LinkedHashSet<String>();
		sourceFolders = new LinkedHashSet<String>();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(content.getBytes()));
			NodeList classPathEntryNodes = doc.getElementsByTagName("classpathentry");
			for (int i = 0; i < classPathEntryNodes.getLength(); i++) {
				Node classPathEntryNode = classPathEntryNodes.item(i);
				if (classPathEntryNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				
				Element element = (Element) classPathEntryNode;
				String kind = element.getAttribute("kind");
				String path = element.getAttribute("path");
				String combineaccessrules = element.getAttribute("combineaccessrules");;
				boolean combineaccessrulesIsSet = combineaccessrules != null && !combineaccessrules.isEmpty();
				if ("lib".equals(kind)) {
					if (path != null) {
						libs.add(path);
					}
				}
				if ("src".equals(kind)) {
					if (path != null && !combineaccessrulesIsSet) {
						sourceFolders.add(path);
					}
				}
			}
		} catch (ParserConfigurationException e) {
			throwRuntimeException(e);
		} catch (SAXException e) {
			throwRuntimeException(e);
		} catch (IOException e) {
			throwRuntimeException(e);
		}
	}

	private void throwRuntimeException(Exception e) {
		String className = e.getClass().getSimpleName();
		String message = className + " while reading .classpath file: " + e.getMessage();
		throw new RuntimeException(message);
	}
}
