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
package de.devboost.buildboost.test;

import java.util.List;

import junit.framework.TestCase;
import de.devboost.buildboost.model.IBuildConfiguration;
import de.devboost.buildboost.model.IBuildStage;

public class ReflectionTest extends TestCase implements IBuildConfiguration {

	public static void main(String[] args) {
		
	}
	
	public void testFindMain() {
		try {
			ReflectionTest.class.getMethod("main", String[].class);
		} catch (SecurityException e) {
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			fail(e.getMessage());
		}
	}
	
	public void testCasting() {
		boolean assignableFrom = IBuildConfiguration.class.isAssignableFrom(ReflectionTest.class);
		assertTrue(assignableFrom);
	}

	@Override
	public List<IBuildStage> getBuildStages(String workspace) {
		return null;
	}
}
