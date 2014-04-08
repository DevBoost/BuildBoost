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
package de.devboost.buildboost.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import de.devboost.buildboost.artifacts.InvalidMetadataException;
import de.devboost.buildboost.artifacts.Plugin;

/**
 * This test checks whether source directories which at located in sub folders
 * are handled correctly. This is usually a problem for Maven projects where
 * sources are located in 'src/main/java'.
 */
public class MavenClasspathTest {

	@Test
	public void testSourcePathHandling() {
		try {
			File location = new File("test.maven");
			Plugin plugin = new Plugin(location) {
				
				private static final long serialVersionUID = -2451561338369270774L;

				@Override
				protected InputStream getManifestInputStream()
						throws IOException {
					return null;
				}
				
				@Override
				protected InputStream getDotClasspathInputStream()
						throws FileNotFoundException {

					byte[] content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><classpath><classpathentry kind=\"src\" output=\"target/classes\" path=\"src/main/java\"/></classpath>".getBytes();
					return new ByteArrayInputStream(content);
				}
			};
			File[] sourceFolders = plugin.getSourceFolders();
			assertEquals(1, sourceFolders.length);
			File sourceFolder = sourceFolders[0];
			assertEquals("test.maven/src/main/java", sourceFolder.getPath().replace(File.separator, "/"));
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (InvalidMetadataException e) {
			fail(e.getMessage());
		}
	}
}
