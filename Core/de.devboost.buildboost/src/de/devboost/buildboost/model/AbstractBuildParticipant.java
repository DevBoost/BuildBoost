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
package de.devboost.buildboost.model;

public abstract class AbstractBuildParticipant implements IBuildParticipant {

	@Override
	public boolean dependsOn(IBuildParticipant otherParticipant) {
		return false;
	}

	@Override
	public boolean isReqiredFor(IBuildParticipant otherParticipant) {
		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
