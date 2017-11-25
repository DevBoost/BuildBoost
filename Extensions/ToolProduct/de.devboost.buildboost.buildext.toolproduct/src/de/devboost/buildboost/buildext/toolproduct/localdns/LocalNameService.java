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
package de.devboost.buildboost.buildext.toolproduct.localdns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import sun.net.spi.nameservice.NameService;

/**
 * A {@link LocalNameService} can be used to resolve all DNS requests to one
 * particular host. The address of this host can be set in the field
 * {@link #HOST}.
 */
public class LocalNameService implements NameService {
	
	/**
	 * The IP address of the host to which all DNS requests shall be resolved.
	 */
	public static byte[] HOST;

	/**
	 * Returns {@link #HOST} regardless of the name.
	 */
	@Override
	public InetAddress[] lookupAllHostAddr(String name) {
		if (name != null && name.endsWith(".eclipse.org")) {
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByAddress(HOST);
				return new InetAddress[] {inetAddress};
			} catch (UnknownHostException e) {
				return null;
			}
		}
		throw new UnknownHostException();
	}

	/**
	 * Returns null as reverse look up is not supported.
	 */
	@Override
	public String getHostByAddr(byte[] bs) {
		return null;
	}
}
