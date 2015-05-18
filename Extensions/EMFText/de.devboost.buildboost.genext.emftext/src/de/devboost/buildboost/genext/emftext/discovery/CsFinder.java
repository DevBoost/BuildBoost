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
package de.devboost.buildboost.genext.emftext.discovery;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.discovery.AbstractFileFinder;
import de.devboost.buildboost.genext.emftext.artifacts.ConcreteSyntaxDefinition;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.util.ArtifactUtil;

/**
 * A {@link CsFinder} can be used to discover EMFText concrete syntax 
 * definitions. Files with extension 'cs' are recognized as such.
 */
public class CsFinder extends AbstractFileFinder<ConcreteSyntaxDefinition> {
	
	public String TEST_PROJECT_SUFFIX = ".test";

	public CsFinder(File directory) {
		super(directory);
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) throws BuildException {
		Collection<ConcreteSyntaxDefinition> csDefinitions = new ArrayList<ConcreteSyntaxDefinition>();
		traverse(context, csDefinitions);
		return new ArtifactUtil().getSetOfArtifacts(csDefinitions);
	}

	protected ConcreteSyntaxDefinition createArtifactFromFile(File file) {
		return new ConcreteSyntaxDefinition(file);
	}

	protected FileFilter getFileFilter() {
		return new FileFilter() {		
			public boolean accept(File file) {
				return file.getName().endsWith(".cs") && file.isFile();
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
