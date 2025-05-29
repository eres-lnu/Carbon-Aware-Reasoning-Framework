/*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.lnu.cloudSimCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.shared.invoker.*;

import org.eclipse.acceleo.engine.event.IAcceleoTextGenerationListener;
import org.eclipse.acceleo.engine.generation.strategy.IAcceleoGenerationStrategy;
import org.eclipse.acceleo.engine.service.AbstractAcceleoGenerator;
import org.eclipse.acceleo.model.mtl.MtlPackage;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.acceleo.model.mtl.Module;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Entry point of the 'Main' generation module.
 *
 * @generated
 */
public class Main extends AbstractAcceleoGenerator {
    /**
     * The name of the module.
     *
     * @generated
     */
    public static final String MODULE_FILE_NAME = "/org/lnu/cloudSimCreator/main";
    
    /**
     * The name of the templates that are to be generated.
     *
     * @generated
     */
    public static final String[] TEMPLATE_NAMES = { "projectGenerator" };
    
    /**
     * @generated NOT
     */
    public static String PROJECT_ROOT;
    
    /**
     * The list of properties files from the launch parameters (Launch configuration).
     *
     * @generated
     */
    private List<String> propertiesFiles = new ArrayList<String>();

    /**
     * Allows the public constructor to be used. Note that a generator created
     * this way cannot be used to launch generations before one of
     * {@link #initialize(EObject, File, List)} or
     * {@link #initialize(URI, File, List)} is called.
     * <p>
     * The main reason for this constructor is to allow clients of this
     * generation to call it from another Java file, as it allows for the
     * retrieval of {@link #getProperties()} and
     * {@link #getGenerationListeners()}.
     * </p>
     *
     * @generated
     */
    public Main() {
        // Empty implementation
    }

    /**
     * This allows clients to instantiates a generator with all required information.
     * 
     * @param modelURI
     *            URI where the model on which this generator will be used is located.
     * @param targetFolder
     *            This will be used as the output folder for this generation : it will be the base path
     *            against which all file block URLs will be resolved.
     * @param arguments
     *            If the template which will be called requires more than one argument taken from the model,
     *            pass them here.
     * @throws IOException
     *             This can be thrown in three scenarios : the module cannot be found, it cannot be loaded, or
     *             the model cannot be loaded.
     * @generated
     */
    public Main(URI modelURI, File targetFolder,
            List<? extends Object> arguments) throws IOException {
        initialize(modelURI, targetFolder, arguments);
    }

    /**
     * This allows clients to instantiates a generator with all required information.
     * 
     * @param model
     *            We'll iterate over the content of this element to find Objects matching the first parameter
     *            of the template we need to call.
     * @param targetFolder
     *            This will be used as the output folder for this generation : it will be the base path
     *            against which all file block URLs will be resolved.
     * @param arguments
     *            If the template which will be called requires more than one argument taken from the model,
     *            pass them here.
     * @throws IOException
     *             This can be thrown in two scenarios : the module cannot be found, or it cannot be loaded.
     * @generated NOT
     */
    public Main(EObject model, File targetFolder,
            List<? extends Object> arguments) throws IOException {
        initialize(model, targetFolder, arguments);
    }
    
