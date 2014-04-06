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
package de.devboost.buildboost.artifacts;

import java.io.File;

/**
 * A {@link TargetPlatformZip} represents a ZIP or TAR/GZ file containing
 * Eclipse plug-ins or features.
 */
@SuppressWarnings("serial")
public class TargetPlatformZip extends AbstractArtifact {

	private final File zipFile;

	public TargetPlatformZip(File zipFile) {
		super();
		this.zipFile = zipFile;
		setIdentifier(zipFile.getName());
	}

	public File getZipFile() {
		return zipFile;
	}

	@Override
	public long getTimestamp() {
		return zipFile.lastModified();
	}
}
