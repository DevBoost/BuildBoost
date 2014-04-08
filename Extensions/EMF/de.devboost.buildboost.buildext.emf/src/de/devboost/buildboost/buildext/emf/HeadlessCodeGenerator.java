/*******************************************************************************
 * Copyright (c) 2006-2014
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
package de.devboost.buildboost.buildext.emf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory.Descriptor.Registry;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenModelGeneratorAdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import de.devboost.buildboost.artifacts.InvalidMetadataException;
import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.genext.emf.stages.GenerateEMFCodeStage;
import de.devboost.buildboost.genext.emf.steps.GenerateGenModelCodeStep;

/**
 * The {@link HeadlessCodeGenerator} is executed by build scripts that are
 * generated by the {@link GenerateEMFCodeStage} or the
 * {@link GenerateGenModelCodeStep}. It uses the EMF code generators to obtain
 * Java code from a given generator model. To allow access to referenced
 * generator models a list of all available plug-ins can also be passed to this
 * class.
 */
public class HeadlessCodeGenerator {

	public static void main(String[] args) throws Exception {
		// TODO use properties file to pass arguments instead
		String pathToGenModel = args[0];
		String projectName = args[1];
		String projectPath = args[2];
		List<String> pluginPaths = new ArrayList<String>();
		for (int i = 3; i < args.length; i++) {
			pluginPaths.add(args[i]);
		}
		
		HeadlessCodeGenerator generator = new HeadlessCodeGenerator();
		generator.run(pathToGenModel, projectName, projectPath, pluginPaths);
	}

	private void run(String pathToGenModel, String projectName,
			String projectPath, List<String> pluginPaths) throws Exception {
		
		ResourceSet rs = new ResourceSetImpl();
		
		registerFactoriesAndPackages(rs);
		registerURIMappings(rs, pluginPaths);
		
		EcorePlugin.getPlatformResourceMap().put(
			projectName,
			URI.createFileURI(projectPath + File.separator)
		);
		
		GenModel genModel = loadGenModel(pathToGenModel, rs);
		if (genModel == null) {
			return;
		}
		
		registerCodeGenAdapter();
		generateCode(genModel, projectPath);
	}

	private void generateCode(GenModel genModel, String projectPath) {
		// Create the generator and set the model-level input object.
		Generator generator = new Generator();
		generator.setInput(genModel);
		genModel.setFacadeHelperClass(getClass().getName());
		
		// Generate model code.
		// EMF 2.8: This logs an exception to the console which is not a problem in our case.
		// The logging was introduced in 2.8: https://bugs.eclipse.org/bugs/show_bug.cgi?id=359551
		generateCode(genModel, generator,
				GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE);

		// Generate edit code (if required).
		if (mustGenerateEditCode(genModel, projectPath)) {
			generateCode(genModel, generator,
					GenBaseGeneratorAdapter.EDIT_PROJECT_TYPE);
		}
	}

	private void generateCode(GenModel genModel, Generator generator,
			String projectType) {
		Diagnostic result = doGenerate(genModel, generator, projectType);
		printDiagnostic(result);
	}

	private Diagnostic doGenerate(GenModel genModel, Generator generator,
			String projectType) {

		BasicMonitor.Printing systemOutMonitor = new BasicMonitor.Printing(System.out);
		return generator.generate(genModel, projectType, systemOutMonitor);
	}

	private boolean mustGenerateEditCode(GenModel genModel, String projectPath) {
		File workDir = new File(projectPath).getParentFile();
		String editDirectory = genModel.getEditDirectory();
		if (!editDirectory.endsWith("src-gen")) {
			return false;
		}
		
		if (editDirectory.startsWith("/")) {
			editDirectory = editDirectory.substring(1);
		}
		
		String editProjectName = editDirectory.substring(0, editDirectory.indexOf("/"));
		File editProjectDir = new File(workDir, editProjectName);
		if (!editProjectDir.exists()) {
			return false;
		}
		
		EcorePlugin.getPlatformResourceMap().put(
			editProjectName,
			URI.createFileURI(editProjectDir.getAbsolutePath() + File.separator)
		);
		
		return editProjectDir.exists();
	}

