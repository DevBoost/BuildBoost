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
package de.devboost.buildboost.genext.maven.steps;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.XMLContent;

public class BuildMavenRepositoryStep extends AbstractAntTargetGenerator {
	
	protected EclipseUpdateSiteDeploymentSpec updateSiteSpec;
	protected File targetDir;
	protected String usernameProperty = null;
	protected String passwordProperty = null;

	public BuildMavenRepositoryStep(EclipseUpdateSiteDeploymentSpec updateSiteSpec, File targetDir) {
		super();
		this.updateSiteSpec = updateSiteSpec;
		this.targetDir = targetDir;
	}
	
	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		if (usernameProperty == null) {
			usernameProperty = updateSiteSpec.getValue("site", "usernameProperty");
			System.out.println("Using user: " + usernameProperty);
		}
		if (passwordProperty == null) {
			passwordProperty = updateSiteSpec.getValue("site", "passwordProperty");
			System.out.println("Using password: " + passwordProperty);
		}
		
		AntTarget mavenRepositoryTarget = generateMavenRepositoryAntTarget();
		return Collections.singletonList(mavenRepositoryTarget);
	}

	protected AntTarget generateMavenRepositoryAntTarget() throws BuildException {
		EclipseUpdateSite updateSite = updateSiteSpec.getUpdateSite();
		if (updateSite == null) {
			throw new BuildException("Can't find update site for update site specification.");
		}
		
		String distDir = targetDir.getAbsolutePath() + File.separator + "dist";

		XMLContent content = new XMLContent();
		
		String repositoryID = updateSite.getIdentifier();
		String jarsDir = distDir + File.separator + "jars" + File.separator + repositoryID;
		String mavenRespoitoryDir = distDir + File.separator + "maven-repository" + File.separator + repositoryID;

		content.append("<property environment=\"env\"/>");
		content.append("<!-- Get BUILD_ID from environment -->");
		content.append("<condition property=\"buildid\" value=\"${env.BUILD_ID}\">");
		content.append("<isset property=\"env.BUILD_ID\" />");
		content.append("</condition>");
		content.appendLineBreak();
	
		content.append("<!-- fallback if env.BUILD_ID is not set -->");
		content.append("<tstamp/>");
		content.append("<property name=\"buildid\" value=\"${DSTAMP}${TSTAMP}\" />");
		content.appendLineBreak();
		
		content.append("<mkdir dir=\"" + jarsDir + "\" />");
		boolean deployedSomething = false;
		
		Set<CompiledPlugin> pluginsToRepack = new LinkedHashSet<CompiledPlugin>();
		Map<String, String> plugin2VersionMap = new LinkedHashMap<String, String>();
		for (EclipseFeature feature : updateSite.getFeatures()) {
			String featureVersion = feature.getVersion();
			for (Plugin plugin : feature.getPlugins()) {
				String pluginID = plugin.getIdentifier();
				String pluginVersion = updateSiteSpec.getValue("plugin", pluginID, "version");
				if (pluginVersion == null) {
					pluginVersion = featureVersion;
				}
				plugin2VersionMap.put(pluginID, pluginVersion);
				addDependenciesRecursively(plugin, pluginsToRepack, plugin2VersionMap);
			}
		}
		
		for (EclipseFeature feature : updateSite.getFeatures()) {
			String featureVersion = feature.getVersion();
			String featureVendor = updateSiteSpec.getFeatureVendor(feature.getIdentifier());
			for (Plugin plugin : feature.getPlugins()) {
				String pluginID = plugin.getIdentifier();
				File pluginDirectory = plugin.getFile();
				String pluginPath = pluginDirectory.getAbsolutePath();
				
				String pluginVersion = updateSiteSpec.getValue("plugin", pluginID, "version");
				if (pluginVersion == null) {
					pluginVersion = featureVersion;
				}
				String pluginVendor = updateSiteSpec.getValue("plugin", pluginID, "vendor");
				if (pluginVendor == null) {
					pluginVendor = featureVendor;
				}
				String pluginName = updateSiteSpec.getValue("plugin", pluginID, "name");
				if (pluginName == null) {
					pluginName = idToName(pluginID);
				}
				
				String snapshotValue = updateSiteSpec.getValue("site", "snapshot");
				boolean snapshot = true;
				if (snapshotValue != null) {
					snapshot = Boolean.parseBoolean(snapshotValue);
				}
				
				if (snapshot) {
					pluginVersion = pluginVersion + "-SNAPSHOT";
				}
				
				String pomXMLContent = generatePomXML(plugin, pluginVersion, pluginName, pluginVendor, plugin2VersionMap);
				if (pomXMLContent == null) {
					continue;
				}
				String pomPropertiesContent = generatePomProperties(plugin, pluginVersion);
				
				String pomFile = writePomFile(pomXMLContent, pluginDirectory, pluginID, "xml").getAbsolutePath();
				writePomFile(pomPropertiesContent, pluginDirectory, pluginID, "properties");
				
				// package plugin(s)
				content.append("<echo message=\"Packaging maven artifact '" + pluginID + "'\"/>");
				String destBinJarFile = jarsDir + "/" + pluginID + "-" + pluginVersion + ".jar";
				content.append("<jar destfile=\"" + destBinJarFile + "\">");
				content.append("<fileset dir=\"" + pluginPath + "\">");
				content.append("<exclude name=\".*\"/>");
				content.append("<exclude name=\"src*/**\"/>");
				content.append("<exclude name=\"bin/**\"/>");
				content.append("</fileset>");
				content.append("</jar>");
				String destSrcJarFile = jarsDir + "/" + pluginID + "-" + pluginVersion + "-sources.jar";
				content.append("<jar destfile=\"" + destSrcJarFile + "\">");
				for (File childFolder : pluginDirectory.listFiles()) {
					 //TODO read .classpath for src-folders instead
					if (childFolder.isDirectory() && childFolder.getName().startsWith("src")) {
						content.append("<fileset dir=\"" + childFolder.getAbsolutePath() + "\"/>");
					}
				}
				content.append("</jar>");
				
				deployBinInRepository(content, jarsDir, mavenRespoitoryDir, pomFile, destBinJarFile);
				deploySrcInRepository(content, jarsDir, mavenRespoitoryDir, plugin, pluginVersion, destSrcJarFile);
				
				content.appendLineBreak();
				
				deployedSomething = true;
			}
		}
		
		for (CompiledPlugin compiledPlugin : pluginsToRepack) {
			String pluginID = compiledPlugin.getIdentifier();
			String pluginPath = compiledPlugin.getFile().getAbsolutePath();
			String pluginVersion = compiledPlugin.getVersion();
			
			//TODO we could read this from plugin.properties
			String pluginName = idToName(pluginID);
			String pluginVendor = "Eclipse Modeling Project";
			
			String pomXMLContent = generatePomXML(compiledPlugin, pluginVersion, pluginName, pluginVendor, plugin2VersionMap);
			if (pomXMLContent == null) {
				continue;
			}
			String pomPropertiesContent = generatePomProperties(compiledPlugin, pluginVersion);
			
			String dirName = compiledPlugin.getFile().getName().replace(".jar", "");
			File pluginDirectory = new File(compiledPlugin.getFile().getParent(), dirName);
			String pomFile = writePomFile(pomXMLContent, pluginDirectory, pluginID, "xml").getAbsolutePath();
			writePomFile(pomPropertiesContent, pluginDirectory, pluginID, "properties").getAbsolutePath();
			
			content.append("<echo message=\"Repacking for maven repository '" + pluginID  + "'\"/>");
			String destBinJarFile = jarsDir + "/" + pluginID + "-" + pluginVersion + ".jar";
			content.append("<jar destfile=\"" + destBinJarFile + "\">");
			content.append("<zipfileset src=\"" + pluginPath + "\">");	
			content.append("<exclude name=\"META-INF/**\"/>");
			content.append("</zipfileset>");
			content.append("<fileset dir=\"" + pluginDirectory.getAbsolutePath() + "\"/>");
			content.append("</jar>");
			
			//src version
			String srcPluginFile = compiledPlugin.getFile().getName().replace(pluginID, pluginID + ".source");
			pluginPath = new File(compiledPlugin.getFile().getParent(), srcPluginFile).getAbsolutePath();
			String destSrcJarFile = jarsDir + "/" + pluginID + "-" + pluginVersion + "-sources.jar";
			content.append("<jar destfile=\"" + destSrcJarFile + "\">");
			content.append("<zipfileset src=\"" + pluginPath + "\">");	
			content.append("<exclude name=\"META-INF/**\"/>");
			content.append("</zipfileset>");
			content.append("</jar>");
			
			deployBinInRepository(content, jarsDir, mavenRespoitoryDir, pomFile, destBinJarFile);
			deploySrcInRepository(content, jarsDir, mavenRespoitoryDir, compiledPlugin, pluginVersion, destSrcJarFile);
			
			content.appendLineBreak();
		}
		
		if (deployedSomething) {
			String targetPath = updateSiteSpec.getValue("site", "uploadPath");
			if (targetPath != null) {
				String repoPath = targetPath.substring(0, targetPath.lastIndexOf('/') + 1) + "maven-repository" ;
				
				content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + repoPath + "\" port=\"22\" sftp=\"true\" trust=\"true\">");
				content.append("<fileset dir=\"" + mavenRespoitoryDir + "\">");
				content.append("</fileset>");
				content.append("</scp>");
			}
		}
		
		AntTarget target = new AntTarget("build-maven-repository", content);
		return target;
	}

	protected void deployBinInRepository(XMLContent content, String jarsDir,
			String mavenRespoitoryDir, String pomFile, String destBinJarFile) {
		
		content.append("<exec executable=\"mvn\" dir=\"" + jarsDir + "\">");
		content.append("<arg value=\"deploy:deploy-file\"/>");
		content.append("<arg value=\"-Dfile=" + destBinJarFile + "\"/>");
		content.append("<arg value=\"-DpomFile=" + pomFile + "\"/>");
		content.append("<arg value=\"-Durl=file:" + mavenRespoitoryDir + "\"/>");
		content.append("</exec>");
	}
	
	protected void deploySrcInRepository(XMLContent content, String jarsDir,
			String mavenRespoitoryDir, Plugin plugin, String pluginVersion, String destSrcJarFile) {
		
		content.append("<exec executable=\"mvn\" dir=\"" + jarsDir + "\">");
		content.append("<arg value=\"deploy:deploy-file\"/>");
		content.append("<arg value=\"-Dfile=" + destSrcJarFile + "\"/>");
		content.append("<arg value=\"-Dpackaging=java-source\"/>");
		content.append("<arg value=\"-DgeneratePom=false\"/>");
		content.append("<arg value=\"-DgroupId=" + getMavenGroup(plugin.getIdentifier()) + "\"/>");
		content.append("<arg value=\"-DartifactId=" + plugin.getIdentifier() + "\"/>");
		content.append("<arg value=\"-Dversion=" + pluginVersion + "\"/>");
		content.append("<arg value=\"-Durl=file:" + mavenRespoitoryDir + "\"/>");
		content.append("</exec>");
	}

	protected void addDependenciesRecursively(Plugin plugin,
			Set<CompiledPlugin> pluginsToRepack, Map<String, String> plugin2VersionMap) {
		for (IDependable dependency : plugin.getDependencies()) {
			if (dependency instanceof CompiledPlugin) {
				CompiledPlugin compiledPlugin = (CompiledPlugin) dependency;
				if (includeInMavenRepository(compiledPlugin)) {
					plugin2VersionMap.put(
							compiledPlugin.getIdentifier(), compiledPlugin.getVersion());
					pluginsToRepack.add(compiledPlugin);
					addDependenciesRecursively(compiledPlugin, pluginsToRepack, plugin2VersionMap);
				}
			}
		}
	}

	protected String generatePomXML(Plugin plugin, String pluginVersion, 
			String pluginName, String pluginVendor, Map<String, String> plugin2VersionMap) {
		XMLContent content = new XMLContent();
		
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		content.append("<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"" +
				" xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		content.append("<modelVersion>4.0.0</modelVersion>");
		content.append("<groupId>" + getMavenGroup(plugin.getIdentifier()) + "</groupId>");
		content.append("<artifactId>" + plugin.getIdentifier() + "</artifactId>");
		content.append("<version>" + pluginVersion + "</version>");
		content.append("<name>" + pluginName + "</name>");
		content.append("<organization>");
		content.append("<name>" + pluginVendor + "</name>");
		content.append("</organization>");
		content.append("<licenses>");
		content.append("<license>");
		content.append("<name>Eclipse Public License - v 1.0</name>");
		content.append("<url>http://www.eclipse.org/org/documents/epl-v10.html</url>");
		content.append("</license>");
		content.append("</licenses>");
		content.append("<dependencies>");
		for (UnresolvedDependency dependency : plugin.getResolvedDependencies()) {
			if (isOptional(dependency)) {
				continue;
			}
			String dependencyVersion = plugin2VersionMap.get(dependency.getIdentifier());
			if (dependencyVersion == null) {
				System.out.println("Can not create maven artifact for " + plugin.getIdentifier() 
						+ " since " + dependency.getIdentifier() + " is not versioned");
				return null;
			}
			content.append("<dependency>");
			content.append("<groupId>" + getMavenGroup(dependency.getIdentifier()) + "</groupId>");
			content.append("<artifactId>" + dependency.getIdentifier() + "</artifactId>");
			content.append("<version>" + dependencyVersion + "</version>");
			content.append("</dependency>");
		}
		content.append("</dependencies>");
		content.append("</project>");
		return content.toString();
	}
	
	protected String generatePomProperties(Plugin plugin, String pluginVersion) {
		String content = "";
		content += "version=" + pluginVersion + "\n";
		content += "groupId=" + getMavenGroup(plugin.getIdentifier()) + "\n";
		content += "artifactId=" + plugin.getIdentifier() + "\n";
		return content;
	}

	protected File writePomFile(String pomXMLContent, File pluginDirectory, String pluginID, String extension) {
		File pomFolder = new File(pluginDirectory.getAbsolutePath() + File.separator + "META-INF"
				+ File.separator + "maven" + File.separator + getMavenGroup(pluginID)
				+ File.separator + pluginID);
		pomFolder.mkdirs();
		File pomXMLFile = new File(pomFolder, "pom." + extension);
		
		try {
			new FileOutputStream(pomXMLFile).write(pomXMLContent.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pomXMLFile;
	}

	protected String getMavenGroup(String identifier) {
		String groupID = "";
		String[] idSegments = identifier.split("\\.");
		for (int i = 0; i < idSegments.length && i < 3; i++) {
			if ("".equals(groupID)) {
				groupID = idSegments[i];
			} else {
				groupID = groupID + "." + idSegments[i];
			}
		}
		return groupID;
	}
	
	protected boolean isOptional(UnresolvedDependency dependency) {
		if (dependency.isOptional()) {
			return true;
		}
		//This dependency is not marked optional in EMF plugins although it is optional.
		//See also: https://bugs.eclipse.org/bugs/show_bug.cgi?id=328227
		if (dependency.getIdentifier().equals("org.eclipse.core.runtime")) {
			return true;
		}
		return false;
	}

	protected boolean includeInMavenRepository(CompiledPlugin compiledPlugin) {
		// TODO This selects the EMF core plugins, could be moved to an external spec
		String id = compiledPlugin.getIdentifier();
		if (id.equals("org.eclipse.emf.common")) {
			return true;
		}
		if (id.equals("org.eclipse.emf.ecore")) {
			return true;
		}
		if (id.equals("org.eclipse.emf.ecore.change")) {
			return true;
		}
		if (id.equals("org.eclipse.emf.xmi")) {
			return true;
		}
		return false;
	}

	protected String idToName(String pluginID) {
		String name = "";
		String[] segements = pluginID.split("\\.");
		for (int i = 1; i < segements.length; i++) {
			String s = segements[i];
			name = name + firstToUpper(s) + " ";
		}
		if ("".equals(name)) {
			return pluginID;
		}
		return name.trim();
	}

	private String firstToUpper(String s) {
		return s.substring(0,1).toUpperCase() + s.substring(1);
	}


}
