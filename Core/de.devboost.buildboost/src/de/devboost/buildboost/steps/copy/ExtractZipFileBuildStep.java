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
package de.devboost.buildboost.steps.copy;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.TargetPlatformZip;
import de.devboost.buildboost.util.XMLContent;

public class ExtractZipFileBuildStep extends AbstractAntTargetGenerator {

	private TargetPlatformZip zip;
	private File targetDir;
	
	public ExtractZipFileBuildStep(TargetPlatformZip zip, File targetDir) {
		super();
		this.zip = zip;
		this.targetDir = targetDir;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		XMLContent content = new XMLContent();
		content.append("<unzip src=\"" + zip.getZipFile().getAbsolutePath() + "\" dest=\"" + targetDir.getAbsolutePath() + "\" />");
		return Collections.singleton(new AntTarget("unzip-target-platform-" + zip.getIdentifier(), content));
	}
}
