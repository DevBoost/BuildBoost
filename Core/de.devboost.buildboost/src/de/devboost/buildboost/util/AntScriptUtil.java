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
			content.append("<exec executable=\"tar\" dir=\".\" failonerror=\"true\">");
			content.append("<arg value=\"cvzf\"/>");
			content.append("<arg value=\"" + zipFile + "\"/>");
			content.append("<arg value=\"" + folderToZip + "\"/>");
			content.append("</exec>");
		}
	}

	public static void addDownloadFileScript(XMLContent content, String url,
			String destination) {
		content.append("<get src=\""+ url + "\" dest=\""+ destination + "\"/>");
	}

}
