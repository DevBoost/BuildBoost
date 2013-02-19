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
package de.devboost.buildboost.genext.webapps.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.XMLContent;

public class WebAppPackagingStep extends AbstractAntTargetGenerator {

	private static final String WEB_CONTENT_DIR_NAME = "WebContent";
	
	private Plugin plugin;

	public WebAppPackagingStep(Plugin plugin) {
		super();
		this.plugin = plugin;
	}

	public Collection<AntTarget> generateAntTargets() throws BuildException {
		XMLContent content = new XMLContent();

		File webContentDir = new File(plugin.getLocation(), WEB_CONTENT_DIR_NAME);
		File webXmlFile = new File(new File(webContentDir, "WEB-INF"), "web.xml");
		
		// this is the directory where we copy all the dependencies of the
		// web application before actually creating the WAR file
		String temporaryWebAppDir = "temp/webapps/" + plugin.getIdentifier();
		
		String distWebAppsPath = "dist/webapps";
		content.append("<mkdir dir=\"" + distWebAppsPath + "\" />");
		content.append("<mkdir dir=\"" + temporaryWebAppDir + "\" />");
		
	    Set<Plugin> dependencies = plugin.getAllDependencies();
	    removeContainerLibraries(dependencies);
	    
	    for (Plugin dependency : dependencies) {
			// each project the WebApp depends on is packaged as individual JAR
			// file
			if (dependency.isProject()) {
			    String jarFile = getJarFileName(temporaryWebAppDir, dependency);
			    String binPath = new ClasspathHelper().getBinPath(dependency);
			    
				content.append("<jar destfile=\"" + jarFile + "\">"); 
				content.append("<fileset dir=\"" + binPath + "\" />");
			    content.append("<fileset dir=\"" + dependency.getAbsolutePath() + "\" >");
			    content.append("<include name=\"metamodel/**\" />");
			    content.append("<include name=\"META-INF/**\" />");
			    content.append("</fileset>");
			    content.append("</jar>");
			} else {
				// TODO?
			}
		}
		content.append("<war destfile=\"" + distWebAppsPath + "/" + plugin.getIdentifier() + ".war\" webxml=\"" + webXmlFile.getAbsolutePath() + "\">");
	    content.append("<lib dir=\"" + temporaryWebAppDir + "\">");
	    content.append("<include name=\"*.jar\" />");
	    content.append("</lib>");
		content.append("<fileset dir=\"" + webContentDir.getAbsolutePath() + "\" />");
	    content.append("<classes dir=\"" + new ClasspathHelper().getBinPath(plugin) + "\" />");
	    for (Plugin dependency : dependencies) {
	    	if (dependency.isProject()) {
		    	File location = dependency.getLocation();
				if (location.isFile()) {
					// target platform plug-ins must be included as whole JAR
				    content.append("<lib dir=\"" + location.getParentFile().getAbsolutePath() + "\">");
				    content.append("<include name=\"" + location.getName() + "\" />");
				    content.append("</lib>");
				} else {
				    // add the libraries that are part of the dependency
				    Set<String> libs = dependency.getLibs();
					for (String lib : libs) {
					    content.append("<lib file=\"" + dependency.getAbsoluteLibPath(lib) + "\" />");
					}

					// TODO handle plug-in dependencies that are extracted
				}
			} else {
				// add packaged dependency
			    String jarFile = getJarFileName(temporaryWebAppDir, dependency);
			    content.append("<lib file=\"" + jarFile + "\" />");
			}
	    }
	    
	    content.append("</war>");
	    
	    // TODO remove webAppDir?
		
		AntTarget target = new AntTarget("package-webapp-" + plugin.getIdentifier(), content);
		return Collections.singleton(target);
	}

	private String getJarFileName(String webAppDir, Plugin dependency) {
		return webAppDir + "/" + dependency.getIdentifier() + ".jar";
	}

	private void removeContainerLibraries(Set<Plugin> dependencies) {
		Iterator<Plugin> iterator = dependencies.iterator();
		while (iterator.hasNext()) {
			Plugin plugin = (Plugin) iterator.next();
	    	// we do not include artificial dependencies that have been only
	    	// added to compile correctly
			if (plugin.getIdentifier().startsWith("javax.servlet")) {
				iterator.remove();
			}
		}
	}
}
