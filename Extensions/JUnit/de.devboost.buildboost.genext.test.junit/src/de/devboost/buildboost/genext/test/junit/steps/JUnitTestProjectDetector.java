/*******************************************************************************
 * Copyright (c) 2006-2014
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
package de.devboost.buildboost.genext.test.junit.steps;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import de.devboost.buildboost.artifacts.Plugin;

/**
 * The {@link JUnitTestProjectDetector} can be used to check whether a plug-in
 * projects contains JUnit tests. The current implementation basically searches
 * for classes where the name matches one from a given set of suffixes.
 */
public class JUnitTestProjectDetector {
	
	private Collection<String> testClassSuffixes;
	
	public JUnitTestProjectDetector(Collection<String> testClassSuffixes) {
		super();
		this.testClassSuffixes = testClassSuffixes;
	}

	public boolean containsTests(Plugin plugin) {
		// We explicitly exclude JUnit because it contains classes that have the
		// suffix 'Test', but which are not JUnit tests.
		if (plugin.getIdentifier().startsWith("org.junit")) {
			return false;
		}
		
		File[] sourceFolders = plugin.getSourceFolders();
		if (sourceFolders.length == 0) {
			// we can skip projects without source folders, because these
			// cannot contain test cases.
			return false;
		}

		boolean containsTestClasses = false;
		for (File sourceFolder : sourceFolders) {
			containsTestClasses |= containsTestClasses(sourceFolder);
		}
		
		return containsTestClasses;
	}

	private boolean containsTestClasses(File sourceFolder) {
		File[] testFiles = sourceFolder.listFiles(new FileFilter() {
			
			public boolean accept(File file) {
				String name = file.getName();
				boolean hasCorrectName = false;
				for (String suffix : testClassSuffixes) {
					hasCorrectName |= name.endsWith(suffix + ".java");
				}
				return file.isFile() && hasCorrectName;
			}
		});
		if (testFiles == null) {
			return false;
		}
		boolean folderContainsTestClasses = testFiles.length > 0;
		if (folderContainsTestClasses) {
			return true;
		}
		
		File[] subFolders = sourceFolder.listFiles(new FileFilter() {
			
			public boolean accept(File file) {
				return file.isDirectory() && !".".equals(file.getName()) && !"..".equals(file.getName());
			}
		});
		for (File subFolder : subFolders) {
			if (containsTestClasses(subFolder)) {
				return true;
			}
		}
		return false;
	}
}
