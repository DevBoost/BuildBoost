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
package de.devboost.buildboost.util;

import java.io.File;

/**
 * A utility class that collects reusable pieces of ANT script.
 */
public class AntScriptUtil {

	public static void addZipFileExtractionScript(XMLContent content, File file, File targetDir) {
		if (file.getName().endsWith(".zip")) {
			content.append("<unzip src=\"" + file.getAbsolutePath() + "\" dest=\"" + targetDir.getAbsolutePath() + "\" />");			
		} else {
			content.append("<exec executable=\"tar\" dir=\"" + targetDir.getAbsolutePath() +  "\" failonerror=\"true\">");
			content.append("<arg value=\"zxf\"/>");
			content.append("<arg value=\"" + file.getAbsolutePath() + "\"/>");
			content.append("</exec>");
		}
	}

	public static void addZipFileCompressionScript(XMLContent content,
			String zipFile, String folderToZip) {
		if (zipFile.endsWith(".zip")) {
			content.append("<zip destfile=\"" + zipFile  + "\" basedir=\""+ folderToZip + "\" />");
		} else {
			content.append("<exec executable=\"tar\" dir=\""+ folderToZip + "\" failonerror=\"true\">");
			content.append("<arg value=\"cvzf\"/>");
			content.append("<arg value=\"" + zipFile + "\"/>");
			content.append("<arg value=\".\"/>");
			content.append("</exec>");
		}
	}

	public static void addDownloadFileScript(XMLContent content, String url,
			String destination) {
		content.append("<get src=\""+ url + "\" dest=\""+ destination + "\"/>");
	}

}
