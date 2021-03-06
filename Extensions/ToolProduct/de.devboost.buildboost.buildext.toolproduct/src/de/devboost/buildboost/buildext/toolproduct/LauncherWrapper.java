/*******************************************************************************
 * Copyright (c) 2006-2017
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
package de.devboost.buildboost.buildext.toolproduct;

import org.eclipse.equinox.launcher.Main;

import de.devboost.buildboost.buildext.toolproduct.localdns.LocalNameService;

/**
 * The {@link LauncherWrapper} is a wrapper for the Eclipse launcher that activates a special DNS service which resolves 'eclipse.org' 
 * domain names to the configured IP address of an Eclipse mirror server.
 */
@SuppressWarnings("restriction")
public class LauncherWrapper {

	public static void main(String[] args) {
		// We use the first argument as host IP
		String hostAddressString = args[0];
		if (!"noeclipsemirror".equals(hostAddressString)) {
			configureLocalDNS(hostAddressString);
		}
		
		// Pass remaining arguments to Eclipse launcher
		String[] otherArgs = new String[args.length - 1];
		System.arraycopy(args, 1, otherArgs, 0, otherArgs.length);
		Main.main(otherArgs);
	}

	private static void configureLocalDNS(String hostAddressString) {
		String[] parts = hostAddressString.split("\\.");
		byte[] hostAddress = new byte[4];
		for (int i = 0; i < hostAddress.length; i++) {
			hostAddress[i] = (byte) Integer.parseInt(parts[i]);
		}
		LocalNameService.HOST = hostAddress;
		
		// Configure VM to use local DNS preferred over default (i.e., system) DNS to resolve eclipse.org domains to Eclipse
		// mirror hosted by DevBoost. All other domains must be resolved using the default DNS.
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,localdns");
		System.setProperty("sun.net.spi.nameservice.provider.2", "default");
	}
}
