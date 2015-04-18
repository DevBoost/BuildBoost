package de.devboost.buildboost.util;

public class TimestampUtil {

	public void addGetBuildTimestampFromEnvironment(XMLContent content) {
		content.append("<property environment=\"env\"/>");
		content.append("<!-- Get BUILD_ID from environment -->");
		content.append("<condition property=\"buildid\" value=\"${env.BUILD_TIMESTAMP}\">");
		content.append("<isset property=\"env.BUILD_TIMESTAMP\" />");
		content.append("</condition>");
		content.appendLineBreak();
	}
}
