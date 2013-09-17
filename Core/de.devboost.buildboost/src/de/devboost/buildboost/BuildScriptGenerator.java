/*******************************************************************************
 * Copyright (c) 2006-2013
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
package de.devboost.buildboost;

import static de.devboost.buildboost.IConstants.ARTIFACTS_FOLDER;
import static de.devboost.buildboost.IConstants.BUILD_BOOST_BIN_FOLDER;
import static de.devboost.buildboost.IConstants.BUILD_BOOST_BUILD_PROJECT_ID_PATTERN;
import static de.devboost.buildboost.IConstants.BUILD_BOOST_CORE_PROJECT_ID;
import static de.devboost.buildboost.IConstants.BUILD_BOOST_GENEXT_PROJECT_ID_PATTERN;
import static de.devboost.buildboost.IConstants.BUILD_FOLDER;
import static de.devboost.buildboost.IConstants.REPOS_FOLDER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.devboost.buildboost.ant.AbstractAntTargetGenerator;
import de.devboost.buildboost.ant.AbstractAntTargetGeneratorProvider;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.ant.IAntTargetGenerator;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.discovery.PluginFinder;
import de.devboost.buildboost.filters.IdentifierFilter;
import de.devboost.buildboost.filters.IdentifierRegexFilter;
import de.devboost.buildboost.filters.OrFilter;
import de.devboost.buildboost.model.IArtifact;
import de.devboost.buildboost.model.IBuildConfiguration;
import de.devboost.buildboost.model.IBuildContext;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.IBuildStage;
import de.devboost.buildboost.stages.AbstractBuildStage;
import de.devboost.buildboost.stages.CloneRepositoriesStage;
import de.devboost.buildboost.stages.CompileStage;
import de.devboost.buildboost.stages.CopyProjectsStage;
import de.devboost.buildboost.util.XMLContent;

public class BuildScriptGenerator implements IBuildConfiguration {
	
	private class MergeBootstrapBinariesStage extends AbstractBuildStage {

		private String sourcePath;
		private String buildBoostBinDir;
		
		public MergeBootstrapBinariesStage(String sourcePath, String buildBoostBinDir) {
			super();
			this.sourcePath = sourcePath;
			this.buildBoostBinDir = buildBoostBinDir;
		}

		@Override
		public AntScript getScript() throws BuildException {
			File buildDir = new File(sourcePath);

			BuildContext context = createContext(true);
			context.addBuildParticipant(new PluginFinder(buildDir));
			
			context.addBuildParticipant(new MergeBinariesStepProvider(buildBoostBinDir));
			AutoBuilder builder = new AutoBuilder(context);
			
			AntScript script = new AntScript();
			script.setName("Merge bootstrapped binaries");
			script.addTargets(builder.generateAntTargets());
			
			return script;
		}

		@Override
		public int getPriority() {
			// TODO Is this correct?
			return 0;
		}
	}
	
	private class MergeBinariesStepProvider extends AbstractAntTargetGeneratorProvider {

		private String buildBoostBinDir;

		public MergeBinariesStepProvider(String buildBoostBinDir) {
			this.buildBoostBinDir = buildBoostBinDir;
		}

		@Override
		public List<IAntTargetGenerator> getAntTargetGenerators(IBuildContext context,
				IArtifact artifact) {
			if (artifact instanceof Plugin) {
				Plugin plugin = (Plugin) artifact;
				if (plugin.isProject()) {
					List<IAntTargetGenerator> steps = new ArrayList<IAntTargetGenerator>();
					steps.add(new MergeBinariesStep(plugin, buildBoostBinDir));
					return steps;
				}
			}
			return Collections.emptyList();
		}
	}
	
	private class MergeBinariesStep extends AbstractAntTargetGenerator {

		private Plugin plugin;
		private String buildBoostBinDir;

		public MergeBinariesStep(Plugin plugin, String buildBoostBinDir) {
			this.plugin = plugin;
			this.buildBoostBinDir = buildBoostBinDir;
		}

		@Override
		public Collection<AntTarget> generateAntTargets() throws BuildException {
			XMLContent content = new XMLContent();
			Set<Plugin> dependencies = plugin.getAllDependencies();
			content.append("<!-- DEPENDENCIES: " + dependencies + " -->");
			copyToSharedBinFolder(content, plugin);
			for (Plugin dependency : dependencies) {
				copyToSharedBinFolder(content, dependency);
			}
			
			String identifier = plugin.getIdentifier();
			AntTarget target = new AntTarget("merge-bin-" + identifier, content);
			return Collections.singleton(target);
		}

		private void copyToSharedBinFolder(XMLContent content, Plugin plugin) {
			File pluginFile = plugin.getFile();
			File binFolder;
			File tempDir = new File(buildBoostBinDir, "temp");
			if (!plugin.isProject()) {
				content.append("<mkdir dir=\"" + tempDir + "\" />");
				content.append("<unzip src=\"" + pluginFile.getAbsolutePath() + "\" dest=\"" + tempDir + "\" />");
				binFolder = tempDir;
			} else {
				binFolder = pluginFile;
			}
			content.append("<copy todir=\"" + buildBoostBinDir + "\">");
			content.append("<fileset dir=\"" + binFolder + "\" />");
			content.append("</copy>");
			
			Set<String> libs = plugin.getLibs();
			for (String lib : libs) {
				if (plugin.isProject()) {
					String absoluteLibPath = plugin.getAbsoluteLibPath(lib);
					content.append("<unzip src=\"" + absoluteLibPath + "\" dest=\"" + buildBoostBinDir + "\" />");
				} else {
					content.append("<unzip src=\"" + tempDir + "/" + lib + "\" dest=\"" + buildBoostBinDir + "\" />");
				}
			}
			content.append("<delete dir=\"" + tempDir + "\" />");
		}
	}

	@Override
	public List<IBuildStage> getBuildStages(String workspace) {
		File buildFolder = new File(workspace, BUILD_FOLDER);
		File buildBoostBinFolder = new File(buildFolder, BUILD_BOOST_BIN_FOLDER);
		File reposFolder = new File(workspace, REPOS_FOLDER);
		File artifactsFolder = new File(buildFolder, ARTIFACTS_FOLDER);

		List<IBuildStage> stages = new ArrayList<IBuildStage>();
		
		CloneRepositoriesStage stage1 = new CloneRepositoriesStage();
		stage1.setReposFolder(reposFolder.getAbsolutePath());

		// update a second time, since the first update might have revealed new
		// '.repository' files
		// TODO This is not correct and it also makes the builds slow. There 
		// can be new '.repositories' files after the second clone and this 
		// might go on even further. We need to come up with a more clever 
		// solution here.
		CloneRepositoriesStage stage2 = new CloneRepositoriesStage();
		stage2.setReposFolder(reposFolder.getAbsolutePath());
		
		CopyProjectsStage stage3 = new CopyProjectsStage();
		stage3.setReposFolder(reposFolder.getAbsolutePath());
		stage3.setArtifactsFolder(artifactsFolder.getAbsolutePath());
		stage3.addBuildParticipant(createFilter());
		
		CompileStage stage4 = new CompileStage();
		stage4.setArtifactsFolder(artifactsFolder.getAbsolutePath());
		stage4.addBuildParticipant(createFilter());
		
		MergeBootstrapBinariesStage stage5 = new MergeBootstrapBinariesStage(
				artifactsFolder.getAbsolutePath(), buildBoostBinFolder.getAbsolutePath());
		stage5.addBuildParticipant(createFilter());
		
		stages.add(stage1);
		stages.add(stage2);
		stages.add(stage3);
		stages.add(stage4);
		stages.add(stage5);
		return stages;
	}

	private IBuildParticipant createFilter() {
		IdentifierFilter filter1 = new IdentifierFilter(BUILD_BOOST_CORE_PROJECT_ID);
		IdentifierRegexFilter filter2 = new IdentifierRegexFilter(BUILD_BOOST_GENEXT_PROJECT_ID_PATTERN);
		IdentifierRegexFilter filter3 = new IdentifierRegexFilter(BUILD_BOOST_BUILD_PROJECT_ID_PATTERN);
		return new OrFilter(filter1, filter2, filter3);
	}
}
