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
package de.devboost.buildboost.util;

import static de.devboost.buildboost.IConstants.NL;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.devboost.buildboost.IConstants;

/**
 * XMLContent objects can be used to compose contents for XML files. The XMLContent class automatically handles line
 * breaks and indentation.
 */
public class XMLContent {

	private List<String> lines = new ArrayList<String>();

	public void append(String text) {
		lines.add(text);
	}

	public void append(XMLContent content) {
		lines.addAll(content.lines);
	}

	private String composeContent() {
		int indentation = 0;
		Map<Integer, String> tabMap = new LinkedHashMap<Integer, String>();
		StringBuilder content = new StringBuilder();

		for (String text : lines) {
			if (NL.equals(text)) {
				content.append(NL);
				continue;
			}
			// tag that is closed on the same line or comment or XML header
			if (text.endsWith("/>") || text.endsWith("-->") || text.endsWith("?>") || text.matches(".*<.+>.+</.+>")) {
				content.append(getTabs(indentation, tabMap));
				content.append(text);
				content.append(NL);
			} else if (text.endsWith(">") && !text.startsWith("</")) {
				// tag that is opened
				content.append(getTabs(indentation, tabMap));
				content.append(text);
				content.append(NL);
				indentation++;
			} else if (text.startsWith("</")) {
				// tag that is closed
				indentation--;
				content.append(getTabs(indentation, tabMap));
				content.append(text);
				content.append(NL);
			} else {
				// invalid XML
				content.append(text);
				content.append(NL);
			}
		}
		return content.toString();
	}

	private String getTabs(int indentation, Map<Integer, String> tabMap) {
		String tabs = tabMap.get(indentation);
		if (tabs == null) {
			tabs = getTabString(indentation);
			tabMap.put(indentation, tabs);
		}
		return tabs;
	}

	private String getTabString(int indentation) {
		StringBuilder tabs = new StringBuilder();
		for (int i = 0; i < indentation; i++) {
			tabs.append('\t');
		}
		return tabs.toString();
	}

	@Override
	public String toString() {
		return composeContent();
	}

	public void appendLineBreak() {
		append(de.devboost.buildboost.IConstants.NL);
	}

	public XMLContent removeDuplicateEntries() {
		// remove duplicate entries
		String content = composeContent();
		String[] lines = content.split(IConstants.NL);
		Set<String> uniqueLines = new LinkedHashSet<String>();
		for (String line : lines) {
			uniqueLines.add(line);
		}

		XMLContent newContent = new XMLContent();
		for (String uniqueLine : uniqueLines) {
			newContent.append(uniqueLine);
		}
		return newContent;
	}
}
