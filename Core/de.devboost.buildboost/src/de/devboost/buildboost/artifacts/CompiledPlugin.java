package de.devboost.buildboost.artifacts;

import java.io.File;

public class CompiledPlugin extends Plugin {

	public CompiledPlugin(File location) throws Exception {
		super(location);
	}
	
	public String getVersion() {
		String fileName = getFile().getName();
		int beginIdx = fileName.indexOf("_") + 1;
		int endIdx = fileName.lastIndexOf(".v");
		String version = fileName.substring(beginIdx, endIdx);
		return version;
	}
	
	@Override
	public boolean isProject() {
		return false;
	}

}
