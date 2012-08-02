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
package org.dropsbox.autobuild.genext.webapps.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

public class WebAppPackagingStep extends AbstractAntTargetGenerator {

	private Plugin plugin;
	private Collection<UnresolvedDependency> webAppDependencies;

	public WebAppPackagingStep(Plugin plugin, Collection<UnresolvedDependency> webAppDependencies) {
		super();
		this.plugin = plugin;
		this.webAppDependencies = webAppDependencies;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		XMLContent content = new XMLContent();

		File webContentDir = new File(plugin.getLocation(), "WebContent");
		File webXmlFile = new File(new File(webContentDir, "WEB-INF"), "web.xml");
		
		content.append("<mkdir dir=\"dist/webapps\" />");
		content.append("<war destfile=\"dist/webapps/" + plugin.getIdentifier() + ".war\" webxml=\"" + webXmlFile.getAbsolutePath() + "\">");
		content.append("<fileset dir=\"" + webContentDir.getAbsolutePath() + "\" />");
	    content.append("<classes dir=\"" + new ClasspathHelper().getBinPath(plugin) + "\" />");
	    Set<Plugin> dependencies = plugin.getAllDependencies();
	    for (Plugin dependency : dependencies) {
	    	boolean isArtificalDependency = false;
	    	for (UnresolvedDependency webAppDependency : webAppDependencies) {
				if (webAppDependency.isFulfilledBy(dependency)) {
			    	isArtificalDependency = true;
					break;
				}
			}
	    	// we do not include artificial dependencies that have been only 
	    	// added to compile correctly
			if (isArtificalDependency) {
				continue;
			}
	    	File location = dependency.getLocation();
			if (dependency.isProject()) {
			    content.append("<classes dir=\"" + new ClasspathHelper().getBinPath(dependency) + "\" >");
			    content.append("<exclude name=\"META-INF/MANIFEST.MF\" />");
			    content.append("</classes>");
			    content.append("<classes dir=\"" + dependency.getAbsolutePath() + "\" >");
			    content.append("<include name=\"metamodel/**\" />");
			    content.append("</classes>");
			    for (String lib : dependency.getLibs()) {
			    	String absoluteLibPath = dependency.getAbsoluteLibPath(lib);
			    	File libFile = new File(absoluteLibPath);
				    content.append("<lib dir=\"" + libFile.getParentFile().getAbsolutePath() + "\">");
				    content.append("<include name=\"" + libFile.getName() + "\" />");
				    content.append("</lib>");
				}
			} else {
				if (location.isFile()) {
					// target platform plug-ins must be included as whole JAR
				    content.append("<lib dir=\"" + location.getParentFile().getAbsolutePath() + "\">");
				    content.append("<include name=\"" + location.getName() + "\" />");
				    content.append("</lib>");
				} else {
					// TODO handle plug-in dependencies that are extracted
				}
			}
		}
	    content.append("</war>");
		
		AntTarget target = new AntTarget("package-webapp-" + plugin.getIdentifier(), content);
		return Collections.singleton(target);
	}
}
