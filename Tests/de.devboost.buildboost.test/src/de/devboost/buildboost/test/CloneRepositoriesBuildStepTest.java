/*******************************************************************************
 * Copyright (c) 2006-2014
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import de.devboost.buildboost.artifacts.RepositoriesFile.Location;
import de.devboost.buildboost.steps.clone.CloneRepositoriesBuildStep;

public class CloneRepositoriesBuildStepTest {

	private static class AccessibleCloneRepositoriesBuildStep extends
			CloneRepositoriesBuildStep {

		private AccessibleCloneRepositoriesBuildStep(File reposFolder,
				Collection<Location> locations) {
			super(reposFolder, locations);
		}

		@Override
		public String removeCredentialPlaceholders(String folderName) {
			return super.removeCredentialPlaceholders(folderName);
		}
		
		@Override
		public boolean containsCredentialPlaceholders(String path) {
			return super.containsCredentialPlaceholders(path);
		}
		
		@Override
		public String getUsernameVar(String path) {
			return super.getUsernameVar(path);
		}
		
		@Override
		public String getPasswordVar(String path) {
			return super.getPasswordVar(path);
		}
	}

	@Test
	public void testCredentialHandling() {
		AccessibleCloneRepositoriesBuildStep step = new AccessibleCloneRepositoriesBuildStep(null, null);
		String path = "http://{domain_user}:{domain_pass}@www.domain.com";
		String result = step.removeCredentialPlaceholders(path);
		assertEquals("http://www.domain.com", result);
		assertTrue(step.containsCredentialPlaceholders(path));
		assertEquals("domain_user", step.getUsernameVar(path));
		assertEquals("domain_pass", step.getPasswordVar(path));
	}
}
