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
package de.devboost.buildboost.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import de.devboost.buildboost.discovery.reader.ManifestReader;
import de.devboost.buildboost.model.UnresolvedDependency;

public class ManifestReaderTest {

	@Test
	public void testReading() {
		try {
			String plugin = "org.emftext.sdk";
			getDependencies(plugin);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			String plugin = "org.dropsbox";
			Set<UnresolvedDependency> dependencies = getDependencies(plugin);
			assertEquals(2, dependencies.size());
			Iterator<UnresolvedDependency> iterator = dependencies.iterator();
			UnresolvedDependency first = iterator.next();
			UnresolvedDependency second = iterator.next();
			assertEquals("org.emftext.sdk", first.getIdentifier());
			assertEquals("org.apache.ant", second.getIdentifier());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testBundleName() throws IOException {
		String input = "Bundle-SymbolicName: com.mysql.connector-java5_1_16\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		String symbolicName = new ManifestReader(bais).getSymbolicName();
		assertEquals("com.mysql.connector-java5_1_16", symbolicName);
	}

	@Test
	public void testDependency3() throws IOException {
		String input = "Require-Bundle: com.mysql.connector-java5_1_16\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		Set<UnresolvedDependency> dependencies = new ManifestReader(bais).getDependencies();
		assertEquals(1, dependencies.size());
		assertEquals("com.mysql.connector-java5_1_16", dependencies.iterator().next().getIdentifier());
	}

	@Test
	public void testDependency4() throws IOException {
		String input = "Require-Bundle: org.eclipse.core.runtime,\n org.eclipse.emf.ecore;visibility:=reexport\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		Set<UnresolvedDependency> dependencies = new ManifestReader(bais).getDependencies();
		assertEquals(2, dependencies.size());
		Iterator<UnresolvedDependency> iterator = dependencies.iterator();
		UnresolvedDependency dependency1 = iterator.next();
		UnresolvedDependency dependency2 = iterator.next();
		assertEquals("org.eclipse.core.runtime", dependency1.getIdentifier());
		assertEquals("org.eclipse.emf.ecore", dependency2.getIdentifier());
	}

	@Test
	public void testRegex() throws IOException {
		String input = ManifestReader.SYMBOLIC_NAME_PREFIX + "myplugin; singleton:=true\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		String symbolicName = new ManifestReader(bais).getSymbolicName();
		assertEquals("myplugin", symbolicName);
	}

	@Test
	public void testRegex2() throws IOException {
		assertTrue("org.apache.commons".matches(ManifestReader.QUALIFIED_NAME_REGEX));
		assertTrue("2.0.0".matches(ManifestReader.QUALIFIED_NUMBER_REGEX));

		assertTrue("bundle-version=\"2.0.0\"".matches(ManifestReader.BUNDLE_VERSION_REGEX));
		assertTrue("bundle-version=\"[2.0.0,3.0.0)\"".matches(ManifestReader.BUNDLE_VERSION_REGEX));
		assertFalse("bundle-version=\"[2.0.0,3.0.0)\",".matches(ManifestReader.BUNDLE_VERSION_REGEX));
		
		assertTrue("-".matches("-"));
		assertTrue("bundle-version".matches(ManifestReader.OPTION_NAME_REGEX));
		assertTrue("\"[2.0.0,3.0.0)\"".matches(ManifestReader.OPTION_VALUE_REGEX));
		assertTrue("optional".matches(ManifestReader.OPTION_VALUE_REGEX));

		assertTrue(";bundle-version=\"[2.0.0,3.0.0)\"".matches(ManifestReader.OPTION_REGEX));
		assertFalse(";bundle-version=\"[2.0.0,3.0.0)\";".matches(ManifestReader.OPTION_REGEX));

		assertTrue(";resolution:=optional".matches(ManifestReader.OPTION_REGEX));
		assertTrue(";resolution:=\"optional\"".matches(ManifestReader.OPTION_REGEX));
		assertTrue(";visibility:=reexport".matches(ManifestReader.OPTION_REGEX));

		assertTrue("com.mysql.connector-java5_1_16".matches(ManifestReader.OPTION_VALUE_REGEX));
	}
	
	@Test
	public void testDependencies() throws IOException {
		String input = 
				"Require-Bundle: org.apache.commons.lang;bundle-version=\"[2.0.0,3.0.0)\"" +
				",org.apache.commons.logging;bundle-version=\"[1.0.0,2.0.0)\";resolution:=optional;visibility:=reexport\n";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		Set<UnresolvedDependency> dependencies = new ManifestReader(bais).getDependencies();
		System.out.println(dependencies);
		assertEquals(2, dependencies.size());
		
		Iterator<UnresolvedDependency> iterator = dependencies.iterator();
		UnresolvedDependency first = iterator.next();
		UnresolvedDependency second = iterator.next();
		assertEquals("org.apache.commons.lang", first.getIdentifier());
		assertFalse(first.isOptional());
		assertFalse(first.isReexported());

		assertEquals("org.apache.commons.logging", second.getIdentifier());
		assertTrue(second.isOptional());
		assertTrue(second.isReexported());
	}

	@Test
	public void testDependencies2() throws IOException {
	
		String input = "Require-Bundle: org.eclipse.core.runtime; bundle-version=\"[3.4.0,4.0.0)\"\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		Set<UnresolvedDependency> dependencies = new ManifestReader(bais).getDependencies();
		System.out.println(dependencies);
		assertEquals(1, dependencies.size());

		Iterator<UnresolvedDependency> iterator = dependencies.iterator();
		UnresolvedDependency first = iterator.next();
		assertEquals("org.eclipse.core.runtime", first.getIdentifier());
		assertEquals("3.4.0", first.getMinVersion());
		assertEquals("4.0.0", first.getMaxVersion());
	}
	
	@Test
	public void testGetVersion() throws IOException {
		String input = "Require-Bundle: org.hamcrest.core;bundle-version=\"1.1.0\";visibility:=r\n" +
			" eexport,org.junit;bundle-version=\"4.8.1\";visibility:=reexport\n" +
			"Bundle-Version: 4.8.1.v20100525\n"+
			"Bundle-ManifestVersion: 2\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		ManifestReader reader = new ManifestReader(bais);
		assertEquals("4.8.1", reader.getVersion());
	}
	
	@Test
	public void testGetMissingVersion() throws IOException {
		String input = "Require-Bundle: org.hamcrest.core;bundle-version=\"1.1.0\";visibility:=r\n" +
			" eexport,org.junit;bundle-version=\"4.8.1\";visibility:=reexport\n" +
			"Bundle-ManifestVersion: 2\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		ManifestReader reader = new ManifestReader(bais);
		assertNull("Version must be null if missing.", reader.getVersion());
	}
	
	public void testGetOptional() throws IOException {
		String input = "Require-Bundle: org.eclipse.core.resources;resolution:=\"optional\"\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		ManifestReader reader = new ManifestReader(bais);
		Set<UnresolvedDependency> dependencies = reader.getDependencies();
		UnresolvedDependency dependency = dependencies.iterator().next();
		assertTrue("Resolution must be optional.", dependency.isOptional());
	}
	
	@Test
	public void testGetFragmentHost() throws IOException {
		String input = "Fragment-Host: org.eclipse.swt; bundle-version=\"[3.0.0,4.0.0)\"\n" +
				"Bundle-Name: %fragmentName\n" +
				"Bundle-SymbolicName: org.eclipse.swt.gtk.linux.x86_64; singleton:=true\n" +
				"Bundle-Version: 3.7.0.v3735b\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
		ManifestReader reader = new ManifestReader(bais);
		assertEquals("org.eclipse.swt", reader.getFragmentHost().getIdentifier());
	}

	private Set<UnresolvedDependency> getDependencies(String plugin) throws IOException {
		File file = new File("input", plugin + ".MF");
		FileInputStream fis = new FileInputStream(file);
		return new ManifestReader(fis).getDependencies();
	}
}
