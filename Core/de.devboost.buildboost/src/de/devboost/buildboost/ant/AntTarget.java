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
package de.devboost.buildboost.ant;

import java.util.Collection;
import java.util.LinkedHashSet;

import de.devboost.buildboost.artifacts.AbstractArtifact;
import de.devboost.buildboost.util.XMLContent;

/**
 * An {@link AntTarget} is a representation of a generated Ant script fragment, namely an ANT target element.
 * 
 * TODO Add parameter Class<?> creator to all constructors to avoid duplicate names for generated targets.
 */
public class AntTarget extends AbstractArtifact {

	private static final long serialVersionUID = -7935166108004658132L;

	private String name;
	private String content;
	private Collection<String> dependencies;

	private String ifConditions;
	private String unlessConditions;

	// TODO the content must not change after calling this constructor as
	// changes will not be reflected because we call toString() right here.
	public AntTarget(String name, XMLContent content) {
		this(name, content.toString());
	}

	public AntTarget(String name, XMLContent content, Collection<String> dependencies) {

		this(name, content.toString(), dependencies);
	}

	private AntTarget(String name, String content) {
		this(name, content, new LinkedHashSet<String>());
	}

	private AntTarget(String name, String content, Collection<String> dependencies) {

		super();
		if (name.contains(",")) {
			throw new RuntimeException("Names for ANT targets must not contain the ',' character, but '" + name
					+ "' does.");
		}
		this.name = name;
		this.content = content;
		this.dependencies = dependencies;
	}

	public void setIfConditions(String ifConditions) {
		this.ifConditions = ifConditions;
	}

	public void setUnlessConditions(String unlessConditions) {
		this.unlessConditions = unlessConditions;
	}

	/**
	 * Returns the name of this target.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the script content of this target as string.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the targets this target depends on.
	 */
	public Collection<String> getRequiredTargets() {
		return dependencies;
	}

	public String getIfConditions() {
		return ifConditions;
	}

	public String getUnlessConditions() {
		return unlessConditions;
	}
}
