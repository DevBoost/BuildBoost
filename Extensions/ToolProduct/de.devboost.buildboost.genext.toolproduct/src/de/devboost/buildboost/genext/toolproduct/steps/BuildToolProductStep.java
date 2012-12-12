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
package de.devboost.buildboost.genext.toolproduct.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.util.XMLContent;
import de.devboost.buildboost.util.AntScriptUtil;

public class BuildToolProductStep extends AbstractAntTargetGenerator {
	
	private EclipseUpdateSiteDeploymentSpec deploymentSpec;
	private File targetDir;

	public BuildToolProductStep(EclipseUpdateSiteDeploymentSpec deploymentSpec, File targetDir) {
		super();
		this.deploymentSpec = deploymentSpec;
		this.targetDir = targetDir;
	}
	
	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		AntTarget updateSiteTarget = generateUpdateSiteAntTarget();
		return Collections.singletonList(updateSiteTarget);
	}

	protected AntTarget generateUpdateSiteAntTarget() throws BuildException {
		if (deploymentSpec == null) {
			throw new BuildException("Can't find deployment spec site for product specification.");
		}
		
		XMLContent content = new XMLContent();
		
		content.append("<property environment=\"env\"/>");
		content.append("<!-- Get BUILD_ID from environment -->");
		content.append("<condition property=\"buildid\" value=\"${env.BUILD_ID}\">");
		content.append("<isset property=\"env.BUILD_ID\" />");
		content.append("</condition>");
		content.appendLineBreak();
		//TODO this is not good, because the tstamp should not be stage dependent
		content.append("<!-- fallback if env.BUILD_ID is not set -->");
		content.append("<tstamp/>");
		content.append("<property name=\"buildid\" value=\"${DSTAMP}${TSTAMP}\" />");
		content.appendLineBreak();
		
		File updateSiteFolder = deploymentSpec.getUpdateSite().getFile().getParentFile();
		
		File productBuildDir = new File(targetDir, "products");
		productBuildDir.mkdir();
		
		String productName = deploymentSpec.getValue("product", "name");
		String productFeatureID = deploymentSpec.getValue("product", "feature");
		String siteVersion = deploymentSpec.getFeatureVersion(productFeatureID);
		
		File sdkFolder = new File(targetDir.getParentFile().getParentFile(), "eclipse-sdks");
		File productFolder = new File(productBuildDir, productName);
		sdkFolder.mkdir();
		productFolder.mkdir();
		
		//call director for publishing
		Map<String, String> configs = deploymentSpec.getValues("product", "type");
		for (Entry<String, String> conf : configs.entrySet()) {
			String productType = conf.getKey();
			String url = conf.getValue();
			String sdkZipName = url.substring(url.lastIndexOf("/") + 1);
			File sdkZipFile = new File(sdkFolder, sdkZipName);
			
			if (!sdkZipFile.exists()) {
				AntScriptUtil.addDownloadFileScript(content, url, sdkFolder.getAbsolutePath());
			}
			content.appendLineBreak();
			
			File productInstallationFolder = new File(productFolder, productType);
			productInstallationFolder.mkdir();
			AntScriptUtil.addZipFileExtractionScript(content, sdkZipFile, productInstallationFolder);
			content.appendLineBreak();
			
			content.append("<exec executable=\"eclipse\" failonerror=\"true\">");
			
			content.append("<arg value=\"--launcher.suppressErrors\"/>");
			content.append("<arg value=\"-noSplash\"/>");
			content.append("<arg value=\"-application\"/>");
			content.append("<arg value=\"org.eclipse.equinox.p2.director\"/>");
			
			content.append("<arg value=\"-repository\"/>");
			//TODO Juno and Feedback update-sites are hard coded as dependency here
			content.append("<arg value=\"file:" + updateSiteFolder.getAbsolutePath() + ",http://download.eclipse.org/releases/juno,http://www.devboost.de/eclipse-feedback/update\"/>");
			content.append("<arg value=\"-installIU\"/>");
			content.append("<arg value=\"" + productFeatureID + ".feature.group\"/>");
			content.append("<arg value=\"-tag\"/>");
			content.append("<arg value=\"InstallationOf" + productName + "\"/>");
			content.append("<arg value=\"-destination\"/>");
			content.append("<arg value=\"" + productInstallationFolder.getAbsolutePath() + "/eclipse\"/>");
			content.append("<arg value=\"-profile\"/>");
			content.append("<arg value=\"SDKProfile\"/>");
			
			content.append("</exec>");
			content.appendLineBreak();
			
			//TODO do more stuff:
			// - rename "eclipse" base folder
			// - add workspace (put 'osgi.instance.area.default=../../../workspace' into config.ini)
			// - replace eclipse launcher file/folder by custom launcher (or rename launcher)
			// - configure splash screen
			

			File productsDistFolder = new File(updateSiteFolder.getParentFile().getParentFile(), "products");
			String productZipPath = new File(productsDistFolder, productName + "-" + siteVersion + "-" + productType + ".zip").getAbsolutePath();
			productsDistFolder.mkdir();
			//TODO this needs to use native tar.gz for unix systems in order to preserve file flags
			content.append("<zip destfile=\"" + productZipPath  + "\" basedir=\""+ productInstallationFolder.getAbsolutePath() + "\" />");
			content.appendLineBreak();
			
			//TODO upload ZIP
		}

		String updateSiteID = deploymentSpec.getUpdateSite().getIdentifier();
		AntTarget target = new AntTarget("build-eclipse-tool-product-" + updateSiteID, content);
		return target;
	}



}
