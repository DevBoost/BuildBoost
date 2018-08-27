/*******************************************************************************
 * Copyright (c) 2006-2018
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
package de.devboost.buildboost.genext.toolproduct.steps;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.toolproduct.IConstants;
import de.devboost.buildboost.genext.toolproduct.artifacts.ToolProductSpecification;
import de.devboost.buildboost.genext.updatesite.artifacts.EclipseUpdateSite;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.util.AntScriptUtil;
import de.devboost.buildboost.util.TimestampUtil;
import de.devboost.buildboost.util.XMLContent;

public class BuildToolProductStep extends AbstractAntTargetGenerator {

	public static final String MACOSX_4_5_OR_NEWER_REGEX = ".+4\\.[5-9](RC[1-9])?\\.?[0-9]?[a-z]?-macosx-cocoa-.+";

	private static final String LAUNCHER_WRAPPER_CLASSNAME = "de.devboost.buildboost.buildext.toolproduct.LauncherWrapper";

	// TODO Make this configurable
	private static final String INSTALLATION_PLATFORM_FILE = "eclipse-platform-4.3-linux-gtk-x86_64.tar.gz";
	private static final String INSTALLATION_PLATFORM_URL = "https://eclipse.devboost.de/downloads/drops4/R-4.3-201306052000/"
			+ INSTALLATION_PLATFORM_FILE;

	private static final String[] ICON_FILES = new String[] { "16.gif", "16.png", "32.gif", "32.png", "48.gif",
			"48.png", "256.png", "512.png", "1024.png" };

	private final ToolProductSpecification specification;

	public BuildToolProductStep(ToolProductSpecification specification) {
		this.specification = specification;
	}

	@Override
	public Collection<AntTarget> generateAntTargets() throws BuildException {
		AntTarget antTarget = generateAntTarget();
		return Collections.singletonList(antTarget);
	}

	protected AntTarget generateAntTarget() throws BuildException {
		if (specification == null) {
			throw new BuildException("Can't find tool product specification.");
		}

		Plugin buildExtPlugin = findBuildExtensionPlugin();
		if (buildExtPlugin == null) {
			throw new BuildException(
					"Can't find build extension plug-in in dependencies of tool product specification.");
		}

		XMLContent content = new XMLContent();

		new TimestampUtil().addGetBuildTimestampFromEnvironment(content);

		// TODO this is not good, because the time stamp should not be stage dependent
		content.append("<!-- fallback if env.BUILD_ID is not set -->");
		content.append("<tstamp/>");
		content.append("<property name=\"buildid\" value=\"${DSTAMP}${TSTAMP}\" />");
		content.appendLineBreak();

		EclipseUpdateSite updateSite = specification.getUpdateSite();
		File toolProductFolder = specification.getFile().getParentFile();

		String distProductsPath = "dist/products";
		content.append("<mkdir dir=\"" + distProductsPath + "\" />");

		// This directory will contain the Eclipse platform that is used to
		// install the product into the final Eclipse platform
		String installationPlatformPath = "temp/tool-product-installation-platform";
		String eclipsePath = installationPlatformPath + "/eclipse/";
		String eclipseBinary = eclipsePath + "eclipse";

		content.append("<mkdir dir=\"" + installationPlatformPath + "\" />");
		content.append("<get src=\"" + INSTALLATION_PLATFORM_URL + "\" dest=\"" + installationPlatformPath + "\" />");
		if (INSTALLATION_PLATFORM_FILE.endsWith(".zip")) {
			content.append("<unzip src=\"" + installationPlatformPath + "/" + INSTALLATION_PLATFORM_FILE + "\" dest=\""
					+ installationPlatformPath + "\" />");
		} else {
			String tarGzFile = INSTALLATION_PLATFORM_FILE;
			String tarFile = INSTALLATION_PLATFORM_FILE.substring(0, INSTALLATION_PLATFORM_FILE.length() - 3);
			content.append("<gunzip src=\"" + installationPlatformPath + "/" + tarGzFile + "\" dest=\""
					+ installationPlatformPath + "\" />");
			content.append("<untar src=\"" + installationPlatformPath + "/" + tarFile + "\" dest=\""
					+ installationPlatformPath + "\" />");
			// Make Eclipse binary executable
			content.append("<exec executable=\"chmod\" >");
			content.append("<arg value=\"u+x\" />");
			content.append("<arg value=\"" + eclipseBinary + "\" />");
			content.append("</exec>");
			// Set VM arguments to use local DNS
			content.append("<echo file=\"" + eclipsePath
					+ "eclipse.ini\" append=\"true\" message=\"-Dsun.net.spi.nameservice.provider.1=dns,localdns\" />");
		}

		File pluginsFolder = new File(installationPlatformPath + "/eclipse", "plugins");

		String productName = specification.getProductName();
		String[] featureIDs = specification.getProductFeature().split(",");

		String sdkFolderPath = "../eclipse-sdks";
		content.append("<mkdir dir=\"" + sdkFolderPath + "\" />");

		String productFolderPath = distProductsPath + "/" + productName;
		content.append("<mkdir dir=\"" + productFolderPath + "\" />");

		// Call director for publishing
		Map<String, String> configs = specification.getProductTypes();
		for (Entry<String, String> configuration : configs.entrySet()) {
			String productType = configuration.getKey();
			String url = configuration.getValue();
			generateAntTargetForConfiguration(buildExtPlugin, content, updateSite, toolProductFolder, pluginsFolder,
					productName, featureIDs, sdkFolderPath, productFolderPath, productType, url);
		}

		String specificationID = specification.getIdentifier();
		AntTarget target = new AntTarget("build-eclipse-tool-product-" + specificationID, content);
		return target;
	}

	private void generateAntTargetForConfiguration(Plugin buildExtPlugin, XMLContent content,
			EclipseUpdateSite updateSite, File toolProductFolder, File pluginsFolder, String productName,
			String[] featureIDs, String sdkFolderPath, String productFolderPath, String productType, String url) {

		String sdkZipName = url.substring(url.lastIndexOf("/") + 1);
		File sdkZipFile = new File(sdkFolderPath, sdkZipName);
		if (!sdkZipFile.exists()) {
			AntScriptUtil.addDownloadFileScript(content, url, new File(sdkFolderPath).getAbsolutePath());
		}
		content.appendLineBreak();

		File productTypeFolder = new File("temp/toolproducts/" + productName + "/" + productType);
		File productInstallationFolder = new File(productTypeFolder, "eclipse");
		File brandedProductFolder = new File(productFolderPath + "/" + productType + "/" + productName);

		// Extract product base
		content.append("<mkdir dir=\"" + productTypeFolder.getAbsolutePath() + "\" />");
		// TODO Remove this special logic if it turns out to work
		boolean isOSXgreater45 = sdkZipFile.getName().matches(MACOSX_4_5_OR_NEWER_REGEX);
		if (isOSXgreater45) {
			// Starting from Eclipse Mars, the OSX distribution does not contain a folder 'eclipse' anymore.
			// Therefore, it needs to be extracted to the subfolder 'eclipse', instead of the parent.
			// content.append("<mkdir dir=\"" + productInstallationFolder.getAbsolutePath() + "\" />");
			AntScriptUtil.addZipFileExtractionScript(content, sdkZipFile, productTypeFolder);
		} else {
			AntScriptUtil.addZipFileExtractionScript(content, sdkZipFile, productTypeFolder);
		}
		content.appendLineBreak();

		content.append("<path id=\"toolproduct.path\">");
		content.append("<pathelement location=\"" + buildExtPlugin.getLocation().getAbsolutePath() + "\"/>");
		content.append("<fileset dir=\"" + pluginsFolder.getAbsolutePath() + "\">");
		content.append("<include name=\"org.eclipse.equinox.launcher_*.jar\"/>");
		content.append("</fileset>");
		content.append("</path>");

		File productEclipseFolder = productInstallationFolder;
		// For the MAC OSX version of Eclipse (starting from 4.5) the target folder to install the feature to is
		// different, because the directory structure has changed.
		if (isOSXgreater45) {
			productEclipseFolder = new File(new File(new File(productTypeFolder, "Eclipse.app"), "Contents"),
					"Eclipse");
		}

		File configIni;
		if (isOSXgreater45) {
			configIni = new File(productEclipseFolder, "configuration/config.ini");
		} else {
			configIni = new File(productInstallationFolder, "configuration/config.ini");
		}
		// make backup of config.ini file, because the installation of the features modifies the file in way that
		// renders it unusable on other machines
		content.append("<copy overwrite=\"true\" file=\"" + configIni.getAbsolutePath() + "\" tofile=\""
				+ configIni.getAbsolutePath() + ".backup\"/>");

		addStepsToInstallFeatures(content, featureIDs, productEclipseFolder);

		File splashScreenFile = new File(toolProductFolder, "splash.bmp");
		File pluginFolder = new File(productEclipseFolder, "plugins");
		File iconFolder = new File(toolProductFolder, "icons");

		File osxAppFolder = new File(productTypeFolder, "Eclipse.app");
		File osxBrandedAppFolder = new File(productTypeFolder, productName + ".app");

		File windowsExe = null;
		if (productType.contains("64")) {
			windowsExe = new File(toolProductFolder, "eclipse64.exe");
		} else {
			windowsExe = new File(toolProductFolder, "eclipse32.exe");
		}
		File windowsBrandedExe = new File(productInstallationFolder, productName + ".exe");

		File linuxIconFile = new File(iconFolder, "icon.xpm");
		File linuxExe = new File(productInstallationFolder, "eclipse");
		File linuxBrandedExe = new File(productInstallationFolder, productName);

		File workspace = new File(toolProductFolder, "workspace");
		File uiPrefs = new File(productInstallationFolder, "configuration/.settings/org.eclipse.ui.ide.prefs");

		// copy splash
		content.append("<first id=\"platformPlugin\">");
		content.append("<dirset dir=\"" + pluginFolder.getAbsolutePath() + "\" includes=\"org.eclipse.platform_*\"/>");
		content.append("</first>");

		content.append("<copy overwrite=\"true\" failonerror=\"false\" file=\"" + splashScreenFile.getAbsolutePath()
				+ "\" todir=\"${toString:platformPlugin}\"/>");

		// copy icons
		for (String iconFormat : ICON_FILES) {
			content.append("<copy overwrite=\"true\" failonerror=\"false\" file=\""
					+ new File(iconFolder, "eclipse" + iconFormat).getAbsolutePath()
					+ "\" todir=\"${toString:platformPlugin}\"/>");
		}

		// Customize plugin.xml file of plug-in org.eclipse.platform
		for (String property : new String[] { "startupForegroundColor", "startupMessageRect", "startupProgressRect" }) {
			String value = specification.getPlatformPluginProperty(property);
			if (value == null) {
				continue;
			}
			String pattern = "<property\\s+name=\"" + property + "\"\\s+value=\"[^\"]+\"/>";
			pattern = escape(pattern);
			String newValue = "<property name=\"" + property + "\" value=\"" + value + "\"/>";
			newValue = escape(newValue);
			content.append("<echo message=\"Replacing property " + property
					+ " in file ${toString:platformPlugin}/plugin.xml\" />");
			content.append("<replaceregexp file=\"${toString:platformPlugin}/plugin.xml\" match=\"" + pattern
					+ "\" replace=\"" + newValue + "\" flags=\"gis\" />");
		}

		File eclipseIni;
		// copy icon osx
		if (productType.startsWith("osx")) {
			eclipseIni = new File(productTypeFolder, productName + ".app/Contents/MacOS/eclipse.ini");
			// copy icon osx
			// content.append("<copy overwrite=\"true\" file=\"" + osxIconFile.getAbsolutePath() + "\" todir=\"" +
			// osxIconFolder.getAbsolutePath() + "\"/>");
			// rename app folder
			content.append("<move file=\"" + osxAppFolder.getAbsolutePath() + "\" tofile=\""
					+ osxBrandedAppFolder.getAbsolutePath() + "\"/>");
			// remove command line "eclipse"
			content.append(
					"<delete file=\"" + new File(productInstallationFolder, "eclipse").getAbsolutePath() + "\"/>");
			// Adjust config.ini path after renaming the app folder
			if (isOSXgreater45) {
				configIni = new File(osxBrandedAppFolder, "Contents/Eclipse/configuration/config.ini");
			}
		} else if (productType.startsWith("win")) {
			eclipseIni = new File(productInstallationFolder, "eclipse.ini");
			// Use branded binary (if available)
			content.append("<copy overwrite=\"true\" failonerror=\"false\" file=\"" + windowsExe.getAbsolutePath()
					+ "\" tofile=\"" + windowsBrandedExe.getAbsolutePath() + "\"/>");
			// Remove default binary "eclipse" (if a branded version was provided)
			if (windowsExe.exists()) {
				content.append("<delete file=\"" + new File(productInstallationFolder, "eclipse.exe").getAbsolutePath()
						+ "\"/>");
				content.append("<delete file=\"" + new File(productInstallationFolder, "eclipsec.exe").getAbsolutePath()
						+ "\"/>");
			}
		} else {
			eclipseIni = new File(productInstallationFolder, "eclipse.ini");
			// Copy Linux icon
			content.append("<copy overwrite=\"true\" failonerror=\"false\" file=\"" + linuxIconFile.getAbsolutePath()
					+ "\" todir=\"" + productInstallationFolder.getAbsolutePath() + "\"/>");
			// Rename binary
			content.append("<move file=\"" + linuxExe.getAbsolutePath() + "\" tofile=\""
					+ linuxBrandedExe.getAbsolutePath() + "\"/>");
		}

		// Copy workspace
		content.append("<copy todir=\"" + new File(productInstallationFolder, "workspace").getAbsolutePath() + "\">");
		content.append("<fileset dir=\"" + workspace.getAbsolutePath() + "\"/>");
		content.append("</copy>");

		String userHomeWorkspace;
		String appRelativeWorkspace;
		if (productType.equals("osx")) {
			userHomeWorkspace = "osgi.instance.area.default=@user.home/Documents/workspace";
			appRelativeWorkspace = "osgi.instance.area.default=../../../workspace";
		} else {
			userHomeWorkspace = "osgi.instance.area.default=@user.home/workspace";
			appRelativeWorkspace = "osgi.instance.area.default=./workspace";
		}

		// Restore config.ini
		String configIniPath = configIni.getAbsolutePath();
		content.append("<copy overwrite=\"true\" file=\"" + configIniPath + ".backup\" tofile=\""
				+ configIniPath + "\"/>");
		if (!productType.startsWith("osx")) {
			// TODO We should also patch the .ini files for OSX, but currently we can't because this breaks the
			// signature

			// Change default workspace location
			content.append("<replace file=\"" + configIniPath + "\" token=\"" + userHomeWorkspace + "\" value=\""
					+ appRelativeWorkspace + "\"/>");
			// Change product to launch, remove application to launch (this is
			// required to make the splash screen settings work)
			content.append("<replace file=\"" + configIniPath
					+ "\" token=\"eclipse.product=org.eclipse.sdk.ide\" value=\"eclipse.product=org.eclipse.platform.ide\"/>");
			content.append("<replace file=\"" + configIniPath
					+ "\" token=\"eclipse.product=eclipse.application=org.eclipse.ui.ide.workbench\" value=\"\"/>");

			content.append("<mkdir dir=\"" + uiPrefs.getParentFile().getAbsolutePath() + "\"/>");
			content.append("<echo file=\"" + uiPrefs.getAbsolutePath()
					+ "\" message=\"SHOW_WORKSPACE_SELECTION_DIALOG=false\"/>");

			String eclipseIniPath = eclipseIni.getAbsolutePath();
			content.append("<replace file=\"" + eclipseIniPath
					+ "\" token=\"-Xmx512m\" ><replacevalue><![CDATA[-Xmx1024m]]></replacevalue></replace>");
			// Disable SSL option to make sure SVN works over HTTPS with Java 1.7
			content.append("<echo file=\"" + eclipseIniPath
					+ "\" append=\"true\" message=\"-Djsse.enableSNIExtension=false\" />");
			// Only rename the eclipse.ini for Unix and for Windows (if branded binary was provided)
			if (windowsExe.exists() && productType.startsWith("win")) {
				content.append("<move file=\"" + eclipseIniPath + "\" tofile=\""
						+ productInstallationFolder.getAbsolutePath() + "/" + productName + ".ini\"/>");
			}
		}

		// Rename base folder
		if (isOSXgreater45) {
			content.append("<move file=\"" + productInstallationFolder.getParentFile().getAbsolutePath()
					+ "\" tofile=\"" + brandedProductFolder.getAbsolutePath() + "\"/>");
		} else {
			content.append("<move file=\"" + productInstallationFolder.getAbsolutePath() + "\" tofile=\""
					+ brandedProductFolder.getAbsolutePath() + "\"/>");
		}

		File productsDistFolder = new File("dist/products");
		content.append("<mkdir dir=\"" + productsDistFolder.getAbsolutePath() + "\" />");

		String zipType;
		if (productType.startsWith("win")) {
			zipType = "zip";
		} else {
			zipType = "tar.gz";
		}

		String productArchiveFileName;
		if (updateSite != null) {
			// If the tool product is installed from a local update site,
			// we include the version of the first feature in the ZIP name
			String siteVersion = updateSite.getFeature(featureIDs[0]).getVersion();
			productArchiveFileName = productName + "-" + siteVersion + "-" + productType + "." + zipType;
		} else {
			productArchiveFileName = productName + "-" + productType + "." + zipType;
		}

		String productZipPath = new File(productsDistFolder, productArchiveFileName).getAbsolutePath();

		String brandedProductFolderParentPath = brandedProductFolder.getParentFile().getAbsolutePath();
		AntScriptUtil.addZipFileCompressionScript(content, productZipPath, brandedProductFolderParentPath);
		content.append("<delete dir=\"" + brandedProductFolderParentPath + "\"/>");
		content.appendLineBreak();

		addUploadTask(content, productsDistFolder, productArchiveFileName);
	}

	private void addStepsToInstallFeatures(XMLContent content, String[] featureIDs, File productEclipseFolder) {
		for (String featureID : featureIDs) {
			content.append(
					"<java classname=\"" + LAUNCHER_WRAPPER_CLASSNAME + "\" fork=\"true\" failonerror=\"true\">");

			content.append("<classpath refid=\"toolproduct.path\"/>");

			content.append("<jvmarg value=\"-Xms40m\"/>");
			content.append("<jvmarg value=\"-Xmx1024m\"/>");
			content.append("<jvmarg value=\"-Declipse.pde.launch=true\"/>");
			content.append("<jvmarg value=\"-Dfile.encoding=UTF-8\"/>");

			String eclipseMirror = specification.getEclipseMirror();
			if (eclipseMirror == null) {
				content.append("<arg value=\"noeclipsemirror\"/>");
			} else {
				content.append("<arg value=\"" + eclipseMirror + "\"/>");
			}

			content.append("<arg value=\"-noSplash\"/>");
			content.append("<arg value=\"-application\"/>");
			content.append("<arg value=\"org.eclipse.equinox.p2.director\"/>");

			content.append("<arg value=\"-repository\"/>");
			content.append("<arg value=\"" + getRepositoryArgument() + "\"/>");
			content.append("<arg value=\"-installIU\"/>");
			content.append("<arg value=\"" + featureID + ".feature.group\"/>");
			content.append("<arg value=\"-tag\"/>");
			content.append("<arg value=\"InstallationOf_" + featureID + "\"/>");
			content.append("<arg value=\"-destination\"/>");
			content.append("<arg value=\"" + productEclipseFolder.getAbsolutePath() + "\"/>");
			content.append("<arg value=\"-profile\"/>");
			content.append("<arg value=\"SDKProfile\"/>");

			content.append("</java>");
			content.appendLineBreak();
		}
	}

	private void addUploadTask(XMLContent content, File productsDistFolder, String productArchiveFileName) {
		String usernameProperty = specification.getUploadUsernameProperty();
		if (usernameProperty == null) {
			content.append(
					"<!-- Can't copy tool product archive to server. Username property for upload is missing. -->");
			return;
		}

		String passwordProperty = specification.getUploadPasswordProperty();
		if (passwordProperty == null) {
			content.append(
					"<!-- Can't copy tool product archive to server. Password property for upload is missing. -->");
			return;
		}

		String targetPath = specification.getUploadTargetPathProperty();
		if (targetPath == null) {
			content.append("<!-- Can't copy tool product archive to server. Target path for upload is missing. -->");
			return;
		}

		content.append("<!-- Copy tool product archive to server -->");
		content.append("<scp todir=\"${env." + usernameProperty + "}:${env." + passwordProperty + "}@" + targetPath
				+ "\" port=\"22\" sftp=\"true\" trust=\"true\">");
		content.append("<fileset dir=\"" + productsDistFolder.getAbsolutePath() + "\">");
		content.append("<include name=\"" + productArchiveFileName + "\"/>");
		content.append("</fileset>");
		content.append("</scp>");
		content.appendLineBreak();
	}

	private String getRepositoryArgument() {
		StringBuilder argument = new StringBuilder();

		EclipseUpdateSite updateSite = specification.getUpdateSite();
		if (updateSite != null) {
			File deployedUpdateSiteFolder = updateSite.getFile().getParentFile();
			argument.append("file:" + deployedUpdateSiteFolder.getAbsolutePath());
		}

		String associateSites = specification.getAssociateSites();
		if (associateSites != null) {
			if (!argument.toString().isEmpty()) {
				argument.append(",");
			}
			argument.append(associateSites);
		}
		return argument.toString();
	}

	private String escape(String text) {
		String result = text.replace("\"", "&quot;");
		result = result.replace(">", "&gt;");
		result = result.replace("<", "&lt;");
		return result;
	}

	private Plugin findBuildExtensionPlugin() {
		Collection<IDependable> dependencies = specification.getDependencies();
		for (IDependable dependable : dependencies) {
			if (dependable instanceof Plugin) {
				Plugin plugin = (Plugin) dependable;
				if (IConstants.BUILDEXT_PLUGIN_ID.equals(plugin.getIdentifier())) {
					return plugin;
				}
			}
		}

		return null;
	}
}
