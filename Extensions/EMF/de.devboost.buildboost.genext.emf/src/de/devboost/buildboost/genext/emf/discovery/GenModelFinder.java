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
package de.devboost.buildboost.genext.emf.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.discovery.AbstractFileFinder;
import de.devboost.buildboost.genext.emf.artifacts.GeneratorModel;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.util.ArtifactUtil;
import de.devboost.buildboost.util.EclipsePluginHelper;

/**
 * A {@link GenModelFinder} can be used to discover EMF generator models (i.e.,
 * file with extension 'genmodel').
 */
public class GenModelFinder extends AbstractFileFinder<GeneratorModel> {
	
	public String TEST_PROJECT_SUFFIX = ".test";

	public GenModelFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {
		Collection<GeneratorModel> genModels = new ArrayList<GeneratorModel>();
		traverse(context, genModels);
		return new ArtifactUtil().getSetOfArtifacts(genModels);
	}

	protected GeneratorModel createArtifactFromFile(File file) {
		return new GeneratorModel(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {		
			public boolean accept(File file) {
				return file.getName().endsWith(".genmodel") && file.isFile() &&
						new EclipsePluginHelper().findProjectDir(file) != null;
			}
		};
	}
	
	protected FileFilter getDirectoryFilter() {
		return new FileFilter() {	
			public boolean accept(File file) {
				return file.isDirectory() && !file.getName().endsWith(TEST_PROJECT_SUFFIX);
			}
		};
	}
}
