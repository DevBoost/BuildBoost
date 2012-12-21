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
package de.devboost.buildboost.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;

public abstract class AbstractAntTargetGenerator implements IAntTargetGenerator {

	final public static String JVMARG_MX = "-Xmx1024m";
	final public static String JVMARG_MAXPERM = "-XX:MaxPermSize=256m";
	final public static String JVMARG_DEBUG = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000";

	protected void writeParaFile(final String fileName,
			final List<Plugin> plugins) {
		final File paraPropFile = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(paraPropFile);
			for (Plugin plugin : plugins) {
				pw.println(plugin.getAbsolutePath());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}

	}

	@Override
	public String getIdentifier() {
		return toString();
	}

	@Override
	public Collection<IDependable> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public void resolveDependencies(Collection<? extends IArtifact> allArtifacts) {
		// do nothing
	}

	@Override
	public Collection<UnresolvedDependency> getUnresolvedDependencies() {
		return Collections.emptySet();
	}

	@Override
	public long getTimestamp() {
		return -1;
	}
}
