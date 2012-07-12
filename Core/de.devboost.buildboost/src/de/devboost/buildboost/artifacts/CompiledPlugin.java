package de.devboost.buildboost.artifacts;

import java.io.File;

public class CompiledPlugin extends Plugin {

	public CompiledPlugin(File location) throws Exception {
		super(location);
	}
	
	@Override
	public boolean isProject() {
		return false;
	}

}
