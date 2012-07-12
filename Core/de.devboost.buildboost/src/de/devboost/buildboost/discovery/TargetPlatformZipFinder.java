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
package de.devboost.buildboost.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.artifacts.TargetPlatformZip;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.ArtifactUtil;

public class TargetPlatformZipFinder extends AbstractFileFinder<TargetPlatformZip> {

	public TargetPlatformZipFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) {
		IBuildListener buildListener = context.getBuildListener();

		Collection<TargetPlatformZip> zipFiles = new ArrayList<TargetPlatformZip>();
		traverse(context, zipFiles);
		for (TargetPlatformZip targetPlatformZip : zipFiles) {
			buildListener.handleBuildEvent(BuildEventType.INFO, "Found target platform zip: " + targetPlatformZip.getIdentifier());
		}
		return new ArtifactUtil().getSetOfArtifacts(zipFiles);
	}

	protected TargetPlatformZip createArtifactFromFile(File file) {
		return new TargetPlatformZip(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {
			
			public boolean accept(File file) {
				return file.getName().endsWith(".zip") && file.getName().startsWith("eclipse-");
			}
		};
	}
}
