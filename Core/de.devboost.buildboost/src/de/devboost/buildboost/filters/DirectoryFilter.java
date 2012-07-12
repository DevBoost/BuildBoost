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
package de.devboost.buildboost.filters;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IFileArtifact;

/**
 * A DirectoryFilter can be used to exclude artifacts that are contained in a
 * certain directory. The DirectoryFilter walks up the parent hierarchy of the
 * file that contains the artifact and compares the names of the parent 
 * directories with the given exclusion list.
 */
public class DirectoryFilter extends AbstractFilter {

	private Set<String> excludedNames;
	
	public DirectoryFilter(Set<String> excludedNames) {
		this.excludedNames = excludedNames;
	}

	public DirectoryFilter(String... excludedNames) {
		this.excludedNames = new LinkedHashSet<String>();
		for (String name : excludedNames) {
			this.excludedNames.add(name);
		}
	}

	public boolean accept(IArtifact artifact) {
		if (artifact instanceof IFileArtifact) {
			IFileArtifact fileArtifact = (IFileArtifact) artifact;
			File file = fileArtifact.getFile();
			while (file != null) {
				String name = file.getName();
				if (excludedNames.contains(name)) {
					return false;
				}
				file = file.getParentFile();
			}
			
		}
		return true;
	}

}
