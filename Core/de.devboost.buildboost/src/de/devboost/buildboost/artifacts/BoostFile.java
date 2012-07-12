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
package de.devboost.buildboost.artifacts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class BoostFile extends AbstractArtifact {

	private File file;
	private List<String> locations;

	public BoostFile(File file) {
		this.file = file;
		setIdentifier(file.getName());
		readContent(file);
	}

	private void readContent(File file) {
		//TODO also read properties
		locations = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String location;
			while((location = reader.readLine()) != null) {
				locations.add(location);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return file;
	}

	@Override
	public long getTimestamp() {
		return file.lastModified();
	}

	public List<String> getLocations() {
		return locations;
	}
}
