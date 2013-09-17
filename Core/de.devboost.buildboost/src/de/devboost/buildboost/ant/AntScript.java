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
package de.devboost.buildboost.ant;

import static de.devboost.buildboost.IConstants.NL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.devboost.buildboost.util.XMLContent;

/**
 * An {@link AntScript} is a representation of a generated Ant script.
 */
public class AntScript {

	private String name;
	private StringBuffer content = new StringBuffer();

	private Collection<AntTarget> targets = new ArrayList<AntTarget>();

	public String getScript() {
		XMLContent script = new XMLContent();
		script.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		script.append("<project basedir=\".\" default=\"execute-all-targets\" name=\"" + name + "\">");
		script.append("<property environment=\"env\"/>");
		script.append(content.toString());

		StringBuilder depends = new StringBuilder();
		List<AntTarget> targets = new ArrayList<AntTarget>(this.targets);
		targets.add(0, createLogTimeTarget("Start"));
		targets.add(createLogTimeTarget("End__"));

		for (AntTarget target : targets) {
			depends.append(target.getName());
			depends.append(", ");
		}
		String allDependencies = depends.toString();
		if (allDependencies.length() > 0) {
			allDependencies = allDependencies.substring(0, allDependencies.length() - 2);
		}
		script.append("<target name=\"execute-all-targets\" " + (allDependencies.trim().length() > 0 ? "depends=\"" + allDependencies + "\"" : "") + " />");
		script.append(NL);

		// TODO make sure that names of the targets do not collide
		for (AntTarget target : targets) {
			StringBuilder deps = new StringBuilder();
			for (String dependency : target.getRequiredTargets()) {
				deps.append(dependency);
				deps.append(", ");
			}
			String allDeps = deps.toString();
			if (allDeps.length() > 0) {
				allDeps = allDeps.substring(0, allDeps.length() - 2);
			}
			String ifConditions = target.getIfConditions();
			String unlessConditions = target.getUnlessConditions();
			script.append("<target name=\"" + target.getName() + "\" " + (allDeps.length() > 0 ? "depends=\"" + allDeps + "\"" : "") + (ifConditions != null ? " if=\"" + ifConditions + "\"" : "") + (unlessConditions != null ? " unless=\"" + unlessConditions + "\"" : "") + ">");
			script.append(target.getContent());
			script.append("</target>");
			script.append(NL);
		}

		script.append("</project>");

		return script.toString();
	}

	private AntTarget createLogTimeTarget(String name) {
		XMLContent content = new XMLContent();
		content.append("<tstamp><format property=\"time-" + name + "\" pattern=\"yyyy-dd-MM HH:mm:ss\" /></tstamp>");
		content.append("<echo file=\"time-log.txt\" append=\"true\">" + name + ": ${time-" + name + "} (" + this.name + ")\n</echo>");
		AntTarget target = new AntTarget("log-time-" + name, content);
		return target;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Use {@link #addTarget(AntTarget)} or {@link #addTargets(Collection)} 
	 * instead.
	 */
	@Deprecated // TODO remove this method once all references are removed
	public void addContent(StringBuffer content) {
		this.content.append(content);
	}

	public void addTargets(Collection<AntTarget> targets) {
		this.targets.addAll(targets);
	}

	public void addTarget(AntTarget target) {
		this.targets.add(target);
	}
}
