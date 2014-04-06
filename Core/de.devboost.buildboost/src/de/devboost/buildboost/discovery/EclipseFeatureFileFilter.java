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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.devboost.buildboost.artifacts.EclipseFeature;

/**
 * The {@link EclipseFeatureFileFilter} accepts JARs that contain Eclipse features
 * and directories which contain extracted features.
 */
public class EclipseFeatureFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return isFeatureDirOrFeatureJar(file);
	}

	private boolean isFeatureDirOrFeatureJar(File file) {
		if (file.isDirectory()) {
			if (!isParentDirCalledFeatures(file)) {
				return false;
			}
			
			File featureDescriptor = new File(file, "feature.xml");
			if (featureDescriptor.exists()) {
				return true;
			}
		} else {
			if (file.getName().endsWith(".jar")) {
				// Check whether the JAR contains a file called
				// 'feature.xml'.
				ZipFile jar = null;
				try {
					jar = new ZipFile(file);
					ZipEntry entry = jar.getEntry(EclipseFeature.FEATURE_XML);
					if (entry != null) {
						return true;
					}
				} catch (IOException e) {
					// FIXME
					e.printStackTrace();
				} finally {
					if (jar != null) {
						try {
							jar.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}

				return false;
			}
		}

		return false;
	}

	private boolean isParentDirCalledFeatures(File file) {
		File parentFile = file.getParentFile();
		String parentName = parentFile.getName();
		return parentName.equals("features");
	}
}