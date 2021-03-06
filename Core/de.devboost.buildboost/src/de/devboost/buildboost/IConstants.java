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
package de.devboost.buildboost;

/**
 * A simple interface to holds constants that are required by the BuildBoost system.
 */
public interface IConstants {

	String BUILD_BOOST_REPOSITORY_URL = "https://github.com/DevBoost/BuildBoost.git";
	String BUILD_BOOST_CORE_PROJECT_ID = "de.devboost.buildboost";
	String BUILD_BOOST_GENEXT_PROJECT_ID_PATTERN = ".*\\.buildboost\\.genext\\..*";
	String BUILD_BOOST_BUILD_PROJECT_ID_PATTERN = ".*\\.build";

	String REPOS_FOLDER = "repos";
	String BUILD_FOLDER = "build";
	String ARTIFACTS_FOLDER = "artifacts";
	String DIST_FOLDER = "dist";
	String PROJECTS_FOLDER = "projects";
	String TARGET_PLATFORM_FOLDER = "target-platform";
	String BUILD_BOOST_BIN_FOLDER = "build-boost-bin";

	String NL = System.getProperty("line.separator");
}
