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
package de.devboost.buildboost.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.Test;

import de.devboost.buildboost.discovery.reader.DotClasspathReader;

public class DotClasspathReaderTest {

	public final static String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<classpath>" +
			"<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>" +
			"<classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>" +
			"<classpathentry kind=\"src\" path=\"src\"/>" +
			"<classpathentry kind=\"src\" path=\"src-gen\"/>" +
			"<classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/otherproject\"/>" +
			"<classpathentry kind=\"lib\" path=\"lib/some-third-party-lib-1.0.0.jar\"/>" +
			"<classpathentry kind=\"lib\" path=\"lib/some-lib-with-source-2.0.0.jar\" sourcepath=\"/Users/Me/Downloads/some-lib-with-source-2.0.0-src.zip\"/>" +
			"<classpathentry kind=\"output\" path=\"bin\"/>" +
			"</classpath>";
	
	@Test
	public void testLibReading() throws IOException {
		DotClasspathReader reader = read();
		Set<String> dependencies = reader.getLibraries();
		assertEquals("Unexpected number of dependencies", 2, dependencies.size());
		
		assertTrue(dependencies.contains("lib/some-third-party-lib-1.0.0.jar"));
		assertTrue(dependencies.contains("lib/some-lib-with-source-2.0.0.jar"));
	}

	@Test
	public void testSourceFolderReading() throws IOException {
		DotClasspathReader reader = read();
		Set<String> sourceFolders = reader.getSourceFolders();
		assertEquals("Unexpected number of source folders", 2, sourceFolders.size());
		
		assertTrue(sourceFolders.contains("src"));
		assertTrue(sourceFolders.contains("src-gen"));
	}

	private DotClasspathReader read() throws IOException {
		byte[] bytes = input.getBytes();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		
		DotClasspathReader reader = new DotClasspathReader(inputStream);
		return reader;
	}
}
