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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;

import de.devboost.buildboost.util.EclipseFeatureHelper;

/**
 * The {@link EclipseFeatureFileFilter} accepts JARs that contain Eclipse features and directories which contain
 * extracted features.
 */
public class EclipseFeatureFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		return EclipseFeatureHelper.INSTANCE.isFeatureDirOrFeatureJar(file);
	}
}
