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

import org.eclipse.equinox.launcher.Main;

import de.devboost.buildboost.buildext.toolproduct.localdns.LocalNameService;

/**
 * The {@link LauncherWrapper} is a wrapper for the Eclipse launcher that
 * activates a special DNS service which resolves all domain names to a single
 * IP address.
 */
@SuppressWarnings("restriction")
public class LauncherWrapper {

	public static void main(String[] args) {
		// We use the first argument as host IP
		String hostAddressString = args[0];
		String[] parts = hostAddressString.split("\\.");
		byte[] hostAddress = new byte[4];
		for (int i = 0; i < hostAddress.length; i++) {
			hostAddress[i] = (byte) Integer.parseInt(parts[i]);
		}
		LocalNameService.HOST = hostAddress;
		
		// Configure VM to use local DNS
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,localdns");
		
		// Pass remaining arguments to Eclipse launcher
		String[] otherArgs = new String[args.length - 1];
		System.arraycopy(args, 1, otherArgs, 0, otherArgs.length);
		Main.main(otherArgs);
	}
}
