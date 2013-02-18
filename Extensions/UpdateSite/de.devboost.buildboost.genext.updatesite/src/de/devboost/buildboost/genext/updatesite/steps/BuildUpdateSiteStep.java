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
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.EclipseFeature;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.model.IDependable;
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
		}
		if (passwordProperty == null) {
			passwordProperty = updateSiteSpec.getValue("site", "passwordProperty");
		}
		
		AntTarget updateSiteTarget = generateUpdateSiteAntTarget();
		return Collections.singletonList(updateSiteTarget);
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
		File associatedSitesXML = new File(updateSiteFile.getParent(), "associateSites.xml");
		if (associatedSitesXML.exists()) {
			content.append("<copy file=\"" + associatedSitesXML.getAbsolutePath() + "\" tofile=\"" + updateSiteDir + "/associateSites.xml\"/>");			
		}
		content.appendLineBreak();

		String updateSiteVendor = updateSiteSpec.getValue("site", "vendor");
		if (updateSiteVendor == null) {
			updateSiteVendor = "Unknown vendor";
		}
		String excludeSrc = updateSiteSpec.getValue("site", "excludeSources");
		if (excludeSrc == null) {
			excludeSrc = "false";
		}

		Collection<EclipseFeature> features = updateSite.getFeatures();
		for (EclipseFeature feature : features) {
			String featureID = feature.getIdentifier();
			File featureFile = feature.getFile();
			String tempDir = distDir + File.separator + "temp_features";
			String tempFeatureDir = tempDir + "/" + featureID;
			String featureVersion = feature.getVersion();
			String featureVendor = updateSiteSpec.getFeatureVendor(featureID);

			content.append("<echo message=\"Building feature '" + featureID + "' for update site '" + updateSiteID + "'\"/>");
			content.append("<!-- update version numbers in feature.xml -->");
			content.append("<!-- copy to be modified -->");
			content.append("<mkdir dir=\"" + tempFeatureDir + "\" />");
			content.append("<copy file=\"" + featureFile.getAbsolutePath() + "\" tofile=\"" + tempFeatureDir + "/feature.xml\"/>");
			content.append("<!-- set version in copy -->");
			content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token=\"0.0.0\" value=\"" + featureVersion + ".v${buildid}\"/>");
			content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token=\"" + featureVersion + "\" value=\"" + featureVersion + ".v${buildid}\"/>");
			content.append("<replace file=\"" + tempFeatureDir + "/feature.xml\" token=\".qualifier\" value=\".v${buildid}\"/>");

			Collection<IDependable> dependencies = feature.getDependencies();
			for (IDependable dependency : dependencies) {
				if (dependency instanceof EclipseFeature) {
					EclipseFeature requiredFeature = (EclipseFeature) dependency;
					content.append("<replaceregexp file=\"" + tempFeatureDir + "/feature.xml\" match='<import feature=\"" + requiredFeature.getIdentifier() + "\"' replace='<import feature=\"" + requiredFeature.getIdentifier() + "\" version=\"" + requiredFeature.getVersion() + "\" match=\"greaterOrEqual\"' />");
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
				content.append("<fileset dir=\"" + pluginPath + "\">");
				// TODO make this configurable or read the build.properties file for this
				content.append("<exclude name=\"**/.*/**\"/>");
				if (Boolean.parseBoolean(excludeSrc)) {
					content.append("<exclude name=\"**/src*/**\"/>");
				}
				content.append("</fileset>");
				content.append("</jar>");
				content.appendLineBreak();
			}
		}
		
		String targetPath = updateSiteSpec.getValue("site", "uploadPath");
		if (targetPath != null) {
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
		}
		
		AntTarget target = new AntTarget("build-update-site-" + updateSiteID, content);
		return target;
	}



}
