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
package de.devboost.buildboost.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MD5Utils {

	public final static MD5Utils INSTANCE = new MD5Utils();

	private static final Logger logger = Logger.getLogger(MD5Utils.class.getName());

	private MD5Utils() {
	}

	/**
	 * Computes the MD5 hash code of the given input string by converting it to a byte array (using UTF-8 as encoding).
	 * 
	 * @param input
	 *            the string to compute the hash code for
	 * @return the MD5 hash code as hex string (or <code>null</code> if the hash code cannot be computed).
	 */
	public String computeMD5(String input) {
		if (input == null) {
			return null;
		}
		try {
			byte[] bytesOfMessage = input.getBytes("UTF-8");
			return computeMD5(bytesOfMessage);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "Can't compute MD5 hashcode (UnsupportedEncodingException).", e);
		}
		return null;
	}

	/**
	 * Computes the MD5 hash code of the given input array.
	 * 
	 * @param input
	 *            the byte array to compute the hash code for
	 * @return the MD5 hash code as hex string (or <code>null</code> if the hash code cannot be computed).
	 */
	public String computeMD5(byte[] input) {
		if (input == null) {
			return null;
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(input);
			return bytesToHex(digest);
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.SEVERE, "Can't compute MD5 hashcode (NoSuchAlgorithmException).", e);
		}
		return null;
	}

	// This is from http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	private String bytesToHex(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
