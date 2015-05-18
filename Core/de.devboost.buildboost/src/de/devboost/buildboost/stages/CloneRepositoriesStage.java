/*******************************************************************************
 * Copyright (c) 2006-2015
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
package de.devboost.buildboost.stages;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.discovery.RepositoriesFileFinder;
import de.devboost.buildboost.steps.clone.CloneRepositoriesBuildStepProvider;
import de.devboost.buildboost.util.XMLContent;

/**
 * The {@link CloneRepositoriesStage} was used to clone all required repositories during the build. As it turned out
 * that this approach is incompatible with the Jenkins SCM plug-in concept, the stage should not be used anymore.
 * Rather, the repositories need to be cloned by the BuildBoost Jenkins SCM plug-in (version 2).
 */
@Deprecated
public class CloneRepositoriesStage extends AbstractBuildStage {

	private String reposFolderPath;

	public void setReposFolder(String reposFolder) {
		this.reposFolderPath = reposFolder;
	}

	public AntScript getScript() throws BuildException {
		BuildContext context = createContext(true);

		Collection<AntTarget> targets = generateAntTargets(context);

		AntScript script = new AntScript();
		script.setName("Clone repositories stage");
		script.addTargets(targets);

		return script;
	}

	private Collection<AntTarget> generateAntTargets(BuildContext context) throws BuildException {
		File reposFolder = new File(reposFolderPath);
		String DO_NOT_CLONE_FILE = "do_not_clone_repositories";
		if (new File(reposFolder, DO_NOT_CLONE_FILE).exists()) {
			XMLContent content = new XMLContent();
			content.append("<echo message=\"Found file '" + DO_NOT_CLONE_FILE + "'. Skipping cloning of projects.\" />");
			AntTarget target = new AntTarget("no-cloning-required", content);
			// This flag file is created by version 2 of the Jenkins BuildBoost plug-in to signal that cloning the
			// projects was already performed.
			return Collections.singleton(target);
		}

		context.addBuildParticipant(new RepositoriesFileFinder(reposFolder));
		context.addBuildParticipant(new CloneRepositoriesBuildStepProvider(reposFolder));

		AutoBuilder builder = new AutoBuilder(context);
		Collection<AntTarget> targets = builder.generateAntTargets();
		return targets;
	}

	@Override
	public int getPriority() {
		// TODO Is this correct?
		return 0;
	}
}
