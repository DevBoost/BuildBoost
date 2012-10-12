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
package de.devboost.buildboost.genext.product.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.genext.product.artifacts.EclipseProduct;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSiteDeploymentSpec;
import de.devboost.buildboost.util.XMLContent;

public class BuildProductStep extends AbstractAntTargetGenerator {
	
	private EclipseProduct productSpec;
	private File targetDir;

	public BuildProductStep(EclipseProduct productSpec, File targetDir) {
		super();
		this.productSpec = productSpec;
		this.targetDir = targetDir;
	}
	
	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		AntTarget updateSiteTarget = generateUpdateSiteAntTarget();
		return Collections.singletonList(updateSiteTarget);
	}

	protected AntTarget generateUpdateSiteAntTarget() throws BuildException {
		EclipseUpdateSiteDeploymentSpec deploymentSpec = productSpec.getDeploymentSpec();
		if (deploymentSpec == null) {
			throw new BuildException("Can't find deployment spec site for product specification.");
		}
		
		XMLContent content = new XMLContent();
		
		String siteVersion = deploymentSpec.getSiteVersion();
		String productSpecPath = productSpec.getFile().getAbsolutePath();
		
		content.append("<property environment=\"env\"/>");
		content.append("<!-- Get BUILD_ID from environment -->");
		content.append("<condition property=\"buildid\" value=\"${env.BUILD_ID}\">");
		content.append("<isset property=\"env.BUILD_ID\" />");
		content.append("</condition>");
		content.appendLineBreak();
		
		//TODO this is not good, because the tstamp is stage dependent
		content.append("<!-- fallback if env.BUILD_ID is not set -->");
		content.append("<tstamp/>");
		content.append("<property name=\"buildid\" value=\"${DSTAMP}${TSTAMP}\" />");
		content.appendLineBreak();
	
		//set version in product file
		content.append("<replace file=\"" + productSpecPath + "\" token=\"0.0.0\" value=\"" + siteVersion + ".v${buildid}\"/>");
		
		String updateSiteID = deploymentSpec.getUpdateSite().getIdentifier();
		
		File repoBaseFolder = deploymentSpec.getUpdateSite().getFile().getParentFile().getParentFile();
		
		File tempDirFile = new File(targetDir.getParentFile(), "pde-build-temp");
		tempDirFile.mkdir();
		String tempDir = tempDirFile.getAbsolutePath();
		String eclipseDir = new File(new File(targetDir, "target-platform"), "eclipse").getAbsolutePath();
		String p2ProductRepo = new File(repoBaseFolder, updateSiteID + "-products-p2").getAbsolutePath();
		String productsDir = new File(repoBaseFolder, updateSiteID + "-products-zip").getAbsolutePath();
		
		//call PDE product build
		content.append("<exec executable=\"eclipse\" failonerror=\"true\">"); //TODO this is a platform dependent executable in the PATH
		content.append("<arg value=\"--launcher.suppressErrors\"/>");
		content.append("<arg value=\"-noSplash\"/>");
		content.append("<arg value=\"-application\"/>");
		content.append("<arg value=\"org.eclipse.ant.core.antRunner\"/>");
		content.append("<arg value=\"-buildfile\"/>");
		content.append("<arg value=\"" + eclipseDir + "/plugins/org.eclipse.pde.build_3.8.1.v20120725-202643/scripts/productBuild/productBuild.xml\"/>"); //TODO pde.build version might differ
		
		content.append("<arg value=\"-Dbuilder=" + tempDir + "\"/>");

		content.append("<arg value=\"-Dproduct=" + productSpecPath + "\"/>");

		content.append("<arg value=\"-DrepoBaseLocation=" + repoBaseFolder.getAbsolutePath() + "\"/>");
		content.append("<arg value=\"-DtransformedRepoLocation=" + tempDir + "/transformedRepo\"/>");

		content.append("<arg value=\"-DpluginPath=" + eclipseDir + "\"/>");

		content.append("<arg value=\"-DbuildDirectory=" + tempDir + "/build\"/>");
		content.append("<arg value=\"-DbaseLocation=" + tempDir + "/build\"/>");
		content.append("<arg value=\"-DbuildLabel=BuildBoost\"/>");
		content.append("<arg value=\"-DcollectingFolder=collectingFolder\"/>");

		content.append("<arg value=\"-Dp2.build.repo=file:"+ p2ProductRepo + "\"/>");
		content.append("<arg value=\"-Dp2.product.qualifier=v${buildid}\"/>");
		content.append("<arg value=\"-Dp2.gathering=true\"/>");
		content.append("<arg value=\"-Dp2.flavor=tooling\"/>");
		content.append("<arg value=\"-DskipDirector=true\"/>");
		content.append("<arg value=\"-DskipBase=true\"/>");

		String configs = deploymentSpec.getValue("site", "configs");
		content.append("<arg value=\"-Dconfigs=" + configs.replaceAll("\\&", "&amp;") + "\"/>");
		
		content.append("</exec>");

		//call director for publishing
		String[] configArray = configs.split("\\&");
		for (String conf : configArray) {
			String[] split = conf.split(",");
			String os = split[0];
			String ws = split[1];
			String arch = split[2];
			
			String productFileName = productSpec.getIdentifier() + "-" + os + "-" + ws + "-" + arch;
			
			content.append("<exec executable=\"eclipse\" failonerror=\"true\">");
			content.append("<arg value=\"--launcher.suppressErrors\"/>");
			content.append("<arg value=\"-noSplash\"/>");
			content.append("<arg value=\"-application\"/>");
			content.append("<arg value=\"org.eclipse.equinox.p2.director\"/>");
			
			String siteDependencies = deploymentSpec.getValue("site", "dependencies");
			content.append("<arg value=\"-repository\"/>");
			content.append("<arg value=\"file:" + p2ProductRepo + "," + siteDependencies + "\"/>");
			content.append("<arg value=\"-installIU\"/>");
			content.append("<arg value=\"" + productSpec.getIdentifier() + "\"/>");
			content.append("<arg value=\"-tag\"/>");
			content.append("<arg value=\"InitialState\"/>");
			content.append("<arg value=\"-destination\"/>");
			content.append("<arg value=\"" + productsDir + "/" + productFileName + "\"/>");
			content.append("<arg value=\"-profile\"/>");
			content.append("<arg value=\"EclipseProduct\"/>");
			content.append("<arg value=\"-profileProperties\"/>");
			content.append("<arg value=\"org.eclipse.update.install.features=true\"/>");
			content.append("<arg value=\"-bundlepool\"/>");
			content.append("<arg value=\"" + productsDir + "/" + productFileName + "\"/>");
			content.append("<arg value=\"-roaming\"/>");
			content.append("<arg value=\"-flavor\"/>");
			content.append("<arg value=\"tooling\"/>");
		
			content.append("<arg value=\"-p2.os\"/>");
			content.append("<arg value=\"" + os +"\"/>");
			content.append("<arg value=\"-p2.ws\"/>");
			content.append("<arg value=\"" + ws + "\"/>");
			content.append("<arg value=\"-p2.arch\"/>");
			content.append("<arg value=\"" + arch + "\"/>");
			
			content.append("</exec>");	
		}
		
		AntTarget target = new AntTarget("build-eclipse-product-" + updateSiteID, content);
		return target;
	}



}
