/*******************************************************************************
 * Copyright (c) 2006-2013
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.steps.ClasspathHelper;
import de.devboost.buildboost.util.PluginPackagingHelper;
import de.devboost.buildboost.util.XMLContent;

public class WebAppPackagingStep extends AbstractAntTargetGenerator {

	private static final String PLUGIN_ID = "de.devboost.buildboost.genext.webapps";

	private static final String WEB_CONTENT_DIR_NAME = "WebContent";
	
	private final Plugin plugin;

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
	    
	    new PluginPackagingHelper().addPackageAsJarFileScripts(content, temporaryWebAppDir, dependencies);
	    
		content.append("<war destfile=\"" + distWebAppsPath + "/" + plugin.getIdentifier() + ".war\" webxml=\"" + webXmlFile.getAbsolutePath() + "\">");
	    content.append("<lib dir=\"" + temporaryWebAppDir + "\">");
	    content.append("<include name=\"*.jar\" />");
	    content.append("</lib>");
		content.append("<fileset dir=\"" + webContentDir.getAbsolutePath() + "\" />");
	    content.append("<classes dir=\"" + new ClasspathHelper().getBinPath(plugin) + "\" />");
	    for (Plugin dependency : dependencies) {
	    	File location = dependency.getLocation();
	    	if (dependency.isProject()) {
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
				if (location.isFile()) {
					// add packaged dependency
				    //String jarFile = new PluginPackagingHelper().getJarFileName(temporaryWebAppDir, dependency);
				    content.append("<lib file=\"" + location.getAbsolutePath() + "\" />");
				} else {
					// TODO
				}
			}
	    }
	    
	    content.append("</war>");
	    
	    // TODO remove webAppDir?
		
		AntTarget target = new AntTarget("package-webapp-" + plugin.getIdentifier(), content);
		return Collections.singleton(target);
	}

	private void removeContainerLibraries(Set<Plugin> dependencies) {
		Iterator<Plugin> iterator = dependencies.iterator();
		while (iterator.hasNext()) {
			Plugin plugin = (Plugin) iterator.next();
	    	// we do not include artificial dependencies that have been only
	    	// added to compile correctly
			if (isContainerLibrary(plugin)) {
				iterator.remove();
			}
		}
	}

	private boolean isContainerLibrary(Plugin plugin) {
		File pluginFile = plugin.getLocation();
		if (!pluginFile.isDirectory()) {
			return false;
		}

		File propertiesFile = new File(pluginFile, PLUGIN_ID + ".properties");
		if (!propertiesFile.exists()) {
			return false;
		}
		
		Properties properties = new Properties();
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(propertiesFile);
			properties.load(inStream);
		} catch (IOException ioe) {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		return "true".equals(properties.getProperty("containerLibrary"));
	}
}
