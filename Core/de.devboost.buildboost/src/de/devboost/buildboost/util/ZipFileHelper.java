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

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileHelper {

	public final static ZipFileHelper INSTANCE = new ZipFileHelper();

	private ZipFileHelper() {
		super();
	}

	public boolean containsZipEntry(File file, String entryName) {
		ZipFile jar = null;
		try {
			jar = new ZipFile(file);
			ZipEntry entry = jar.getEntry(entryName);
			if (entry != null) {
				return true;
			}
		} catch (IOException e) {
			// FIXME
			e.printStackTrace();
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
		return false;
	}
}
