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
package de.devboost.buildboost.stages;

import java.io.File;
import java.util.Collection;

import de.devboost.buildboost.AutoBuilder;
import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.BuildException;
import de.devboost.buildboost.ant.AntScript;
import de.devboost.buildboost.ant.AntTarget;
import de.devboost.buildboost.discovery.RepositoriesFileFinder;
import de.devboost.buildboost.steps.clone.CloneRepositoriesBuildStepProvider;

public class CloneRepositoriesStage extends AbstractBuildStage {

	private String reposFolder;

	public void setReposFolder(String reposFolder) {
		this.reposFolder = reposFolder;
	}

	public AntScript getScript() throws BuildException {
		BuildContext context = createContext(true);
		
		context.addBuildParticipant(
				new RepositoriesFileFinder(new File(reposFolder)));
		context.addBuildParticipant(
				new CloneRepositoriesBuildStepProvider(new File(reposFolder)));
		
		AutoBuilder builder = new AutoBuilder(context);
		Collection<AntTarget> targets = builder.generateAntTargets();

		AntScript copyScript = new AntScript();
		copyScript.setName("Clone repositories stage");
		copyScript.addTargets(targets);
		
		return copyScript;
	}

}
