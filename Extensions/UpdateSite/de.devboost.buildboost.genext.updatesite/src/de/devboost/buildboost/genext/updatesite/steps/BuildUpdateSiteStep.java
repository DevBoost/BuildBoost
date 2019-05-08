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
package de.devboost.buildboost.genext.updatesite.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.util.PluginPackagingHelper;
import de.devboost.buildboost.util.TimestampUtil;
import de.devboost.buildboost.util.XMLContent;

public class BuildUpdateSiteStep extends AbstractAntTargetGenerator {
	
	private final EclipseUpdateSiteDeploymentSpec updateSiteSpec;
	private final File targetDir;
	
	private String usernameProperty = null;
	private String passwordProperty = null;

	public BuildUpdateSiteStep(EclipseUpdateSiteDeploymentSpec updateSiteSpec,
			File targetDir) {
		
		super();
		this.updateSiteSpec = updateSiteSpec;
		this.targetDir = targetDir;
	}
	
	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		if (usernameProperty == null) {
			usernameProperty = updateSiteSpec.getSiteUsernameProperty();
		}
		if (passwordProperty == null) {
			passwordProperty = updateSiteSpec.getSitePasswordProperty();
		}
		
		AntTarget updateSiteTarget = generateUpdateSiteAntTarget();
		return Collections.singletonList(updateSiteTarget);
	}

	protected AntTarget generateUpdateSiteAntTarget() throws BuildException {
		EclipseUpdateSite updateSite = updateSiteSpec.getUpdateSite();
		if (updateSite == null) {
			throw new BuildException("Can't find update site for update site specification.");
		}
		
		String distDir = targetDir.getAbsolutePath();

		XMLContent content = new XMLContent();
		
		String updateSiteID = updateSite.getIdentifier();
		File updateSiteFile = updateSite.getFile();
		String updateSiteDir = distDir + File.separator + "updatesites" + File.separator + updateSiteID;

		new TimestampUtil().addGetBuildTimestampFromEnvironment(content);
	
		content.append("<!-- fallback if env.BUILD_ID is not set -->");
		content.append("<tstamp/>");
		content.append("<property name=\"buildid\" value=\"${DSTAMP}${TSTAMP}\" />");
		content.appendLineBreak();

		content.append("<echo message=\"Copying update site descriptor for update site '" + updateSiteID + "'\"/>");
		content.append("<mkdir dir=\"" + updateSiteDir + "\" />");
		content.append("<mkdir dir=\"" + updateSiteDir + "/plugins\" />");
		content.append("<mkdir dir=\"" + updateSiteDir + "/features\" />");
		content.append("<copy file=\"" + updateSiteFile.getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/site.xml\"/>");
		File associatedSitesXML = new File(updateSiteFile.getParent(), "associateSites.xml");
		if (associatedSitesXML.exists()) {
			content.append("<copy file=\"" + associatedSitesXML.getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/associateSites.xml\"/>");			
		}
		content.appendLineBreak();
		
		File categoryXML = new File(updateSiteFile.getParent(), "category.xml");
		if (categoryXML.exists()) {
			content.append("<copy file=\"" + categoryXML.getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/category.xml\"/>");			
		}
		content.appendLineBreak();

		String updateSiteVendor = updateSiteSpec.getSiteVendor();
		if (updateSiteVendor == null) {
			updateSiteVendor = "Unknown vendor";
		}
		String excludeSrc = updateSiteSpec.getExcludeSources();
		if (excludeSrc == null) {
			excludeSrc = "false";
		}

		Collection<EclipseFeature> features = updateSite.getFeatures();
		for (EclipseFeature feature : features) {
			String featureID = feature.getIdentifier();
			File featureFile = feature.getFile();
			boolean isFeatureJAR = featureFile.getName().endsWith(".jar");
			String tempDir = distDir + File.separator + "temp_features";
			String tempFeatureDir = tempDir + "/" + featureID;
			String featureVersion = feature.getVersion();
			String featureVendor = updateSiteSpec.getFeatureVendor(featureID);

			if (isFeatureJAR) {
				// the feature is already packaged, we can just copy it
				content.append("<copy file=\"" + featureFile.getAbsolutePath() + "\" todir=\"" + updateSiteDir + "/features\"/>");
				content.append("<!-- set correct reference in site.xml -->");
				content.append("<replaceregexp file=\"" + updateSiteDir + "/site.xml\" match='feature url=\"features/" + featureID + "_[0-9]*.[0-9]*.[0-9]*.v[0-9]*.jar\" id=\"" + featureID + "\" version=\"[0-9]*.[0-9]*.[0-9]*.v[0-9]*\"' replace='feature url=\"features/" + featureFile.getName() + "\" id=\"" + featureID + "\" version=\"" + featureVersion + "\"'/>");
				if(categoryXML.exists()){
					// <feature url="features/org.modelrefactoring.smells_0.0.0.v0.jar" id="org.modelrefactoring.smells" version="0.0.0.v0">
					content.append("<replaceregexp file=\"" + updateSiteDir + "/category.xml\" match='feature url=\"features/" + featureID + "_[0-9]*.[0-9]*.[0-9]*.v[0-9]*.jar\" id=\"" + featureID + "\" version=\"[0-9]*.[0-9]*.[0-9]*.v[0-9]*\"' replace='feature url=\"features/" + featureFile.getName() + "\" id=\"" + featureID + "\" version=\"" + featureVersion + "\"'/>");
				}
				content.appendLineBreak();
			} else {
				// the feature is not packaged yet, we need to create a JAR file
				content.append("<echo message=\"Building feature '" + featureID + "' for update site '" + updateSiteID + "'\"/>");
				content.append("<!-- update version numbers in feature.xml -->");
				content.append("<!-- copy to be modified -->");
				content.append("<mkdir dir=\"" + tempFeatureDir + "\" />");
				content.append("<copy file=\"" + featureFile.getAbsolutePath() + "\" tofile=\"" + tempFeatureDir + "/feature.xml\"/>");
				content.append("<!-- set version in copy -->");
				content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token='\"0.0.0\"' value='\"" + featureVersion + ".v${buildid}\"'/>");
				content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token='\"" + featureVersion + "\"' value='\"" + featureVersion + ".v${buildid}\"'/>");
				content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token=\".qualifier\" value=\".v${buildid}\"/>");
	
				Collection<IDependable> dependencies = feature.getDependencies();
				for (IDependable dependency : dependencies) {
					if (dependency instanceof EclipseFeature) {
						EclipseFeature requiredFeature = (EclipseFeature) dependency;
						if (requiredFeature.isTargetPlatformFeature()) {
							// for target platform features we do not set a minimum
							// version
							continue;
						}
						content.append("<replaceregexp file=\"" + tempFeatureDir + "/feature.xml\" match='&lt;import feature=\"" + requiredFeature.getIdentifier() + "\"' replace='&lt;import feature=\"" + requiredFeature.getIdentifier() + "\" version=\"" + requiredFeature.getVersion() + "\" match=\"greaterOrEqual\"' />");
					}
				}

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
				if(categoryXML.exists()){
					// <feature url="features/org.modelrefactoring.smells_0.0.0.v0.jar" id="org.modelrefactoring.smells" version="0.0.0.v0">
					content.append("<replaceregexp file=\"" + updateSiteDir + "/category.xml\" match='feature url=\"features/" + featureID + "_[0-9]*.[0-9]*.[0-9]*.v[0-9]*.jar\" id=\"" + featureID + "\" version=\"[0-9]*.[0-9]*.[0-9]*.v[0-9]*\"' replace='feature url=\"features/" + featureID + "_" + featureVersion + ".v${buildid}.jar\" id=\"" + featureID + "\" version=\"" + featureVersion + ".v${buildid}\"'/>");
				}
				content.appendLineBreak();
			}
			
			Collection<Plugin> plugins = feature.getRequiredPlugins();
			for (Plugin plugin : plugins) {
				addPackagePluginTasks(content, updateSiteID, updateSiteDir,
						Boolean.parseBoolean(excludeSrc), featureVersion, featureVendor, plugin);
			}
		}
		
		addCreateCompleteZipTask(content, updateSiteID, updateSiteDir);
		
		String targetPath = updateSiteSpec.getSiteUploadPath();
		if (targetPath != null) {
			addUploadTasks(content, updateSiteID, updateSiteDir, targetPath, categoryXML);
		}
		
		AntTarget target = new AntTarget("build-update-site-" + updateSiteID, content);
		return target;
	}

	private void addCreateCompleteZipTask(XMLContent content,
			String updateSiteID, String updateSiteDir) {
		
		String zipFilePath = updateSiteDir + "/" + getUpdateSiteCompleteFileName(updateSiteID);

		content.append("<!-- Create zipped version of update site -->");
		content.append("<zip destfile=\"" + zipFilePath + "\" basedir=\"" + updateSiteDir + "\" />");
		content.append("<checksum file=\"" + zipFilePath + "\"/>");
	}

	private void addUploadTasks(XMLContent content, String updateSiteID, 
			String updateSiteDir, String targetPath, File categoryXML) {
		// TODO this requires that jsch-0.1.48.jar is in ANTs classpath. we
		// should figure out a way to provide this JAR together with BuildBoost.
		content.append("<!-- Copy new version of update site to server -->");
		String scpTag = "<scp todir=\"${env." + usernameProperty + "}@" + targetPath + "\" keyfile=\"${user.home}/.ssh/id_rsa\" port=\"22\" sftp=\"true\" trust=\"true\">";
		content.append(scpTag);
		content.append("<fileset dir=\"" + updateSiteDir + "\">");
		content.append("<include name=\"features/**\"/>");
		content.append("<include name=\"plugins/**\"/>");
		content.append("<include name=\"associateSites.xml\"/>");
		content.append("<include name=\"digest.zip\"/>");
		content.append("<include name=\"COPYING\"/>");
		content.append("</fileset>");
		content.append("</scp>");
		content.appendLineBreak();

		content.append("<!-- We copy the site.xml, artifacts.jar and content.jar separately to make sure these");
		content.append("are the last files that are replaced. Otherwise the files might point to JARs that ");
		content.append("have not been uploaded yet. -->");
		if(!categoryXML.exists()){
			// if it doesn't exist just create a copy of site.xml and rename it
			// must be done before uploading
			content.append("<copy file=\"" + updateSiteDir + File.separator + "site.xml\" tofile=\"" + updateSiteDir + File.separator + "category.xml\" overwrite=\"true\"/>");
		}
		content.append(scpTag);
		content.append("<fileset dir=\"" + updateSiteDir + "\">");
		content.append("<include name=\"artifacts.jar\"/>");
		content.append("<include name=\"content.jar\"/>");
		content.append("<include name=\"site.xml\"/>");
		content.append("<include name=\"category.xml\"/>");
		content.append("</fileset>");
		content.append("</scp>");
		content.appendLineBreak();
		
		content.append("<!-- Copy zipped version of update site to server (used by downstream builds) -->");
		content.append(scpTag);
		content.append("<fileset dir=\"" + updateSiteDir + "\">");
		content.append("<include name=\"" + getUpdateSiteCompleteFileName(updateSiteID) + "\"/>");
		content.append("<include name=\"" + getUpdateSiteCompleteFileName(updateSiteID) + ".MD5\"/>");
		content.append("</fileset>");
		content.append("</scp>");
		content.appendLineBreak();
	}

	private String getUpdateSiteCompleteFileName(String updateSiteID) {
		return updateSiteID + "-complete.zip";
	}

	private void addPackagePluginTasks(XMLContent content, String updateSiteID,
			String updateSiteDir, boolean excludeSrc, String featureVersion,
			String featureVendor, Plugin plugin) {
		
		String pluginID = plugin.getIdentifier();
		File pluginDirectory = plugin.getFile();
		String pluginPath = pluginDirectory.getAbsolutePath();

		String pluginVersion = updateSiteSpec.getPluginVersion(pluginID);
		if (pluginVersion == null) {
			pluginVersion = featureVersion;
		}
		String pluginVendor = updateSiteSpec.getPluginVendor(pluginID);
		if (pluginVendor == null) {
			pluginVendor = featureVendor;
		}
		String pluginName = updateSiteSpec.getPluginName(pluginID);
		
		new PluginPackagingHelper().addUpdateManifestScript(content, plugin, 
				pluginVersion, pluginVendor, pluginName);

		boolean isPackaged = plugin.isJarFile();

		// TODO Use PluginPackagingHelper here instead?
		if (isPackaged) {
			content.append("<copy file=\"" + pluginPath + "\" todir=\"" + updateSiteDir + "/plugins\" />");
		} else {
			content.append("<jar destfile=\"" + updateSiteDir + "/plugins/" + pluginID + "_" + pluginVersion + ".v${buildid}.jar\" manifest=\"" + pluginPath + "/META-INF/MANIFEST.MF\">");
			content.append("<fileset dir=\"" + pluginPath + "\">");
			// TODO make this configurable or read the build.properties file for this
			content.append("<exclude name=\"**/.*/**\"/>");
			if (excludeSrc) {
				Set<String> relativeSourceFolders = plugin.getRelativeSourceFolders();
				for (String relativeSourceFolder : relativeSourceFolders) {
					content.append("<exclude name=\"" + relativeSourceFolder + "/**\"/>");
				}
			}
			
			content.append("</fileset>");
			content.append("</jar>");
		}
		content.appendLineBreak();
	}
}