	private void registerCodeGenAdapter() {
		Registry adapterRegistry = GeneratorAdapterFactory.Descriptor.Registry.INSTANCE;
		adapterRegistry.addDescriptor(GenModelPackage.eNS_URI, GenModelGeneratorAdapterFactory.DESCRIPTOR);
	}

	private GenModel loadGenModel(String pathToGenModel, ResourceSet rs) {
		URI uri = URI.createFileURI(pathToGenModel);
		Resource resource = rs.getResource(uri, true);
		// TODO add checks
		List<EObject> contents = resource.getContents();
		GenModel genModel = (GenModel) contents.get(0);
		// reconcile the  GenModel: Since the IDE does also do this in the 
		// background on opening a GenModel, it can happen, that the model 
		// is not up-to-date w.r.t. the underlying Ecore model.
		genModel.reconcile();
		genModel.setCanGenerate(true);
		
		// do not generate manifest, this may override an existing manifest
		genModel.setBundleManifest(false);
		
		EcoreUtil.resolveAll(rs);
		
		boolean foundProxy = false;
		TreeIterator<Notifier> allContents = rs.getAllContents();
		while (allContents.hasNext()) {
			Notifier notifier = (Notifier) allContents.next();
			if (notifier instanceof EObject) {
				EObject eObject = (EObject) notifier;
				if (eObject.eIsProxy()) {
					foundProxy = true;
					System.out.println("ERROR: Found unresolved proxy in generator model " + eObject);
				}
			}
		}
		
		if (foundProxy) {
			return null;
		}
		
		return genModel;
	}

	private void registerURIMappings(ResourceSet rs, List<String> pluginPaths)
			throws Exception {
		
		URIConverter uriConverter = rs.getURIConverter();
		Map<URI, URI> uriMap = uriConverter.getURIMap();
		for (String pluginPath : pluginPaths) {
			registerURIMapping(uriMap, pluginPath);
		}
	}

	private void registerURIMapping(Map<URI, URI> uriMap, String pluginPath)
			throws IOException {
		
		File pluginFile = new File(pluginPath);
		if (pluginFile.isDirectory() && !pluginPath.endsWith("/")) {
			pluginPath = pluginPath + "/";
		}
		
		Plugin plugin;
		try {
			plugin = new Plugin(pluginFile);
		} catch (InvalidMetadataException e) {
			return;
		}
		String identifier = plugin.getIdentifier();
		URI from = URI.createPlatformPluginURI(identifier + "/", true);
		URI to = URI.createFileURI(pluginPath);
		boolean isJAR = "jar".equals(to.fileExtension());
		if (isJAR) {
			to = URI.createURI("archive:" + to.toString() + "!/");
		} else if (!"".equals(to.lastSegment())) {
			to = to.appendSegment("");
		}
		
		System.out.println("Mapping URI " + from + " to " + to);
		uriMap.put(from, to);
	}

	private void registerFactoriesAndPackages(ResourceSet rs) {
		// TODO we must search the target platform for registered resource
		// factories, generator models and EPackages. Currently we do solely
		// register the resource factories and EPackages for the Ecore and the
		// GenModel meta models.
		org.eclipse.emf.ecore.resource.Resource.Factory.Registry resourceFactoryRegistry = rs.getResourceFactoryRegistry();
		Map<String, Object> extensionToFactoryMap = resourceFactoryRegistry.getExtensionToFactoryMap();
		extensionToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
		extensionToFactoryMap.put("genmodel", new EcoreResourceFactoryImpl());
		
		GenModelPackage.eINSTANCE.getGenModel();
		EcorePackage.eINSTANCE.getEPackage();
	}

	private void printDiagnostic(Diagnostic diagnostic) {
		// TODO print only warnings and errors?
		System.out.println("Diagnostic: " + diagnostic.getMessage());
		List<Diagnostic> children = diagnostic.getChildren();
		for (Diagnostic child : children) {
			printDiagnostic(child);
		}
	}
}
