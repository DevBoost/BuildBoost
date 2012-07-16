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
import java.util.Map;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.UnresolvedDependency;
import de.devboost.buildboost.util.XMLContent;

public class BuildUpdateSiteStep extends AbstractAntTargetGenerator {

	private EclipseUpdateSiteDeploymentSpec updateSiteSpec;
	private File targetDir;
	private String usernameProperty;
	private String passwordProperty;

	public BuildUpdateSiteStep(EclipseUpdateSiteDeploymentSpec updateSiteSpec, String usernameProperty, String passwordProperty, File targetDir) {
		super();
		this.updateSiteSpec = updateSiteSpec;
		this.usernameProperty = usernameProperty;
		this.passwordProperty = passwordProperty;
		this.targetDir = targetDir;
	}

	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
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
		content.append("<copy file=\"" + updateSiteFile.getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/site.xml\"/>");
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
		/*content.append("<!-- Copy new version of update site to server -->");
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
		content.append("</scp>");*/
		
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
		File updateSiteFile = updateSite.getFile();
		String repositoryDir = distDir + File.separator + "repositories" + File.separator + repositoryID;

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
					pluginName = "Unknown";
				}
				
				String pomXML = createPomXML(plugin, pluginVersion, pluginName, pluginVendor, plugin2VersionMap);
				if (pomXML == null) {
					continue;
				}
				
				File pomFolder = new File(pluginDirectory.getAbsolutePath() + File.separator + "META-INF"
						+ File.separator + "maven" + File.separator + getMavenGroup(pluginID)
						+ File.separator + pluginID);
				pomFolder.mkdirs();
				File pomXMLFile = new File(pomFolder, "pom.xml");
				
				try {
					new FileOutputStream(pomXMLFile).write(pomXML.getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// package plugin(s)
				content.append("<echo message=\"Packaging binary artifact '" + pluginID + "'\"/>");
				content.append("<manifest file=\"" + pluginPath + "/META-INF/MANIFEST.MF\" mode=\"update\">");
				content.append("<attribute name=\"Bundle-Version\" value=\"" + pluginVersion + ".v${buildid}\"/>");
				content.append("<attribute name=\"Bundle-Vendor\" value=\"" + pluginVendor + "\"/>");
				content.append("<attribute name=\"Bundle-SymbolicName\" value=\"" + pluginID + "; singleton:=true\"/>");
				content.append("<attribute name=\"Bundle-Name\" value=\"" + pluginName + "\"/>");
				content.append("</manifest>");
				content.appendLineBreak();
				content.append("<jar destfile=\"" + repositoryDir + "/" + pluginID + "-" + pluginVersion + "-SNAPSHOT.jar\" manifest=\"" + pluginPath + "/META-INF/MANIFEST.MF\">");
				content.append("<fileset dir=\"" + pluginPath + "\" excludes=\".*,src*\"/>"); //TODO pattern
				content.append("</jar>");
				content.appendLineBreak();

				content.append("<echo message=\"Packaging source artifact '" + pluginID + "'\"/>");
				content.append("<manifest file=\"" + pluginPath + "/META-INF/MANIFEST.MF\" mode=\"update\">");
				content.append("<attribute name=\"Bundle-Version\" value=\"" + pluginVersion + ".v${buildid}\"/>");
				content.append("<attribute name=\"Bundle-Vendor\" value=\"" + pluginVendor + "\"/>");
				content.append("<attribute name=\"Bundle-SymbolicName\" value=\"" + pluginID + "; singleton:=true\"/>");
				content.append("<attribute name=\"Bundle-Name\" value=\"" + pluginName + "\"/>");
				content.append("</manifest>");
				content.appendLineBreak();
				content.append("<jar destfile=\"" + repositoryDir + "/" + pluginID + "-" + pluginVersion + "-SNAPSHOT-sources.jar\" manifest=\"" + pluginPath + "/META-INF/MANIFEST.MF\">");
				 //TODO read .classpath for src-folders! remove <mkdir/>s then...
				content.append("<mkdir dir=\"" + pluginPath + "/src\"/>");
				content.append("<fileset dir=\"" + pluginPath + "/src\"/>");
				content.append("<mkdir dir=\"" + pluginPath + "/src-gen\"/>");
				content.append("<fileset dir=\"" + pluginPath + "/src-gen\"/>");
				// ----
				content.append("</jar>");
				content.appendLineBreak();
			}
		}
		
		//TODO call mvn deploy
		
		AntTarget target = new AntTarget("build-maven-repository", content);
		return target;
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
	
	private String createPomXML(Plugin plugin, String pluginVersion, 
			String pluginName, String pluginVendor, Map<String, String> plugin2VersionMap) {
		XMLContent content = new XMLContent();
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		content.append("<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"" +
				" xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		content.append("<modelVersion>");
		content.append("4.0.0");
		content.append("</modelVersion>");
		content.append("<groupId>");
		content.append(getMavenGroup(plugin.getIdentifier()));
		content.append("</groupId>");
		content.append("<artifactId>");
		content.append(plugin.getIdentifier());
		content.append("</artifactId>");
		content.append("<version>");
		content.append(pluginVersion);
		content.append("</version>");
		content.append("<name>");
		content.append(pluginName);
		content.append("</name>");
		content.append("<organization>");
		content.append("<name>");
		content.append(pluginVendor);
		content.append("</name>");
		content.append("</organization>");
		content.append("<licenses>");
		content.append("<license>");
		content.append("<name>");
		content.append("Eclipse Public License - v 1.0");
		content.append("</name>");
		content.append("<url>");
		content.append("http://www.eclipse.org/org/documents/epl-v10.html");
		content.append("</url>");
		content.append("</license>");
		content.append("</licenses>");
		content.append("<dependencies>");
		content.append("<dependency>");
		for (UnresolvedDependency dependency : plugin.getResolvedDependencies()) {
			if (dependency.isOptional()) {
				continue;
			}
			String dependencyVersion = plugin2VersionMap.get(dependency.getIdentifier());
			if (dependencyVersion == null) {
				System.out.println("Can not create maven artifact for " + plugin.getIdentifier() 
						+ " since " + dependency.getIdentifier() + " is not versioned");
				return null;
			}
			content.append("<groupId>");
			content.append(getMavenGroup(dependency.getIdentifier()));
			content.append("</groupId>");
			content.append("<artifactId>");
			content.append(dependency.getIdentifier());
			content.append("</artifactId>");
			content.append("<version>");
			content.append("" + dependencyVersion);
			content.append("</version>");
		}
		content.append("</dependency>");
		content.append("</dependencies>");
		content.append("</project>");
		return content.toString();
	}
	

	public String getMavenGroup(String identifier) {
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

}
