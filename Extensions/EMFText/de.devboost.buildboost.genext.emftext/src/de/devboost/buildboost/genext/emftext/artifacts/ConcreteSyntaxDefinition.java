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
package de.devboost.buildboost.genext.emftext.artifacts;

import java.io.File;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emftext.IConstants;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.EclipsePluginHelper;

/**
 * A ConcreteSyntaxDefinition represents an EMFText syntax specification (a
 * file with extension 'cs').
 */
@SuppressWarnings("serial")
public class ConcreteSyntaxDefinition extends AbstractArtifact {

	private File file;
	private File projectDir;

	public ConcreteSyntaxDefinition(File file) {
		super();
		this.file = file;
		this.projectDir = new EclipsePluginHelper().findProjectDir(file);
		
		// TODO this is not a unique identifier
		setIdentifier(file.getName());
		UnresolvedDependency buildBoostDependency = new UnresolvedDependency(Plugin.class, 
				IConstants.BUILDEXT_PLUGIN_ID, null, true, null, true, false, false);
		UnresolvedDependency emftextSDKDependency = new UnresolvedDependency(EclipseFeature.class, 
				"org.emftext.sdk", null, true, null, true, false, false);
		getUnresolvedDependencies().add(buildBoostDependency);
		getUnresolvedDependencies().add(emftextSDKDependency);
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
