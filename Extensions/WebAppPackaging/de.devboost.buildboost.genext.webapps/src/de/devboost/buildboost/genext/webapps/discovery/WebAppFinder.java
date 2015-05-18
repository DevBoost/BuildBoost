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
package de.devboost.buildboost.genext.webapps.discovery;

import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.discovery.AbstractArtifactDiscoverer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildContext;

/**
 * A {@link WebAppFinder} can be used to find web applications among the set
 * of previously discovered projects. Thus, it must be used after the 
 * {@link PluginFinder}.
 * 
 * TODO Remove this class? It was previously used to add a dependency to Tomcat,
 * but this is obsolete now.
 */
public class WebAppFinder extends AbstractArtifactDiscoverer {

	public WebAppFinder() {
		super();
	}

	public Collection<IArtifact> discoverArtifacts(IBuildContext context) {
		return Collections.emptySet();
	}
}
