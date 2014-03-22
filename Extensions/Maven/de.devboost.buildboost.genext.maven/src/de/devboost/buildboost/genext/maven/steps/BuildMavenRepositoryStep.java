/*******************************************************************************
 * Copyright (c) 2006-2014
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.IConstants;
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
import de.devboost.buildboost.model.IBuildListener;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.model.ResolvedDependency;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link BuildMavenRepositoryStep} creates two Maven repositories based on
 * the configuration that is found in a <code>maven-repository.properties</code>
 * file. It creates both a snapshot and a release repository as the decision
 * about whether the build will yield a release will be done after the build and
 * not before. The snapshot repository is uploaded using SCP.
 * 
 * Both repositories are zipped and stored in the <code>dist</code> folder to
 * let Jenkins/Hudson archive them.
 */
public class BuildMavenRepositoryStep extends AbstractAntTargetGenerator {
	
	private static final String MAVEN_REPOSITORIES = "maven-repositories";
	private static final String SNAPSHOT_SUFFIX = "-snapshot";
	private static final String SNAPSHOT_SUFFIX_UPPER = SNAPSHOT_SUFFIX.toUpperCase();
	
	private final IBuildContext context;
	private final File artifactsFolder;
	private final MavenRepositorySpec repositorySpec;
	private final EclipseUpdateSiteDeploymentSpec updateSiteDeploymentSpec;
	private final EclipseUpdateSite updateSite;

	public BuildMavenRepositoryStep(IBuildContext context,
			MavenRepositorySpec repositorySpec, File artifactsFolder) {
		super();
		this.context = context;
		this.repositorySpec = repositorySpec;
		this.artifactsFolder = artifactsFolder;
		this.updateSiteDeploymentSpec = repositorySpec.getUpdateSite();
		this.updateSite = updateSiteDeploymentSpec.getUpdateSite();
	}
	
	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		if (updateSite == null) {
			throw new BuildException("Can't find update site for update site deployment specification (required by to build Maven repository).");
		}
		
		AntTarget mavenSnapshotRepositoryTarget = generateMavenRepositoryAntTarget(true);
		AntTarget mavenReleaseRepositoryTarget = generateMavenRepositoryAntTarget(false);
		