    /**
     * This can be used to launch the generation from a standalone application.
     * 
     * @param args
     *            Arguments of the generation. 
     * @generated NOT
     */
    public static void main(String[] args){
        try {
        	/*
        	 * Check that the parameter is a valid UML model and create its URI
        	 */
        	if (args.length < 1) throw new IllegalArgumentException("Please specify the path of the input model in the arguments");
        	File model = new File(args[0]).getAbsoluteFile();
        	URI modelURI = URI.createFileURI(model.getAbsolutePath());
        	if (!model.exists() || !"uml".equals(modelURI.fileExtension())) throw new IllegalArgumentException("The input model should be an existing UML file");
        	/*
        	 * Use the UML file name to determine the package name of the CloudSimPlus source and the name of its main class
        	 */
        	String fileName = modelURI.trimFileExtension().lastSegment();
        	StringBuilder sb = new StringBuilder();
        	String[] fileNameParts = fileName.split("[^a-zA-Z0-9]+");
        	for (String word : fileNameParts) {
        		if (word.isEmpty()) continue;
        		sb.append(Character.toUpperCase(word.charAt(0)));
        		if (word.length() > 1) {
        			sb.append(word.substring(1).toLowerCase());
        		}
        	}
        	String projectName = sb.toString();
        	String packageName = projectName.toLowerCase();
        	/*
        	 * Prepare the Maven project folder structure
        	 */
        	Path projectDirectory = Files.createTempDirectory(projectName);
        	PROJECT_ROOT = projectDirectory.toAbsolutePath().toString();
        	System.out.println(PROJECT_ROOT);
        	//System.exit(0);
        	// String projectRoot = ".." + File.separator + projectName;
        	List<String> mavenDirectories = new ArrayList<>(Arrays.asList(
        		"src" + File.separator + "main" + File.separator + "java",
        		"src" + File.separator + "main" + File.separator + "resources",
        		"src" + File.separator + "test" + File.separator + "java",
        		"src" + File.separator + "test" + File.separator + "resources"
        	));
        	mavenDirectories.add(mavenDirectories.get(0) + File.separator + packageName);
        	mavenDirectories.replaceAll(path -> PROJECT_ROOT + File.separator + path);
        	for (String dir : mavenDirectories) {
                File folder = new File(dir);
                if (!folder.mkdirs() && !folder.exists()) throw new IOException("Failed to create: " + folder.getPath());
            }
        	/*
        	 * Write the needed resources (including pom.xml) in the Maven project and setup Maven
        	 */
        	String MAVEN_ARCHIVE = "apache-maven-3.9.9-bin.zip";
        	writeResource("pom.xml", PROJECT_ROOT);
        	writeResource("gcis.dtd", mavenDirectories.get(1));
        	writeResource("gcis.xml", mavenDirectories.get(1));
        	Path mavenHome = unzip(MAVEN_ARCHIVE, PROJECT_ROOT);
        	File mavenFile = new File(mavenHome.toAbsolutePath().toString() + File.separator + "bin" + File.separator + "mvn");
        	if (!mavenFile.canExecute()) if (!mavenFile.setExecutable(true)) throw new RuntimeException("Cannot execute the bundled Maven");
        	/*
        	 * Launch the Acceleo transformation, passing the package name as argument and registering the profile
        	 */
        	List<String> acceleoArguments = new ArrayList<String>();
        	acceleoArguments.add(packageName);
        	ResourceSet rs = new ResourceSetImpl();
        	UMLResourcesUtil.init(rs);
            /*URL profileURL = Thread.currentThread().getContextClassLoader().getResource("sci-uml.profile.uml");
        	URI profileURI = URI.createFileURI(Paths.get(profileURL.getPath()).toFile().getAbsolutePath());*/
        	URI profileSampleURI = URI.createURI("internal:/sci-uml.profile.uml");
        	Resource profileResource = rs.createResource(profileSampleURI);
        	InputStream profileIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("sci-uml.profile.uml");
        	if (profileIn == null) throw new IOException("Something went wrong when loading the profile");
        	profileResource.load(profileIn, null);
        	Resource umlModelResource = rs.getResource(modelURI, true);
        	//Resource profileResource = rs.getResource(profileURI, true);
        	Model umlModel = (Model) EcoreUtil.getObjectByType(umlModelResource.getContents(), UMLPackage.Literals.MODEL);
        	Profile profileRoot = (Profile) EcoreUtil.getObjectByType(profileResource.getContents(), UMLPackage.Literals.PACKAGE);
        	profileRoot.define();
        	profileRoot.setURI("http://lnu.se/sciuml");
        	String nsURI = profileRoot.getDefinition().getNsURI();
        	EPackage.Registry.INSTANCE.put(nsURI, profileRoot.getDefinition());
        	for (ProfileApplication pa : new ArrayList<>(umlModel.getProfileApplications())) {
        	    umlModel.getProfileApplications().remove(pa);
        	}
        	rs.getURIConverter().getURIMap().put(profileSampleURI, URI.createURI(nsURI));
        	umlModel.applyProfile(profileRoot);
        	EcoreUtil.resolveAll(rs);
        	//Map<String, Object> saveOptions = new HashMap<>();
        	//umlModelResource.save(saveOptions);
        	System.out.println("The model has " + umlModel.allOwnedElements().stream().mapToInt(e -> e.getAppliedStereotypes().size()).sum() + " stereotype(s) applied.");
        	
        	// Register and load the .emtl for correct generation from JAR
        	MtlPackage.eINSTANCE.eClass();
        	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("emtl", new XMIResourceFactoryImpl());
        	URI emtlSampleURI = URI.createURI("internal:/main.emtl");
        	Resource emtlResource = rs.createResource(emtlSampleURI);
        	InputStream emtlIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("main.emtl");
        	if (emtlIn == null) throw new IOException("The transformation template cannot be loaded");
        	emtlResource.load(emtlIn, null);
        	
        	Main acceleoGenerator = new Main((EObject) umlModel, new File(mavenDirectories.get(4)), acceleoArguments);
        	acceleoGenerator.doGenerate(new BasicMonitor());
        	/*
        	 * Execute the simulation through Maven
        	 */
        	InvocationRequest request = new DefaultInvocationRequest();
        	request.setMavenHome(mavenHome.toFile());
        	request.setPomFile(new File(PROJECT_ROOT + File.separator + "pom.xml"));
        	request.setGoals(Arrays.asList("clean", "install", "exec:java"));
        	request.setMavenOpts("-Dmaven.repo.local=" + mavenHome.toAbsolutePath().toString() + File.separator + ".m2"
        			+ " -Dexec.mainClass=" + packageName + "." + packageName.replaceFirst(String.valueOf(packageName.charAt(0)), String.valueOf(packageName.charAt(0)).toUpperCase()));
        	request.setBatchMode(true);
        	request.setOutputHandler(System.out::println);
        	request.setErrorHandler(System.err::println);
        	Invoker invoker = new DefaultInvoker();
        	invoker.execute(request);
        	/*
        	 * Clean up
        	 */
        	Files.walk(projectDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @generated NOT
     * @param resourceName
     * @param newPath
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected static void writeResource(String resourceName, String newPath) throws FileNotFoundException, IOException {
    	InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    	if (in == null) throw new FileNotFoundException("Something went wrong when retrieving the resource " + resourceName);
    	Path resourcesPath = Paths.get(newPath + File.separator + resourceName);
    	Files.copy(in, resourcesPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * @generated NOT
     * @param zipFilePath
     * @param destDir
     * @throws IOException
     */
    public static Path unzip(String resourceName, String destDir) throws IOException {
        try (InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            Path mvnHomePath = null;
            Path destDirPath = Paths.get(destDir);
            while ((entry = zis.getNextEntry()) != null) {
                Path newFilePath = destDirPath.resolve(entry.getName()).normalize();
                if (mvnHomePath == null) mvnHomePath = newFilePath;

                if (!newFilePath.startsWith(destDir)) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    Files.createDirectories(newFilePath.getParent());
                    Files.copy(zis, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
            return mvnHomePath;
        }
    }

    /**
     * Launches the generation described by this instance.
     * 
     * @param monitor
     *            This will be used to display progress information to the user.
     * @throws IOException
     *             This will be thrown if any of the output files cannot be saved to disk.
     * @generated
     */
    @Override
    public void doGenerate(Monitor monitor) throws IOException {
        /*
         * TODO if you wish to change the generation as a whole, override this. The default behavior should
         * be sufficient in most cases. If you want to change the content of this method, do NOT forget to
         * change the "@generated" tag in the Javadoc of this method to "@generated NOT". Without this new tag,
         * any compilation of the Acceleo module with the main template that has caused the creation of this
         * class will revert your modifications. If you encounter a problem with an unresolved proxy during the
         * generation, you can remove the comments in the following instructions to check for problems. Please
         * note that those instructions may have a significant impact on the performances.
         */

        //org.eclipse.emf.ecore.util.EcoreUtil.resolveAll(model);

        /*
         * If you want to check for potential errors in your models before the launch of the generation, you
         * use the code below.
         */

        //if (model != null && model.eResource() != null) {
        //    List<org.eclipse.emf.ecore.resource.Resource.Diagnostic> errors = model.eResource().getErrors();
        //    for (org.eclipse.emf.ecore.resource.Resource.Diagnostic diagnostic : errors) {
        //        System.err.println(diagnostic.toString());
        //    }
        //}

        super.doGenerate(monitor);
    }
    
    /**
     * If this generator needs to listen to text generation events, listeners can be returned from here.
     * 
     * @return List of listeners that are to be notified when text is generated through this launch.
     * @generated
     */
    @Override
    public List<IAcceleoTextGenerationListener> getGenerationListeners() {
        List<IAcceleoTextGenerationListener> listeners = super.getGenerationListeners();
        /*
         * TODO if you need to listen to generation event, add listeners to the list here. If you want to change
         * the content of this method, do NOT forget to change the "@generated" tag in the Javadoc of this method
         * to "@generated NOT". Without this new tag, any compilation of the Acceleo module with the main template
         * that has caused the creation of this class will revert your modifications.
         */
        return listeners;
    }
    
    /**
     * If you need to change the way files are generated, this is your entry point.
     * <p>
     * The default is {@link org.eclipse.acceleo.engine.generation.strategy.DefaultStrategy}; it generates
     * files on the fly. If you only need to preview the results, return a new
     * {@link org.eclipse.acceleo.engine.generation.strategy.PreviewStrategy}. Both of these aren't aware of
     * the running Eclipse and can be used standalone.
     * </p>
     * <p>
     * If you need the file generation to be aware of the workspace (A typical example is when you wanna
     * override files that are under clear case or any other VCS that could forbid the overriding), then
     * return a new {@link org.eclipse.acceleo.engine.generation.strategy.WorkspaceAwareStrategy}.
     * <b>Note</b>, however, that this <b>cannot</b> be used standalone.
     * </p>
     * <p>
     * All three of these default strategies support merging through JMerge.
     * </p>
     * 
     * @return The generation strategy that is to be used for generations launched through this launcher.
     * @generated
     */
    @Override
    public IAcceleoGenerationStrategy getGenerationStrategy() {
        return super.getGenerationStrategy();
    }
    
    /**
     * This will be called in order to find and load the module that will be launched through this launcher.
     * We expect this name not to contain file extension, and the module to be located beside the launcher.
     * 
     * @return The name of the module that is to be launched.
     * @generated NOT
     */
    @Override
    public String getModuleName() {
        return !isRunningFromJar() ? MODULE_FILE_NAME : "/src" + MODULE_FILE_NAME;
    }
    
    /**
     * @generated NOT
     * @return
     */
    protected static boolean isRunningFromJar() {
        String className = Main.class.getSimpleName() + ".class";
        String classPath = Main.class.getResource(className).toString();
        return classPath.startsWith("rsrc:");
    }

    /**
     * This will use the standard initializer when launched from inside Eclipse and fall back to manual loading when launched from JAR
     * @generated NOT
     */
    @Override
    public void initialize(EObject model, File targetFolder, List<? extends Object> arguments) throws IOException{
    	try {
    		super.initialize(model, targetFolder, arguments);
    	} catch (Exception e) {
    		writeResource("main.emtl", PROJECT_ROOT);
    		URI moduleURI = URI.createFileURI(PROJECT_ROOT + File.separator + "main.emtl");
        	ResourceSet rs = new ResourceSetImpl();
        	Resource emtlResource = rs.getResource(moduleURI, true);
        	Module module = (Module) emtlResource.getContents().get(0);
        	
        	this.module = module;
        	this.targetFolder = targetFolder;
        	this.model = model;
        	this.generationArguments = arguments;
    	}
    }
    
    /**
     * If the module(s) called by this launcher require properties files, return their qualified path from
     * here.Take note that the first added properties files will take precedence over subsequent ones if they
     * contain conflicting keys.
     * 
     * @return The list of properties file we need to add to the generation context.
     * @see java.util.ResourceBundle#getBundle(String)
     * @generated
     */
    @Override
    public List<String> getProperties() {
        /*
         * If you want to change the content of this method, do NOT forget to change the "@generated"
         * tag in the Javadoc of this method to "@generated NOT". Without this new tag, any compilation
         * of the Acceleo module with the main template that has caused the creation of this class will
         * revert your modifications.
         */

        /*
         * TODO if your generation module requires access to properties files, add their qualified path to the list here.
         * 
         * Properties files can be located in an Eclipse plug-in or in the file system (all Acceleo projects are Eclipse
         * plug-in). In order to use properties files located in an Eclipse plugin, you need to add the path of the properties
         * files to the "propertiesFiles" list:
         * 
         * final String prefix = "platform:/plugin/";
         * final String pluginName = "org.eclipse.acceleo.module.sample";
         * final String packagePath = "/org/eclipse/acceleo/module/sample/properties/";
         * final String fileName = "default.properties";
         * propertiesFiles.add(prefix + pluginName + packagePath + fileName);
         * 
         * With this mechanism, you can load properties files from your plugin or from another plugin.
         * 
         * You may want to load properties files from the file system, for that you need to add the absolute path of the file:
         * 
         * propertiesFiles.add("C:\Users\MyName\MyFile.properties");
         * 
         * If you want to let your users add properties files located in the same folder as the model:
         *
         * if (EMFPlugin.IS_ECLIPSE_RUNNING && model != null && model.eResource() != null) { 
         *     propertiesFiles.addAll(AcceleoEngineUtils.getPropertiesFilesNearModel(model.eResource()));
         * }
         * 
         * To learn more about Properties Files, have a look at the Acceleo documentation (Help -> Help Contents).
         */
        return propertiesFiles;
    }
    
    /**
     * Adds a properties file in the list of properties files.
     * 
     * @param propertiesFile
     *            The properties file to add.
     * @generated
     * @since 3.1
     */
    @Override
    public void addPropertiesFile(String propertiesFile) {
        this.propertiesFiles.add(propertiesFile);
    }
    
    /**
     * This will be used to get the list of templates that are to be launched by this launcher.
     * 
     * @return The list of templates to call on the module {@link #getModuleName()}.
     * @generated
     */
    @Override
    public String[] getTemplateNames() {
        return TEMPLATE_NAMES;
    }
    
    /**
     * This can be used to update the resource set's package registry with all needed EPackages.
     * 
     * @param resourceSet
     *            The resource set which registry has to be updated.
     * @generated NOT
     */
	@Override
    public void registerPackages(ResourceSet resourceSet) {
        super.registerPackages(resourceSet);
        if (!isInWorkspace(org.eclipse.uml2.uml.UMLPackage.class)) {
            resourceSet.getPackageRegistry().put(org.eclipse.uml2.uml.UMLPackage.eINSTANCE.getNsURI(), org.eclipse.uml2.uml.UMLPackage.eINSTANCE);
        }
        
        /*URL profileURL = Thread.currentThread().getContextClassLoader().getResource("sci-uml.profile.uml");
    	if (profileURL == null) throw new IllegalArgumentException("Something went wrong while registering the SCIUML profile");
    	URI profileURI = URI.createFileURI(Paths.get(profileURL.getPath()).toFile().getAbsolutePath());
    	UMLResourcesUtil.init(resourceSet);
    	Resource profileResource = resourceSet.getResource(profileURI, true);
    	Object profileRoot = profileResource.getContents().get(0);
    	if (!(profileRoot instanceof Profile)) throw new IllegalArgumentException("The SCIUML profile is corrupted and cannot be registered");
    	Profile profilePackage = (Profile) profileRoot;
    	String nsURI = profilePackage.getURI();
    	EPackage.Registry.INSTANCE.put(nsURI, profilePackage);*/
        /*
         * If you want to change the content of this method, do NOT forget to change the "@generated"
         * tag in the Javadoc of this method to "@generated NOT". Without this new tag, any compilation
         * of the Acceleo module with the main template that has caused the creation of this class will
         * revert your modifications.
         */
        
        /*
         * If you need additional package registrations, you can register them here. The following line
         * (in comment) is an example of the package registration for UML.
         * 
         * You can use the method  "isInWorkspace(Class c)" to check if the package that you are about to
         * register is in the workspace.
         * 
         * To register a package properly, please follow the following conventions:
         *
         * If the package is located in another plug-in, already installed in Eclipse. The following content should
         * have been generated at the beginning of this method. Do not register the package using this mechanism if
         * the metamodel is located in the workspace.
         *  
         * if (!isInWorkspace(UMLPackage.class)) {
         *     // The normal package registration if your metamodel is in a plugin.
         *     resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
         * }
         * 
         * If the package is located in another project in your workspace, the plugin containing the package has not
         * been register by EMF and Acceleo should register it automatically. If you want to use the generator in
         * stand alone, the regular registration (seen a couple lines before) is needed.
         * 
         * To learn more about Package Registration, have a look at the Acceleo documentation (Help -> Help Contents).
         */
    }

    /**
     * This can be used to update the resource set's resource factory registry with all needed factories.
     * 
     * @param resourceSet
     *            The resource set which registry has to be updated.
     * @generated
     */
    @Override
    public void registerResourceFactories(ResourceSet resourceSet) {
        super.registerResourceFactories(resourceSet);
        /*
         * If you want to change the content of this method, do NOT forget to change the "@generated"
         * tag in the Javadoc of this method to "@generated NOT". Without this new tag, any compilation
         * of the Acceleo module with the main template that has caused the creation of this class will
         * revert your modifications.
         */
        
        /*
         * TODO If you need additional resource factories registrations, you can register them here. the following line
         * (in comment) is an example of the resource factory registration.
         *
         * If you want to use the generator in stand alone, the resource factory registration will be required.
         *  
         * To learn more about the registration of Resource Factories, have a look at the Acceleo documentation (Help -> Help Contents). 
         */ 
        
        // resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(XyzResource.FILE_EXTENSION, XyzResource.Factory.INSTANCE);
        
        /*
         * Some metamodels require a very complex setup for standalone usage. For example, if you want to use a generator
         * targetting UML models in standalone, you NEED to use the following:
         */ 
        // UMLResourcesUtil.init(resourceSet)
    }
    
}
