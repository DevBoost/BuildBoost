package de.devboost.buildboost.artifacts;


public class Package extends AbstractArtifact {

	private final Plugin exportingPlugin;

	public Package(String packageName, Plugin exportingPlugin) {
		setIdentifier(packageName);
		this.exportingPlugin = exportingPlugin;
		addDependency(exportingPlugin);
	}

	public Plugin getExportingPlugin() {
		return exportingPlugin;
	}
	
}