		Collection<AntTarget> targets = new ArrayList<AntTarget>(2);
		targets.add(mavenSnapshotRepositoryTarget);
		targets.add(mavenReleaseRepositoryTarget);
		return targets;
	}

	private AntTarget generateMavenRepositoryAntTarget(boolean buildSnapshot)
			throws BuildException {

		XMLContent content = new XMLContent();

		String tempDir = getTempPath();

		String repositoryID = updateSite.getIdentifier();
		String tempJarsDir = tempDir + File.separator + "maven-jars" + File.separator + repositoryID;
		String mavenRepositoryDir = getMavenRepositoryPath(buildSnapshot);

		addCreateEnvironmentScript(content);
		
		content.append("<mkdir dir=\"" + tempJarsDir + "\" />");
		boolean packagedSomething = false;
		
		Set<String> includedPlugins = repositorySpec.getIncludedPlugins();
		
		Set<CompiledPlugin> pluginsToRepack = new LinkedHashSet<CompiledPlugin>();
		Map<String, String> pluginIdToVersionMap = computePluginIdToVersionMap(
				pluginsToRepack, includedPlugins, buildSnapshot);
		
		Collection<Plugin> pluginsToPackage = new LinkedHashSet<Plugin>();
		Collection<PackagePluginTask> packagingTasks = new ArrayList<PackagePluginTask>();
		for (EclipseFeature feature : updateSite.getFeatures()) {
			collectionPluginsFromFeature(feature, includedPlugins,
					pluginsToPackage, packagingTasks);
		}

		addRepackScripts(content, tempJarsDir, mavenRepositoryDir,
				pluginsToPackage, pluginsToRepack, pluginIdToVersionMap,
				buildSnapshot);
		
		for (PackagePluginTask packagingTask : packagingTasks) {
			packagedSomething |= addPackageScript(packagingTask, content,
					tempJarsDir, mavenRepositoryDir, pluginIdToVersionMap,
					includedPlugins, pluginsToPackage, pluginsToRepack,
					buildSnapshot);
		}
		
		if (packagedSomething && buildSnapshot) {
			// We do only upload the repository if it contains something and if
			// it is a snapshot repository. Release repositories must be
			// uploaded manually.
			addUploadScript(content, mavenRepositoryDir);
		}
		
		addZipMavenRepositoryScript(content, buildSnapshot);
		
		AntTarget target = new AntTarget("build-" + (buildSnapshot ? "snapshot-" : "") + "maven-repository-" + repositoryID, content);
		return target;
	}

	private void collectionPluginsFromFeature(EclipseFeature feature,
			Set<String> includedPlugins, Collection<Plugin> pluginsToPackage,
			Collection<PackagePluginTask> packagingTasks) {
		
		String featureVersion = feature.getVersion();
		String featureID = feature.getIdentifier();
		String featureVendor = updateSiteDeploymentSpec.getFeatureVendor(featureID);

		Collection<Plugin> plugins = feature.getRequiredPlugins();
		for (Plugin plugin : plugins) {
			if (plugin instanceof CompiledPlugin) {
				// We skip compiled plug-ins (i.e., plug-in which already exist
				// as JAR file) here, because the will not be packaged, but re-
				// packed instead.
				continue;
			}
			
			String pluginID = plugin.getIdentifier();
			if (!includedPlugins.contains(pluginID)) {
				context.getBuildListener().handleBuildEvent(BuildEventType.WARNING, 
					"Skipping plug-in '" + plugin.getIdentifier() + "' as it is not listed to be included in Maven repository."
				);
				continue;
			}
			
			PackagePluginTask task = new PackagePluginTask(plugin, featureVersion, featureVendor, updateSiteDeploymentSpec);
			packagingTasks.add(task);
			pluginsToPackage.add(plugin);
		}
		
		Collection<EclipseFeature> requiredFeatures = feature.getRequiredFeatures();
		for (EclipseFeature requiredFeature : requiredFeatures) {
			collectionPluginsFromFeature(requiredFeature, includedPlugins,
					pluginsToPackage, packagingTasks);
		}
	}

	private String getMavenRepositoryPath(boolean buildSnapshot) {

		String tempDir = getTempPath();
		String tempMavenRepositoryDir = tempDir + File.separator
				+ MAVEN_REPOSITORIES + File.separator
				+ getRepositoryDirName(buildSnapshot);

		return tempMavenRepositoryDir;
	}

	private String getRepositoryDirName(boolean buildSnapshot) {
		String mavenRepositoryDirName = getMavenRepositoryID();
		if (buildSnapshot) {
			mavenRepositoryDirName += SNAPSHOT_SUFFIX;
		}
		return mavenRepositoryDirName;
	}

	private String getMavenRepositoryID() {
		EclipseUpdateSiteDeploymentSpec deploymentSpec = repositorySpec.getUpdateSite();
		EclipseUpdateSite updateSite = deploymentSpec.getUpdateSite();
		String repositoryID = updateSite.getIdentifier();
		return repositoryID;
	}

	private String getTempPath() {
		return artifactsFolder.getAbsolutePath() + File.separator + "temp";
	}

	private void addZipMavenRepositoryScript(XMLContent content, boolean buildSnapshot) {
		File distFolder = new File(artifactsFolder.getParent(), IConstants.DIST_FOLDER);
		File distMavenFolder = new File(distFolder.getAbsolutePath(), MAVEN_REPOSITORIES);
		File targetFile = new File(distMavenFolder, getRepositoryDirName(buildSnapshot) + ".zip");
		String repositoryDir = getMavenRepositoryPath(buildSnapshot);
		content.append("<mkdir dir=\"" + distFolder + "\" />");
		content.append("<zip destfile=\"" + targetFile + "\" basedir=\"" + repositoryDir + "\" />");
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

	private void addUploadScript(XMLContent content, String mavenRepositoryDir) {
		IBuildListener buildListener = context.getBuildListener();
		
		String usernameProperty = repositorySpec.getUserNameProperty();
		if (usernameProperty == null) {
			String message = "Maven repository specification does not contain username property (required to upload repository).";
			buildListener.handleBuildEvent(BuildEventType.ERROR, message);
		}
		
		String passwordProperty = repositorySpec.getPasswordProperty();
		if (passwordProperty == null) {
			String message = "Maven repository specification does not contain password property (required to upload repository).";
			buildListener.handleBuildEvent(BuildEventType.ERROR, message);
		}
		
		String targetPath = repositorySpec.getRepositoryPath();
		if (targetPath == null) {
			String message = "Maven repository specification does not contain target path (required to upload repository).";
			buildListener.handleBuildEvent(BuildEventType.ERROR, message);
		}
		
		if (usernameProperty != null && passwordProperty != null && targetPath != null) {
			content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + targetPath + "\" port=\"22\" sftp=\"true\" trust=\"true\">");
			content.append("<fileset dir=\"" + mavenRepositoryDir + "\">");
			content.append("</fileset>");
			content.append("</scp>");
		} else {
			content.append("<echo message=\"Skipping upload of maven repository because credentials or target path are missing.\"/>");
		}
	}

	private void addRepackScripts(XMLContent content, String jarsDir,
			String mavenRepositoryDir, Collection<Plugin> pluginsToPackage,
			Set<CompiledPlugin> pluginsToRepack,
			Map<String, String> pluginIdToVersionMap, boolean buildSnapshot)
			throws BuildException {
		
		for (CompiledPlugin pluginToRepack : pluginsToRepack) {
			addRepackScript(content, jarsDir, mavenRepositoryDir,
					pluginsToPackage, pluginToRepack, pluginsToRepack,
					pluginIdToVersionMap, buildSnapshot);
		}
	}
	
	private void addRepackScript(XMLContent content, String jarsDir,
			String mavenRepositoryDir, Collection<Plugin> pluginsToPackage,
			CompiledPlugin compiledPlugin, Set<CompiledPlugin> pluginsToRepack,
			Map<String, String> pluginIdToVersionMap, boolean buildSnapshot)
			throws BuildException {
		
		String pluginID = compiledPlugin.getIdentifier();
		String pluginPath = compiledPlugin.getFile().getAbsolutePath();
		String pluginVersion = compiledPlugin.getVersion();
		String pluginName = getName(compiledPlugin);
		
		// TODO
		String pluginVendor = "Eclipse Modeling Project";
		
		String pomXMLContent = generatePomXML(compiledPlugin,
				pluginVersion, pluginName, pluginVendor,
				pluginIdToVersionMap, pluginsToPackage, pluginsToRepack);
		
		if (pomXMLContent == null) {
			return;
		}
		
		String pomPropertiesContent = composePomProperties(compiledPlugin, pluginVersion);
		String pomFile = writeFile(pomXMLContent, pluginID, "xml", buildSnapshot).getAbsolutePath();
		writeFile(pomPropertiesContent, pluginID, "properties", buildSnapshot).getAbsolutePath();

		String destBinJarFile = jarsDir + File.separator + pluginID + "-" + pluginVersion + ".jar";
		
		// Repackage binary version
		content.append("<echo message=\"Repacking plug-in '" + pluginID  + "' for Maven repository\"/>");
		content.append("<jar destfile=\"" + destBinJarFile + "\">");
		content.append("<zipfileset src=\"" + pluginPath + "\">");	
		content.append("<exclude name=\"META-INF/**\"/>");
		content.append("</zipfileset>");
		content.append("<fileset dir=\"" + getTemporaryFolderForPOM(pluginID, buildSnapshot) + "\"/>");
		content.append("</jar>");
		
		// Repackage source version
		String srcPluginPath = compiledPlugin.getFile().getName().replace(pluginID, pluginID + ".source");
		File srcPluginFile = new File(compiledPlugin.getFile().getParent(), srcPluginPath);
		pluginPath = srcPluginFile.getAbsolutePath();
		String destSrcJarFile = jarsDir + File.separator + pluginID + "-" + pluginVersion + "-sources.jar";
		
		content.append("<jar destfile=\"" + destSrcJarFile + "\">");
		// If there is not source JAR, we create an empty JAR
		content.append("<zipfileset src=\"" + pluginPath + "\" erroronmissingarchive=\"false\">");
		content.append("<exclude name=\"META-INF/**\"/>");
		content.append("</zipfileset>");
		content.append("</jar>");
		
		addDeployJarsToLocalRepositoryScript(content, jarsDir, mavenRepositoryDir, pomFile, destBinJarFile, destSrcJarFile);
		//addDeploySourceJarToLocalRepositoryScript(content, jarsDir, mavenRepositoryDir, compiledPlugin, pluginVersion, destSrcJarFile);
		
		content.appendLineBreak();
	}

	private String getName(CompiledPlugin compiledPlugin) {
		String name = compiledPlugin.getName();
		
		// Currently we do not read the plugin.properties file to obtain the
		// name of plug-in (if it is defined using a placeholder instead of
		// specifying it directly in the manifest). Therefore some artificial
		// name is create from the plug-in ID.
		if (name == null || name.startsWith("%")) {
			name = idToName(compiledPlugin.getIdentifier());
		}
		return name;
	}

	private boolean addPackageScript(PackagePluginTask packagingTask,
			XMLContent content, String jarsDir, String mavenRepositoryDir,
			Map<String, String> pluginIdToVersionMap,
			Set<String> includedPlugins, Collection<Plugin> pluginsToPackage,
			Set<CompiledPlugin> pluginsToRepack, boolean buildSnapshot)
			throws BuildException {
		
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
		/*
		if (pluginName == null) {
			pluginName = idToName(pluginID);
		}
		*/
		
		if (buildSnapshot) {
			pluginVersion = pluginVersion + SNAPSHOT_SUFFIX_UPPER;
		}
		
		String pomXMLContent = generatePomXML(plugin, pluginVersion,
				pluginName, pluginVendor, pluginIdToVersionMap,
				pluginsToPackage, pluginsToRepack);
		if (pomXMLContent == null) {
			content.append("<echo message=\"WARNING: Can't package maven artifact '" + plugin.getIdentifier() + "'. This may lead to a broken maven repository. Scan previous log entries for errors.\"/>");
			return false;
		}
		
		String pomPropertiesContent = composePomProperties(plugin, pluginVersion);
		
		String pomFile = writeFile(pomXMLContent, pluginID, "xml", buildSnapshot).getAbsolutePath();
		writeFile(pomPropertiesContent, pluginID, "properties", buildSnapshot);
		
		// Package plug-in(s)
		String destBinJarFile = jarsDir + File.separator + pluginID + "-" + pluginVersion + ".jar";
		String destSrcJarFile = jarsDir + File.separator + pluginID + "-" + pluginVersion + "-sources.jar";
		File pomFolder = getTemporaryFolderForPOM(pluginID, buildSnapshot);

		content.append("<echo message=\"Packaging maven artifact '" + pluginID + "'\"/>");
		content.append("<jar destfile=\"" + destBinJarFile + "\">");
		content.append("<fileset dir=\"" + pluginPath + "\">");
		content.append("<exclude name=\".*\"/>");
		content.append("<exclude name=\"src*/**\"/>");
		content.append("<exclude name=\"bin/**\"/>");
		content.append("</fileset>");
		// Add generated POM to JAR file
		content.append("<fileset dir=\"" + pomFolder.getAbsolutePath() + "\">");
		content.append("</fileset>");
		content.append("</jar>");
		content.append("<jar destfile=\"" + destSrcJarFile + "\">");
		for (File sourceFolder : plugin.getSourceFolders()) {
			content.append("<fileset dir=\"" + sourceFolder.getAbsolutePath() + "\"/>");
		}
		content.append("</jar>");
		
		addDeployJarsToLocalRepositoryScript(content, jarsDir, mavenRepositoryDir, pomFile, destBinJarFile, destSrcJarFile);
		//addDeploySourceJarToLocalRepositoryScript(content, jarsDir, mavenRepositoryDir, plugin, pluginVersion, destSrcJarFile);
		
		content.appendLineBreak();
		return true;
	}

	private Map<String, String> computePluginIdToVersionMap(
			Set<CompiledPlugin> pluginsToRepack,
			Collection<String> includedPlugins,
			boolean buildSnapshot) {
		
		Map<String, String> pluginIdToVersionMap = new LinkedHashMap<String, String>();
		
		for (EclipseFeature feature : updateSite.getFeatures()) {
			computePluginIdToVersionMap(pluginsToRepack, includedPlugins,
					buildSnapshot, pluginIdToVersionMap, feature);
		}
		return pluginIdToVersionMap;
	}

	private void computePluginIdToVersionMap(
			Set<CompiledPlugin> pluginsToRepack,
			Collection<String> includedPlugins, boolean buildSnapshot,
			Map<String, String> pluginIdToVersionMap, EclipseFeature feature) {
		
		String featureVersion = feature.getVersion();
		for (Plugin requiredPlugin : feature.getRequiredPlugins()) {
			String pluginID = requiredPlugin.getIdentifier();
			if (!includedPlugins.contains(pluginID)) {
				continue;
			}
			
			String pluginVersion = updateSiteDeploymentSpec.getPluginVersion(pluginID);
			if (pluginVersion == null) {
				pluginVersion = featureVersion;
			}
			if (buildSnapshot) {
				pluginVersion = pluginVersion + SNAPSHOT_SUFFIX_UPPER;
			}
			pluginIdToVersionMap.put(pluginID, pluginVersion);
			findDependenciesRecursively(requiredPlugin, pluginsToRepack,
					pluginIdToVersionMap, buildSnapshot);
		}
		
		for (EclipseFeature requiredFeature : feature.getRequiredFeatures()) {
			computePluginIdToVersionMap(pluginsToRepack, includedPlugins,
					buildSnapshot, pluginIdToVersionMap, requiredFeature);
		}
	}

	protected void addDeployJarsToLocalRepositoryScript(XMLContent content,
			String jarsDir, String mavenRepositoryDir, String pomFile,
			String destBinJarFile, String destSrcJarFile) {
		
		content.append("<exec executable=\"${mvn-executable}\" dir=\"" + jarsDir + "\">");
		content.append("<arg value=\"deploy:deploy-file\"/>");
		content.append("<arg value=\"-Dfile=" + destBinJarFile + "\"/>");
		content.append("<arg value=\"-DpomFile=" + pomFile + "\"/>");
		content.append("<arg value=\"-Dsources=" + destSrcJarFile + "\"/>");
		addMavenRepositoryArgument(content, mavenRepositoryDir);
		content.append("</exec>");
	}
	
	/*
	protected void addDeploySourceJarToLocalRepositoryScript(XMLContent content,
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
	*/

	private void addMavenRepositoryArgument(XMLContent content,
			String localMavenRepositoryDir) {

		content.append("<arg value=\"-Durl=file:" + localMavenRepositoryDir + "\"/>");
	}

	protected void findDependenciesRecursively(Plugin plugin,
			Set<CompiledPlugin> pluginsToRepack,
			Map<String, String> pluginIdToVersionMap,
			boolean buildSnapshot) {
		
		Set<String> pluginsAssumedAvailable = repositorySpec.getPluginsAssumedAvailable();

		for (IDependable dependency : plugin.getDependencies()) {
			if (dependency instanceof CompiledPlugin) {
				CompiledPlugin compiledPlugin = (CompiledPlugin) dependency;

				String identifier = compiledPlugin.getIdentifier();
				String version = compiledPlugin.getVersion();
				
				// When we're building the snapshot repository we reference the
				// snapshot versions of the plug-ins that are assumed to be
				// available from other Maven repositories. This is required
				// to have a consistent dependency structure (snapshots must
				// reference snapshots, releases must reference releases).
				if (pluginsAssumedAvailable.contains(identifier) &&
					buildSnapshot) {
					version += SNAPSHOT_SUFFIX_UPPER;
				}
				pluginIdToVersionMap.put(identifier, version);

				if (includeInMavenRepository(compiledPlugin)) {
					pluginsToRepack.add(compiledPlugin);
				}

				findDependenciesRecursively(compiledPlugin, pluginsToRepack,
						pluginIdToVersionMap, buildSnapshot);
			}
		}
	}

	protected String generatePomXML(Plugin plugin, String pluginVersion,
			String pluginName, String pluginVendor,
			Map<String, String> plugin2VersionMap,
			Collection<Plugin> pluginsToPackage,
			Set<CompiledPlugin> pluginsToRepack) throws BuildException {
		
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
		for (ResolvedDependency dependency : plugin.getResolvedDependencies()) {
			UnresolvedDependency unresolvedDependency = dependency.getUnresolvedDependency();
			if (isOptional(unresolvedDependency)) {
				continue;
			}
			String dependencyID = unresolvedDependency.getIdentifier();
			if (!isContainedIn(pluginsToPackage, dependencyID) &&
				!isContainedIn(pluginsToRepack, dependencyID) &&
				!pluginsAssumedAvailable.contains(dependencyID)) {
				
				String message = "Can not create Maven artifact for '"
						+ plugin.getIdentifier()
						+ "' since its dependency '"
						+ dependencyID
						+ "' is not available nor assumed as being available in another Maven repository.";
				context.getBuildListener().handleBuildEvent(BuildEventType.ERROR, 
						message);
				
				if (repositorySpec.isStopOnError()) {
					throw new BuildException(message);
				}
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
		String mavenGroup = getMavenGroup(identifier);

		StringBuilder content = new StringBuilder();
		content.append("groupId=" + mavenGroup + "\n");
		content.append("artifactId=" + identifier + "\n");
		content.append("version=" + pluginVersion + "\n");
		return content.toString();
	}

	// TODO This must not be done here, but in the ANT Script
	protected File writeFile(String pomXMLContent, String pluginID,
			String pomExtension, boolean buildSnapshot) {
		
		File pomFolder = getTemporaryFolderForPOM(pluginID, buildSnapshot);
		pomFolder.mkdirs();
		
		File pomXMLFile = new File(pomFolder, "pom." + pomExtension);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pomXMLFile);
			// TODO Use explicit encoding?
			fos.write(pomXMLContent.getBytes());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
		return pomXMLFile;
	}

	private File getTemporaryFolderForPOM(String pluginID, boolean buildSnapshot) {
		File pomFolder = new File(artifactsFolder + File.separator + "temp"
				+ File.separator + "maven_poms"
				+ (buildSnapshot ? SNAPSHOT_SUFFIX : "") + File.separator
				+ pluginID + File.separator + "META-INF" + File.separator
				+ "maven" + File.separator + getMavenGroup(pluginID)
				+ File.separator + pluginID);
		return pomFolder;
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
		
		// This dependency is not marked optional in EMF plug-ins although it is
		// optional.
		// See also: https://bugs.eclipse.org/bugs/show_bug.cgi?id=328227
		if ("org.eclipse.core.runtime".equals(dependency.getIdentifier())) {
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

	private String idToName(String pluginID) {
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

	private String firstToUpper(String text) {
		return text.substring(0,1).toUpperCase() + text.substring(1);
	}
}
