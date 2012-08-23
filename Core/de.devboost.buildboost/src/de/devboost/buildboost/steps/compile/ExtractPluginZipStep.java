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
package de.devboost.buildboost.steps.compile;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.util.XMLContent;

public class ExtractPluginZipStep extends AbstractAntTargetGenerator {

	private CompiledPlugin plugin;
	private File targetPlatformCache;

	public ExtractPluginZipStep(CompiledPlugin plugin, File targetPlatformCache) {
		this.plugin = plugin;
		this.targetPlatformCache = targetPlatformCache;
	}

	public Collection<AntTarget> generateAntTargets() {
		XMLContent content = new XMLContent();
		File zippedLocation = plugin.getLocation();
		plugin.unzip();
		File unzippedLocation = plugin.getLocation();
		content.append("<unzip src=\"" + zippedLocation.getAbsolutePath() + "\" dest=\"" + unzippedLocation.getAbsolutePath() + "\" />");
		content.append("<delete file=\"" + zippedLocation.getAbsolutePath() + "\" />");
		//delete cache file because target platform has changed
		content.append("<delete file=\"" + targetPlatformCache.getAbsolutePath() + "\" />");
		return Collections.singleton(new AntTarget("unzip-plugin-with-libs-" + plugin.getIdentifier(), content));
	}

}
