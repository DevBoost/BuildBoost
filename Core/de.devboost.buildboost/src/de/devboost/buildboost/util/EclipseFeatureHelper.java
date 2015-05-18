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

import java.io.File;

import de.devboost.buildboost.artifacts.EclipseFeature;

public class EclipseFeatureHelper {

	public final static EclipseFeatureHelper INSTANCE = new EclipseFeatureHelper();

	private EclipseFeatureHelper() {
		super();
	}

	public boolean isFeatureDirOrFeatureJar(File file) {

		if (isFeatureDir(file)) {
			return true;
		}
		if (isFeatureJAR(file)) {
			return true;
		}

		return false;
	}

	public boolean isFeatureDir(File file) {
		if (file.isDirectory()) {
			if (!isParentDirCalledFeatures(file)) {
				return false;
			}

			File featureDescriptor = new File(file, "feature.xml");
			if (featureDescriptor.exists()) {
				return true;
			}
		}
		return false;
	}

	private boolean isParentDirCalledFeatures(File file) {
		File parentFile = file.getParentFile();
		String parentName = parentFile.getName();
		return parentName.equals("features");
	}

	public boolean isFeatureJAR(File file) {
		if (file.isFile() && file.getName().endsWith(".jar")) {
			// Check whether the JAR contains a file called 'feature.xml'.
			String entryName = EclipseFeature.FEATURE_XML;
			return ZipFileHelper.INSTANCE.containsZipEntry(file, entryName);
		}

		return false;
	}
}
