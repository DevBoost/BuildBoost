package de.devboost.buildboost.steps.clone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLToFolderConverter {
	
	public final static URLToFolderConverter INSTANCE = new URLToFolderConverter();

	private static final String CREDENTIALS_REGEX = "\\{([a-z|A-Z|_]+)\\}:\\{([a-z|A-Z|_]+)\\}@";
	private static final Pattern CREDENTIALS_PATTERN = Pattern.compile(CREDENTIALS_REGEX);
	
	private URLToFolderConverter() {
	}

	/**
	 * Converts the given URL to a folder names. This is used to compute the
	 * names for the folder where to checkout the repository with the given URL 
	 * to.
	 * 
	 * @param url a URL of a repository
	 * @return a folder name
	 */
	public String url2FolderName(String url) {
		int idx;
		String folderName = url;
		
		// Cut leading protocol
		idx = folderName.indexOf("//");
		if (idx != -1) {
			folderName = folderName.substring(idx + 2);
		}
		
		// Cut arguments
		idx = folderName.indexOf("?");
		if (idx != -1) {
			folderName = folderName.substring(0, idx);
		}
		
		// Remove credential place holders
		folderName = removeCredentialPlaceholders(folderName);
		
		// Replace special character that are not allows in folder names
		folderName = folderName.replace(":", "");
		folderName = folderName.replace("~", "_");
		folderName = folderName.replace("@", "_");
		folderName = folderName.replace("/", "_");
		folderName = folderName.replace("\\", "_");
		folderName = folderName.replace(" ", "-");
		return folderName;
	}

	public String url2RootFolderName(String locationURL) {
		return url2FolderName(locationURL.substring(locationURL.lastIndexOf("/") + 1));
	}

	protected String removeCredentialPlaceholders(String path) {
		return path.replaceAll(CREDENTIALS_REGEX, "");
	}

	protected boolean containsCredentialPlaceholders(String path) {
		return !path.equals(removeCredentialPlaceholders(path));
	}


	protected String getUsername(String path) {
		return getCredentialVar(path, 1);
	}

	protected String getPasswordVar(String path) {
		return getCredentialVar(path, 2);
	}
	
	private String getCredentialVar(String path, int group) {
		Matcher matcher = CREDENTIALS_PATTERN.matcher(path);
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}
}
