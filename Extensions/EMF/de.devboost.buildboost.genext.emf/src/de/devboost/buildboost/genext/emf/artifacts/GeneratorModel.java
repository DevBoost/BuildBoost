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
package de.devboost.buildboost.genext.emf.artifacts;

import java.io.File;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emf.IConstants;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.EclipsePluginHelper;

/**
 * A {@link GeneratorModel} represents a file with extension 'genmodel' that is
 * used by the Eclipse Modeling Framework (EMF) to configure code generation for
 * Ecore models.
 */
@SuppressWarnings("serial")
public class GeneratorModel extends AbstractArtifact {

	private final File file;
	private final File projectDir;

	public GeneratorModel(File file) {
		this.file = file;
		this.projectDir = EclipsePluginHelper.INSTANCE.findProjectDir(file);
		UnresolvedDependency buildBoostDependency = new UnresolvedDependency(Plugin.class, 
				IConstants.BUILDEXT_PLUGIN_ID, null, true, null, true, false, false);
		getUnresolvedDependencies().add(buildBoostDependency);
	}

	public String getIdentifier() {
		// TODO this is not unique
		return file.getName();
	}

	public File getFile() {
		return file;
	}

	public File getProjectDir() {
		return projectDir;
	}
	
	@Override
	public long getTimestamp() {
		return file.lastModified();
	}
}
