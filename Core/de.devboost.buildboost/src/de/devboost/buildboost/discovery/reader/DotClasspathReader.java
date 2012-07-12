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
package de.devboost.buildboost.discovery.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.util.StreamUtil;

/**
 * This class can be used to extract information from '.classpath' files.
 */
public class DotClasspathReader {

	private String content;
	
	public DotClasspathReader(InputStream dotClasspathInputStream) throws IOException {
		content = new StreamUtil().getContentAsString(dotClasspathInputStream);
	}

	public Set<String> getDependencies() {
		Set<String> libs = new LinkedHashSet<String>();
		
		String beginString = "kind=\"lib\" path=\"";
		String endString = "\"";
		
		int idx = content.indexOf(beginString);
		while (idx != -1) {
			int begin = idx + beginString.length();
			int end = content.indexOf(endString, begin);
			
			String path = content.substring(begin, end);
			libs.add(path);
			
			idx = content.indexOf(beginString, end);
		}
		
		return libs;
	}
}
