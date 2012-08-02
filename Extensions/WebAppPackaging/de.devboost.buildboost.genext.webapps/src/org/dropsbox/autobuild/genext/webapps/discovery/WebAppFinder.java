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
package org.dropsbox.autobuild.genext.webapps.discovery;

import java.util.Collection;
import java.util.Collections;

import org.dropsbox.autobuild.genext.webapps.util.WebAppUtil;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.discovery.AbstractArtifactDiscoverer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.UnresolvedDependency;

/**
 * A {@link WebAppFinder} can be used to find web applications among the set
 * of previously discovered projects. Thus, it must be used after the 
 * {@link PluginFinder}.
 */
public class WebAppFinder extends AbstractArtifactDiscoverer {

	private Collection<UnresolvedDependency> webAppDependencies;
	
	public WebAppFinder(Collection<UnresolvedDependency> webAppDependencies) {
		super();
		this.webAppDependencies = webAppDependencies;
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) {
		Collection<IArtifact> discoveredArtifacts = context.getDiscoveredArtifacts();
		for (IArtifact artifact : discoveredArtifacts) {
			if (artifact instanceof Plugin) {
				Plugin plugin = (Plugin) artifact;
				if (plugin.isProject() && new WebAppUtil().isWebApp(plugin)) {
					plugin.getUnresolvedDependencies().addAll(webAppDependencies);
				}
			}
		}
		return Collections.emptySet();
	}
}
