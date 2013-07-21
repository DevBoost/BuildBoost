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
package de.devboost.buildboost.buildext.toolproduct;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.p2.director.app.DirectorApplication;

/**
 * The {@link DirectorWrapper} is a wrapper for the p2
 * {@link DirectorApplication} that uses a special DNS to retrieve all artifacts
 * from localhost.
 */
@SuppressWarnings("restriction")
public class DirectorWrapper implements IApplication {
	
	private DirectorApplication delegate = new DirectorApplication();

	@Override
	public Object start(IApplicationContext context) throws Exception {
		// Configure VM to use local DNS
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,localdns");
		
		return delegate.start(context);
	}

	@Override
	public void stop() {
		delegate.stop();
	}
}
