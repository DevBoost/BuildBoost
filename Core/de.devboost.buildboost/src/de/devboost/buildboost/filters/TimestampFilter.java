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
package de.devboost.buildboost.filters;


import de.devboost.buildboost.model.IArtifact;

/**
 * Only accepts artifacts that have a time stamp after the given
 * one or artifacts that have no time stamp (-1);
 * 
 * TODO currently unused / for incremental build
 */
public class TimestampFilter extends AbstractFilter {
	
	private long timestamp;
	
	public TimestampFilter(long timestamp) {
		this.timestamp = timestamp;
	}
	
	@Override
	public boolean accept(IArtifact artifact) {
		if (artifact.getTimestamp() < 0) {
			return true;
		}
		else {
			return timestamp < artifact.getTimestamp();
		}
	}

}
