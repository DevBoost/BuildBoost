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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.devboost.buildboost.BuildContext;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.IBuildStage;

public abstract class AbstractBuildStage implements IBuildStage {

	private List<IBuildParticipant> participants = new ArrayList<IBuildParticipant>();
	private boolean enabled = true;

	public void addBuildParticipant(IBuildParticipant participant) {
		participants.add(participant);
	}

	public void addBuildParticipants(Collection<IBuildParticipant> participants) {
		this.participants.addAll(participants);
	}

	protected BuildContext createContext(boolean ignoreUnresolvedDependencies) {
		BuildContext context = new BuildContext();
		context.setIgnoreUnresolvedDependencies(ignoreUnresolvedDependencies);
		for (IBuildParticipant participant : participants) {
			context.addBuildParticipant(participant);
		}
		return context;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
