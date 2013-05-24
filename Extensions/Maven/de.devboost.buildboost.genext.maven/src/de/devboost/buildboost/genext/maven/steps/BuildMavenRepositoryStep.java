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
package de.devboost.buildboost.genext.maven.steps;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.artifacts.CompiledPlugin;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.maven.artifacts.MavenRepositorySpec;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.BuildEventType;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.XMLContent;

public class BuildMavenRepositoryStep extends AbstractAntTargetGenerator {
	
	private IBuildContext context;
	protected File targetDir;
	protected String usernameProperty = null;
	protected String passwordProperty = null;
	private MavenRepositorySpec repositorySpec;

	public BuildMavenRepositoryStep(IBuildContext context, MavenRepositorySpec repositorySpec, File targetDir) {
		super();
		this.context = context;
		this.repositorySpec = repositorySpec;
		this.targetDir = targetDir;
	}
	
	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		EclipseUpdateSiteDeploymentSpec deploymentSpec = repositorySpec.getUpdateSite();

		if (usernameProperty == null) {
			usernameProperty = repositorySpec.getUserNameProperty();
		}
		if (passwordProperty == null) {
			passwordProperty = repositorySpec.getPasswordProperty();
		}
		
		AntTarget mavenRepositoryTarget = generateMavenRepositoryAntTarget(deploymentSpec);
		return Collections.singletonList(mavenRepositoryTarget);
	}

	protected AntTarget generateMavenRepositoryAntTarget(
			EclipseUpdateSiteDeploymentSpec deploymentSpec)
			throws BuildException {
		
		EclipseUpdateSite updateSite = deploymentSpec.getUpdateSite();
		if (updateSite == null) {
			throw new BuildException("Can't find update site for update site specification.");
		}
		
		String distDir = targetDir.getAbsolutePath() + File.separator + "dist";

		XMLContent content = new XMLContent();
		
		String repositoryID = updateSite.getIdentifier();
		String jarsDir = distDir + File.separator + "jars" + File.separator + repositoryID;
		String mavenRepositoryDir = distDir + File.separator + "maven-repositories" + File.separator + repositoryID;

		addCreateEnvironmentScript(content);
		
		content.append("<mkdir dir=\"" + jarsDir + "\" />");
		boolean deploySomething = false;
		
		Set<CompiledPlugin> pluginsToRepack = new LinkedHashSet<CompiledPlugin>();
		Map<String, String> pluginIdToVersionMap = computePluginIdToVersionMap(
				updateSite, pluginsToRepack, deploymentSpec);
		
		Set<String> includedPlugins = repositorySpec.getIncludedPlugins();
		
		Collection<Plugin> pluginsToPackage = new LinkedHashSet<Plugin>();
		Collection<PackagePluginTask> packagingTasks = new ArrayList<PackagePluginTask>();
		for (EclipseFeature feature : updateSite.getFeatures()) {
			String featureVersion = feature.getVersion();
			String featureVendor = deploymentSpec.getFeatureVendor(feature.getIdentifier());
			Collection<Plugin> plugins = feature.getPlugins();

			for (Plugin plugin : plugins) {
				String pluginID = plugin.getIdentifier();
				if (!includedPlugins.contains(pluginID)) {
					context.getBuildListener().handleBuildEvent(BuildEventType.WARNING, 
						"Skipping plug-in '" + plugin.getIdentifier() + "' as it is not listed to be included in Maven repository."
					);
					continue;
				}
				
				PackagePluginTask task = new PackagePluginTask(plugin, featureVersion, featureVendor, deploymentSpec);
				packagingTasks.add(task);
				pluginsToPackage.add(plugin);
			}
		}

		addRepackScripts(content, jarsDir, mavenRepositoryDir,
				pluginsToPackage, pluginsToRepack, pluginIdToVersionMap);
		
		for (PackagePluginTask packagingTask : packagingTasks) {
			deploySomething |= addPackageScript(packagingTask, content, jarsDir, mavenRepositoryDir,
					pluginIdToVersionMap, includedPlugins, pluginsToPackage, pluginsToRepack);
		}
		
		if (deploySomething) {
			addDeployScript(content, mavenRepositoryDir);
		}
		
		AntTarget target = new AntTarget("build-maven-repository", content);
		return target;
	}

	private void addCreateEnvironmentScript(XMLContent content) {
		content.append("<property environment=\"env\"/>");
		content.append("<!-- Get BUILD_ID from environment -->");
		content.append("<condition property=\"buildid\" value=\"${env.BUILD_ID}\">");
		content.append("<isset property=\"env.BUILD_ID\" />");
		content.append("</condition>");
		content.appendLineBreak();
	
		content.append("<condition property=\"mvn-executable\" value=\"mvn.bat\">");
		content.append("<os family=\"windows\"/>");
		content.append("</condition>");
		content.append("<condition property=\"mvn-executable\" value=\"mvn\">");
		content.append("<not>");
		content.append("<os family=\"windows\"/>");
		content.append("</not>");
		content.append("</condition>");
		content.appendLineBreak();
		
		content.append("<!-- fallback if env.BUILD_ID is not set -->");
		content.append("<tstamp/>");
		content.append("<property name=\"buildid\" value=\"${DSTAMP}${TSTAMP}\" />");
		content.appendLineBreak();
	}

	private void addDeployScript(XMLContent content, String mavenRepositoryDir) {
		String targetPath = repositorySpec.getRepositoryPath();
		if (targetPath != null) {
			content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + targetPath + "\" port=\"22\" sftp=\"true\" trust=\"true\">");
			content.append("<fileset dir=\"" + mavenRepositoryDir + "\">");
			content.append("</fileset>");
			content.append("</scp>");
		}
	}

	private void addRepackScripts(XMLContent content, String jarsDir,
			String mavenRepositoryDir, Collection<Plugin> pluginsToPackage,
			Set<CompiledPlugin> pluginsToRepack,
			Map<String, String> pluginIdToVersionMap) {
		
		for (CompiledPlugin compiledPlugin : pluginsToRepack) {
			addRepackScript(content, jarsDir, mavenRepositoryDir,
					pluginsToPackage, compiledPlugin, pluginsToRepack,
					pluginIdToVersionMap);
		}
	}
	
	private void addRepackScript(XMLContent content, String jarsDir,
			String mavenRepositoryDir, Collection<Plugin> pluginsToPackage,
			CompiledPlugin compiledPlugin,
			Set<CompiledPlugin> pluginsToRepack,
			Map<String, String> pluginIdToVersionMap) {
		
		String pluginID = compiledPlugin.getIdentifier();
		String pluginPath = compiledPlugin.getFile().getAbsolutePath();
		String pluginVersion = compiledPlugin.getVersion();
		
		//TODO We could read this from the Manifest
		String pluginName = idToName(pluginID);
		String pluginVendor = "Eclipse Modeling Project";
		
		String pomXMLContent = generatePomXML(compiledPlugin,
				pluginVersion, pluginName, pluginVendor,
				pluginIdToVersionMap, pluginsToPackage, pluginsToRepack);
		
		if (pomXMLContent == null) {
			return;
		}
		
		String pomPropertiesContent = composePomProperties(compiledPlugin, pluginVersion);
		
		String dirName = compiledPlugin.getFile().getName().replace(".jar", "");
		File pluginDirectory = new File(compiledPlugin.getFile().getParent(), dirName);
		String pomFile = writePomFile(pomXMLContent, pluginDirectory, pluginID, "xml").getAbsolutePath();
		writePomFile(pomPropertiesContent, pluginDirectory, pluginID, "properties").getAbsolutePath();

		String destBinJarFile = jarsDir + "/" + pluginID + "-" + pluginVersion + ".jar";
		
		content.append("<echo message=\"Repacking plug-in '" + pluginID  + "' for maven repository\"/>");
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
		
		addDeployBinaryToRepositoryScript(content, jarsDir, mavenRepositoryDir, pomFile, destBinJarFile);
		addDeploySourceToRepositoryScript(content, jarsDir, mavenRepositoryDir, compiledPlugin, pluginVersion, destSrcJarFile);
		
		content.appendLineBreak();
	}

	private boolean addPackageScript(PackagePluginTask packagingTask,
			XMLContent content, String jarsDir, String mavenRepositoryDir,
			Map<String, String> pluginIdToVersionMap,
			Set<String> includedPlugins, Collection<Plugin> pluginsToPackage,
			Set<CompiledPlugin> pluginsToRepack) {
		
		Plugin plugin = packagingTask.getPlugin();
		EclipseUpdateSiteDeploymentSpec deploymentSpec = packagingTask.getDeploymentSpec();
		
		String pluginID = plugin.getIdentifier();
		File pluginDirectory = plugin.getFile();
		String pluginPath = pluginDirectory.getAbsolutePath();
		
		String pluginVersion = deploymentSpec.getPluginVersion(pluginID);
		if (pluginVersion == null) {
			pluginVersion = packagingTask.getFeatureVersion();
		}
		String pluginVendor = deploymentSpec.getPluginVendor(pluginID);
		if (pluginVendor == null) {
			pluginVendor = packagingTask.getFeatureVendor();
		}
		
		String pluginName = deploymentSpec.getPluginName(pluginID);
		if (pluginName == null) {
			pluginName = plugin.getName();
		}
		if (pluginName == null) {
			pluginName = idToName(pluginID);
		}
		
		if (repositorySpec.isSnapshot()) {
			pluginVersion = pluginVersion + "-SNAPSHOT";
		}
		
		String pomXMLContent = generatePomXML(plugin, pluginVersion,
				pluginName, pluginVendor, pluginIdToVersionMap,
				pluginsToPackage, pluginsToRepack);
		if (pomXMLContent == null) {
			content.append("<echo message=\"WARNING: Can't package maven artifact '" + plugin.getIdentifier() + "'. This may lead to a broken maven repository. Scan previous log entries for errors.\"/>");
			return false;
		}
		
		String pomPropertiesContent = composePomProperties(plugin, pluginVersion);
		
		String pomFile = writePomFile(pomXMLContent, pluginDirectory, pluginID, "xml").getAbsolutePath();
		writePomFile(pomPropertiesContent, pluginDirectory, pluginID, "properties");
		
		// package plugin(s)
		String destBinJarFile = jarsDir + File.separator + pluginID + "-" + pluginVersion + ".jar";
		String destSrcJarFile = jarsDir + File.separator + pluginID + "-" + pluginVersion + "-sources.jar";

		content.append("<echo message=\"Packaging maven artifact '" + pluginID + "'\"/>");
		content.append("<jar destfile=\"" + destBinJarFile + "\">");
		content.append("<fileset dir=\"" + pluginPath + "\">");
		content.append("<exclude name=\".*\"/>");
		content.append("<exclude name=\"src*/**\"/>");
		content.append("<exclude name=\"bin/**\"/>");
		content.append("</fileset>");
		content.append("</jar>");
		content.append("<jar destfile=\"" + destSrcJarFile + "\">");
		// TODO Don't do this here!
		for (File childFolder : pluginDirectory.listFiles()) {
			 //TODO read .classpath for src-folders instead
			if (childFolder.isDirectory() && childFolder.getName().startsWith("src")) {
				content.append("<fileset dir=\"" + childFolder.getAbsolutePath() + "\"/>");
			}
		}
		content.append("</jar>");
		
		addDeployBinaryToRepositoryScript(content, jarsDir, mavenRepositoryDir, pomFile, destBinJarFile);
		addDeploySourceToRepositoryScript(content, jarsDir, mavenRepositoryDir, plugin, pluginVersion, destSrcJarFile);
		
		content.appendLineBreak();
		return true;
	}

	private Map<String, String> computePluginIdToVersionMap(
			EclipseUpdateSite updateSite, Set<CompiledPlugin> pluginsToRepack, 
			EclipseUpdateSiteDeploymentSpec deploymentSpec) {
		
		Map<String, String> pluginIdToVersionMap = new LinkedHashMap<String, String>();
		
		for (EclipseFeature feature : updateSite.getFeatures()) {
			String featureVersion = feature.getVersion();
			for (Plugin plugin : feature.getPlugins()) {
				String pluginID = plugin.getIdentifier();
				String pluginVersion = deploymentSpec.getPluginVersion(pluginID);
				if (pluginVersion == null) {
					pluginVersion = featureVersion;
				}
				pluginIdToVersionMap.put(pluginID, pluginVersion);
				findDependenciesRecursively(plugin, pluginsToRepack, pluginIdToVersionMap);
			}
		}
		return pluginIdToVersionMap;
	}

	protected void addDeployBinaryToRepositoryScript(XMLContent content,
			String jarsDir, String mavenRepositoryDir, String pomFile,
			String destBinJarFile) {
		
		content.append("<exec executable=\"${mvn-executable}\" dir=\"" + jarsDir + "\">");
		content.append("<arg value=\"deploy:deploy-file\"/>");
		content.append("<arg value=\"-Dfile=" + destBinJarFile + "\"/>");
		content.append("<arg value=\"-DpomFile=" + pomFile + "\"/>");
		addMavenRepositoryArgument(content, mavenRepositoryDir);
		content.append("</exec>");
	}
	
	private void addMavenRepositoryArgument(XMLContent content,
			String localMavenRepositoryDir) {

		content.append("<arg value=\"-Durl=file:" + localMavenRepositoryDir + "\"/>");
	}

	protected void addDeploySourceToRepositoryScript(XMLContent content,
			String jarsDir, String mavenRepositoryDir, Plugin plugin,
			String pluginVersion, String destSrcJarFile) {
		
		content.append("<exec executable=\"${mvn-executable}\" dir=\"" + jarsDir + "\">");
		content.append("<arg value=\"deploy:deploy-file\"/>");
		content.append("<arg value=\"-Dfile=" + destSrcJarFile + "\"/>");
		content.append("<arg value=\"-Dpackaging=java-source\"/>");
		content.append("<arg value=\"-DgeneratePom=false\"/>");
		content.append("<arg value=\"-DgroupId=" + getMavenGroup(plugin.getIdentifier()) + "\"/>");
		content.append("<arg value=\"-DartifactId=" + plugin.getIdentifier() + "\"/>");
		content.append("<arg value=\"-Dversion=" + pluginVersion + "\"/>");
		addMavenRepositoryArgument(content, mavenRepositoryDir);
		content.append("</exec>");
	}

	protected void findDependenciesRecursively(Plugin plugin,
			Set<CompiledPlugin> pluginsToRepack,
			Map<String, String> plugin2VersionMap) {
		
		for (IDependable dependency : plugin.getDependencies()) {
			if (dependency instanceof CompiledPlugin) {
				CompiledPlugin compiledPlugin = (CompiledPlugin) dependency;

				String identifier = compiledPlugin.getIdentifier();
				String version = compiledPlugin.getVersion();
				plugin2VersionMap.put(identifier, version);

				if (includeInMavenRepository(compiledPlugin)) {
					pluginsToRepack.add(compiledPlugin);
				}
				findDependenciesRecursively(compiledPlugin, pluginsToRepack, plugin2VersionMap);
			}
		}
	}

	protected String generatePomXML(Plugin plugin, String pluginVersion,
			String pluginName, String pluginVendor,
			Map<String, String> plugin2VersionMap,
			Collection<Plugin> pluginsToPackage,
			Set<CompiledPlugin> pluginsToRepack) {
		
		Set<String> pluginsAssumedAvailable = repositorySpec.getPluginsAssumedAvailable();
		
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
			String dependencyID = dependency.getIdentifier();
			if (!isContainedIn(pluginsToPackage, dependencyID) &&
				!isContainedIn(pluginsToRepack, dependencyID) &&
				!pluginsAssumedAvailable.contains(dependencyID)) {
				
				context.getBuildListener().handleBuildEvent(BuildEventType.ERROR, 
						"Can not create Maven artifact for " + plugin.getIdentifier() 
						+ " since " + dependencyID + " is not available nor assumed as being available in another Maven repository.");
				return null;
			}
			String dependencyVersion = plugin2VersionMap.get(dependencyID);
			content.append("<dependency>");
			content.append("<groupId>" + getMavenGroup(dependencyID) + "</groupId>");
			content.append("<artifactId>" + dependencyID + "</artifactId>");
			if (dependencyVersion != null) {
				content.append("<version>" + dependencyVersion + "</version>");
			}
			content.append("</dependency>");
		}
		
		content.append("</dependencies>");
		content.append("</project>");
		return content.toString();
	}
	
	private boolean isContainedIn(Collection<? extends AbstractArtifact> artifacts,
			String identifier) {

		for (AbstractArtifact artifact : artifacts) {
			if (artifact.getIdentifier().equals(identifier)) {
				return true;
			}
		}
		return false;
	}

	protected String composePomProperties(Plugin plugin, String pluginVersion) {
		String identifier = plugin.getIdentifier();

		StringBuilder content = new StringBuilder();
		content.append("version=" + pluginVersion + "\n");
		content.append("groupId=" + getMavenGroup(identifier) + "\n");
		content.append("artifactId=" + identifier + "\n");
		return content.toString();
	}

	// TODO This must not be done here, but in the ANT Script
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
		
		Set<String> includedPlugins = repositorySpec.getIncludedPlugins();
		if (includedPlugins.contains(compiledPlugin.getIdentifier())) {
			return true;
		} else {
			return false;
		}
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
