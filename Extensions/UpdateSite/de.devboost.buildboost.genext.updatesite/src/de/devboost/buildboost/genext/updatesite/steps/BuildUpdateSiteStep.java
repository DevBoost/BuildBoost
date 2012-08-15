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
package de.devboost.buildboost.genext.updatesite.steps;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
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

public class BuildUpdateSiteStep extends AbstractAntTargetGenerator {
	
	private EclipseUpdateSiteDeploymentSpec updateSiteSpec;
	private File targetDir;
	private String usernameProperty = null;
	private String passwordProperty = null;

	public BuildUpdateSiteStep(EclipseUpdateSiteDeploymentSpec updateSiteSpec, File targetDir) {
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
		
		AntTarget updateSiteTarget = generateUpdateSiteAntTarget();
		AntTarget mavenRepositoryTarget = generateMavenRepositoryAntTarget();
		return Arrays.asList(new AntTarget[] {updateSiteTarget, mavenRepositoryTarget});
	}

	protected AntTarget generateUpdateSiteAntTarget() throws BuildException {
		EclipseUpdateSite updateSite = updateSiteSpec.getUpdateSite();
		if (updateSite == null) {
			throw new BuildException("Can't find update site for update site specification.");
		}
		
		String distDir = targetDir.getAbsolutePath() + File.separator + "dist";

		XMLContent content = new XMLContent();
		
		String updateSiteID = updateSite.getIdentifier();
		File updateSiteFile = updateSite.getFile();
		String updateSiteDir = distDir + File.separator + "updatesites" + File.separator + updateSiteID;

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

		content.append("<echo message=\"Copying update site descriptor for update site '" + updateSiteID + "'\"/>");
		content.append("<mkdir dir=\"" + updateSiteDir + "\" />");
		content.append("<mkdir dir=\"" + updateSiteDir + "/plugins\" />");
		content.append("<mkdir dir=\"" + updateSiteDir + "/features\" />");
		content.append("<copy file=\"" + updateSiteFile.getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/site.xml\"/>");
		content.append("<copy file=\"" + new File(updateSiteFile.getParent(), "associateSites.xml").getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/associateSites.xml\"/>");
		content.appendLineBreak();

		String updateSiteVendor = updateSiteSpec.getValue("site", "vendor");
		if (updateSiteVendor == null) {
			updateSiteVendor = "Unknown vendor";
		}

		Collection<EclipseFeature> features = updateSite.getFeatures();
		for (EclipseFeature feature : features) {
			String featureID = feature.getIdentifier();
			File featureFile = feature.getFile();
			String tempDir = distDir + File.separator + "temp_features";
			String tempFeatureDir = tempDir + "/" + featureID;
			String featureVersion = getFeatureVersion(featureID);
			String featureVendor = getFeatureVendor(featureID);

			content.append("<echo message=\"Building feature '" + featureID + "' for update site '" + updateSiteID + "'\"/>");
			content.append("<!-- update version numbers in feature.xml -->");
			content.append("<!-- copy to be modified -->");
			content.append("<mkdir dir=\"" + tempFeatureDir + "\" />");
			content.append("<copy file=\"" + featureFile.getAbsolutePath() + "\" tofile=\"" + tempFeatureDir + "/feature.xml\"/>");
			content.append("<!-- set version in copy -->");
			content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token=\"0.0.0\" value=\"" + featureVersion + ".v${buildid}\"/>");
			// === TODO put this into a separate stage
			String feedbackFeatureID = "de.devboost.eclipse.feedback";
			if (!feedbackFeatureID.equals(featureID)) {
				content.append("<replace file='" + tempFeatureDir + "/feature.xml' token='&lt;requires&gt;' value='&lt;requires&gt;&lt;import feature=\"" + feedbackFeatureID + "\"/&gt;'/>");			
			}
			// ===
			content.append("<!-- create empty file 'feature.properties' -->");
			content.append("<touch file=\"feature.properties\"/>");
			content.append("<!-- create feature JAR -->");
			content.append("<jar basedir=\"" + tempFeatureDir + "\" includes=\"feature.xml\" destfile=\"" + updateSiteDir + "/features/" + featureID + "_" + featureVersion + ".v${buildid}.jar\">");
			content.append("<fileset dir=\".\" includes=\"feature.properties\" />");
			content.append("</jar>");
			content.append("<!-- delete temporary directory -->");
			content.append("<delete dir=\"" + tempDir + "\"/>");
			content.append("<!-- set version in site.xml -->");
			content.append("<replaceregexp file=\"" + updateSiteDir + "/site.xml\" match='feature url=\"features/" + featureID + "_[0-9]*.[0-9]*.[0-9]*.v[0-9]*.jar\" id=\"" + featureID + "\" version=\"[0-9]*.[0-9]*.[0-9]*.v[0-9]*\"' replace='feature url=\"features/" + featureID + "_" + featureVersion + ".v${buildid}.jar\" id=\"" + featureID + "\" version=\"" + featureVersion + ".v${buildid}\"'/>");
			content.appendLineBreak();

			Collection<Plugin> plugins = feature.getPlugins();
			for (Plugin plugin : plugins) {
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
					pluginName = "Unknown";
				}
				// package plugin(s)
				content.append("<echo message=\"Packaging plug-in '" + pluginID + "' for update site '" + updateSiteID + "'\"/>");
				content.append("<manifest file=\"" + pluginPath + "/META-INF/MANIFEST.MF\" mode=\"update\">");
				content.append("<attribute name=\"Bundle-Version\" value=\"" + pluginVersion + ".v${buildid}\"/>");
				content.append("<attribute name=\"Bundle-Vendor\" value=\"" + pluginVendor + "\"/>");
				content.append("<attribute name=\"Bundle-SymbolicName\" value=\"" + pluginID + "; singleton:=true\"/>");
				content.append("<attribute name=\"Bundle-Name\" value=\"" + pluginName + "\"/>");
				content.append("</manifest>");
				content.appendLineBreak();
				content.append("<jar destfile=\"" + updateSiteDir + "/plugins/" + pluginID + "_" + pluginVersion + ".v${buildid}.jar\" manifest=\"" + pluginPath + "/META-INF/MANIFEST.MF\">");
				// TODO make this configurable / or read the build.properties file for this
				content.append("<fileset dir=\"" + pluginPath + "\" excludes=\".*\"/>");
				content.append("</jar>");
				content.appendLineBreak();
			}
		}
		
		String targetPath = updateSiteSpec.getValue("site", "uploadPath");
		// TODO this requires that jsch-0.1.48.jar is in ANTs classpath. we
		// should figure out a way to provide this JAR together with BuildBoost.
		content.append("<!-- Copy new version of update site to server -->");
		content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + targetPath + "\" port=\"22\" sftp=\"true\" trust=\"true\">");
		content.append("<fileset dir=\"" + updateSiteDir + "\">");
		content.append("<include name=\"features/**\"/>");
		content.append("<include name=\"plugins/**\"/>");
		content.append("<include name=\"associateSites.xml\"/>");
		content.append("<include name=\"digest.zip\"/>");
		content.append("<include name=\"COPYING\"/>");
		content.append("</fileset>");
		content.append("</scp>");
		content.append("<!-- We copy the site.xml, artifacts.jar and content.jar separately to make sure these");
		content.append("are the lasts file that are replaced. Otherwise the files might point to JARs that ");
		content.append("have not been uploaded yet. -->");
		content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + targetPath + "\" port=\"22\" sftp=\"true\" trust=\"true\">");
		content.append("<fileset dir=\"" + updateSiteDir + "\">");
		content.append("<include name=\"artifacts.jar\"/>");
		content.append("<include name=\"content.jar\"/>");
		content.append("<include name=\"site.xml\"/>");
		content.append("</fileset>");
		content.append("</scp>");
		
		AntTarget target = new AntTarget("build-update-site", content);
		return target;
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
			String featureVersion = getFeatureVersion(feature.getIdentifier());
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
			String featureVersion = getFeatureVersion(feature.getIdentifier());
			String featureVendor = getFeatureVendor(feature.getIdentifier());
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
			String repoPath = targetPath.substring(0, targetPath.lastIndexOf('/') + 1) + "maven-repository" ;
			
			content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + repoPath + "\" port=\"22\" sftp=\"true\" trust=\"true\">");
			content.append("<fileset dir=\"" + mavenRespoitoryDir + "\">");
			content.append("</fileset>");
			content.append("</scp>");
		}
		
		AntTarget target = new AntTarget("build-maven-repository", content);
		return target;
	}

	private void deployBinInRepository(XMLContent content, String jarsDir,
			String mavenRespoitoryDir, String pomFile, String destBinJarFile) {
		
		content.append("<exec executable=\"mvn\" dir=\"" + jarsDir + "\">");
		content.append("<arg value=\"deploy:deploy-file\"/>");
		content.append("<arg value=\"-Dfile=" + destBinJarFile + "\"/>");
		content.append("<arg value=\"-DpomFile=" + pomFile + "\"/>");
		content.append("<arg value=\"-Durl=file:" + mavenRespoitoryDir + "\"/>");
		content.append("</exec>");
	}
	
	private void deploySrcInRepository(XMLContent content, String jarsDir,
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

	private void addDependenciesRecursively(Plugin plugin,
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

	private File writePomFile(String pomXMLContent, File pluginDirectory, String pluginID, String extension) {
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

	private String getFeatureVendor(String featureID) {
		String featureVendor = updateSiteSpec.getValue("feature", featureID, "vendor");
		if (featureVendor == null) {
			featureVendor = updateSiteSpec.getValue("site", "vendor");
		}
		if (featureVendor == null) {
			featureVendor = "Unknown vendor";
		}
		return featureVendor;
	}

	private String getFeatureVersion(String featureID) {
		String featureVersion = updateSiteSpec.getValue("feature", featureID, "version");
		if (featureVersion == null) {
			featureVersion = updateSiteSpec.getValue("site", "version");
		}
		if (featureVersion == null) {
			featureVersion = "0.0.1";
		}
		return featureVersion;
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
	
	private String firstToUpper(String s) {
		return s.substring(0,1).toUpperCase() + s.substring(1);
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


}
