package de.devboost.buildboost.genext.cmdlineapp.stages;

import java.io.File;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.discovery.EclipseTargetPlatformAnalyzer;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.genext.cmdlineapp.steps.CommandlineAppPackagingStepProvider;
import de.devboost.buildboost.model.IUniversalBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;

public class CommandlineAppPackagingStage extends AbstractBuildStage implements IUniversalBuildStage {

	private String artifactsFolder;

	public void setArtifactsFolder(String artifactsFolder) {
		this.artifactsFolder = artifactsFolder;
	}
	
	@Override
	public AntScript getScript() throws BuildException {
		BuildContext context = createContext(false);
	
		context.addBuildParticipant(new EclipseTargetPlatformAnalyzer(new File(artifactsFolder)));
		context.addBuildParticipant(new PluginFinder(new File(artifactsFolder)));
		context.addBuildParticipant(new CommandlineAppPackagingStepProvider());
		
		AutoBuilder builder = new AutoBuilder(context);
		AntScript script = new AntScript();
		script.setName("Create executable JARs for command line applications");
		script.addTargets(builder.generateAntTargets());
		
		return script;
	}

	@Override
	public int getPriority() {
		return 11002; // Used this because WebAppPackagingStage uses 11000 
	}
}
