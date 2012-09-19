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
package de.devboost.buildboost.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtil {

	public String explode(Object[] parts, String glue) {
		List<Object> list = Arrays.asList(parts);
		List<String> stringList = new ArrayList<String>(list.size());
		for (Object next : list) {
			stringList.add(next.toString());
		}
		return explode(stringList, glue);
	}
	
	public String explode(Collection<String> parts, String glue) {
		StringBuffer result = new StringBuffer();
		int size = parts.size();
		Iterator<String> iterator = parts.iterator();
		for (int i = 0; i < size; i++) {
			result.append(iterator.next());
			if (i < size - 1) {
				result.append(glue);
			}
		}
		return result.toString();
	}
}
