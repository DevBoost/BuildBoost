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
package de.devboost.buildboost.genext.toolproduct.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.discovery.AbstractFileFinder;
import de.devboost.buildboost.genext.toolproduct.artifacts.ToolProductSpecification;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.util.ArtifactUtil;

public class ToolProductSpecificationFinder extends AbstractFileFinder<ToolProductSpecification> {

	private IBuildListener buildListener;

	public ToolProductSpecificationFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {
		buildListener = context.getBuildListener();
		Collection<ToolProductSpecification> specifications = new ArrayList<ToolProductSpecification>();
		traverse(context, specifications);
		return new ArtifactUtil().getSetOfArtifacts(specifications);
	}

	protected ToolProductSpecification createArtifactFromFile(File file) {
		if (buildListener != null) {
			String message = "Discovered tool product specification: " + file.getAbsolutePath();
			buildListener.handleBuildEvent(BuildEventType.INFO, message);
		}
		return new ToolProductSpecification(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {
			
			public boolean accept(File file) {
				return file.getName().equals("toolproduct.spec") && file.isFile();
			}
		};
	}
}
