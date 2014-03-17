package com.staticvoidgames.svgexe;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Core SvgExe class that does the work of creating a self-extracting jar.
 * @author Kevin
 */
public class SvgExe {

	/**
	 * The self-extracting jar output file.
	 */
	private JarOutputStream outputJar;

	/**
	 * Directory inside the self-extracting jar to add natives to.
	 */
	private String inJarNativesDirectory;

	/**
	 * Directory inside the self-extracting jar to add external files to.
	 */
	private String inJarExternalDirectory;

	/**
	 * List of files already added to the output jar.
	 * TODO: Prompt instead of skipping?
	 */
	List<String> alreadyAdded = new ArrayList<String>();

	/**
	 * Constructs a new SvgExe instance.
	 * TODO: stop taking all these constructor args and make more setter functions
	 */
	public SvgExe(String jarFile, String mainClass, String version, String nativesDirectory, String externalDirectory, boolean showSplash, boolean useCustomSplash, File customSplashFile, boolean extractToTempDir, String args, String jvmOptions, boolean willContainNatives, boolean willContainExternalFiles) throws Exception{

		this.inJarNativesDirectory = nativesDirectory;
		this.inJarExternalDirectory = externalDirectory;

		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, version);
		manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.staticvoidgames.svgexe.SvgExeLauncher");

		//add splash to manifest
		if(showSplash){
			if(useCustomSplash){
				manifest.getMainAttributes().put(new Attributes.Name("SplashScreen-Image"), customSplashFile.getName());
			}
			else{
				manifest.getMainAttributes().put(new Attributes.Name("SplashScreen-Image"), "splash.png");
			}
		}

		outputJar = new JarOutputStream(new FileOutputStream(jarFile), manifest);
		alreadyAdded = new ArrayList<String>();

		//add splash image to jar
		if(showSplash){
			if(useCustomSplash){
				addInternalFile(customSplashFile);
			}
			else{
				addSvgExeFile("splash.png");
			}
		}

		addSvgExeFile("com/staticvoidgames/svgexe/SvgExeLauncher.class");
		addSvgExeFile("com/staticvoidgames/svgexe/util/StreamGobbler.class");
		
		addPropertiesFile(mainClass, extractToTempDir, args, jvmOptions, willContainNatives, willContainExternalFiles);
	}


	/**
	 * Adds the properties file that is read by the SvgExeLauncher.
	 */
	private void addPropertiesFile(String mainClass, boolean extractToTempDir, String args, String jvmOptions, boolean willContainNatives, boolean willContainExternalFiles) throws IOException{

		StringBuilder sb = new StringBuilder();

		sb.append(SvgExeLauncher.mainClassProperty);
		sb.append("=");
		sb.append(mainClass);
		sb.append("\n");

		if(!"".equals(args)){
			sb.append(SvgExeLauncher.argsProperty);
			sb.append("=");
			sb.append(args);
			sb.append("\n");
		}

		if(!"".equals(jvmOptions)){
			sb.append(SvgExeLauncher.jvmOptionsProperty);
			sb.append("=");
			sb.append(jvmOptions);
			sb.append("\n");
		}

		sb.append(SvgExeLauncher.extractToTempDirProperty);
		sb.append("=");
		sb.append(extractToTempDir);
		sb.append("\n");

		sb.append(SvgExeLauncher.nativesInJarDirectoryProperty);
		sb.append("=");
		sb.append(inJarNativesDirectory);
		sb.append("\n");

		sb.append(SvgExeLauncher.externalFilesInJarDirectoryProperty);
		sb.append("=");
		sb.append(inJarExternalDirectory);
		sb.append("\n");

		sb.append(SvgExeLauncher.containsNativesProperty);
		sb.append("=");
		sb.append(willContainNatives);
		sb.append("\n");

		sb.append(SvgExeLauncher.containsExternalFilesProperty);
		sb.append("=");
		sb.append(willContainExternalFiles);
		sb.append("\n");

		InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

		outputJar.putNextEntry(new ZipEntry("SvgExeProperties.p"));

		int bufferSize;
		byte[] buffer = new byte[4096];

		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
			outputJar.write(buffer, 0, bufferSize);
		}

		in.close();
		outputJar.closeEntry();
	}

	/**
	 * Adds a file from the SvgExe jar (the one run by programmers) to the self-extracting jar (run by end-users).
	 */
	private void addSvgExeFile(String fileName) throws Exception{

		InputStream in = SvgExe.class.getClassLoader().getResourceAsStream(fileName);

		outputJar.putNextEntry(new ZipEntry(fileName));

		int bufferSize;
		byte[] buffer = new byte[4096];

		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
			outputJar.write(buffer, 0, bufferSize);
		}

		in.close();
		outputJar.closeEntry();
	}

	/**
	 * Closes the output file.
	 */
	public void close() throws IOException{
		outputJar.close();
	}

	/**
	 * Adds the native directories and files in the List to the directory inside the jar with the nativesDirectoryName.
	 */
	public void addNatives(String nativesDirectoryName, List<File> files) throws IOException{

		for(File file : files){
			if(file.isFile()){
				addNativeFile(nativesDirectoryName, file);
			}
			else{
				addNativeDirectory(nativesDirectoryName, "", file);
			}
		}
	}

	/**
	 * Adds a single native file to the in-jar directory named nativesDirectoryName.
	 */
	private void addNativeFile(String nativesDirectoryName, File file) throws IOException{

		InputStream in = new FileInputStream(file);

		outputJar.putNextEntry(new ZipEntry(nativesDirectoryName + "/" + file.getName()));

		int bufferSize;
		byte[] buffer = new byte[4096];

		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
			outputJar.write(buffer, 0, bufferSize);
		}

		in.close();
		outputJar.closeEntry();
	}

	/**
	 * Recursively adds the directory to the in-jar native directory.
	 */
	private void addNativeDirectory(String nativesDirectoryName, String parent, File directory) throws IOException{

		for(File f : directory.listFiles()){

			if(f.isFile()){
				InputStream in = new FileInputStream(f);


				outputJar.putNextEntry(new ZipEntry(nativesDirectoryName + parent + "/" + directory.getName() + "/" + f.getName()));

				int bufferSize;
				byte[] buffer = new byte[4096];

				while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
					outputJar.write(buffer, 0, bufferSize);
				}

				in.close();
				outputJar.closeEntry();
			}
			else{
				addNativeDirectory(nativesDirectoryName, parent + "/" + directory.getName(), f);
			}
		}
	}

	/**
	 * Extracts the files from inside the jars into the self-extracting jar.
	 * TODO: detect duplicates and prompt?
	 */
	public void addFilesFromJars(List<File> jars) throws Exception{


		for (int i = 0; i < jars.size(); i++){
			ZipFile jarFile = new ZipFile(jars.get(i));

			Enumeration<? extends ZipEntry> entities = jarFile.entries();

			while (entities.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entities.nextElement();

				if (!alreadyAdded.contains(entry.getName()) && !entry.getName().toLowerCase().startsWith("meta-inf")){
					//  if (!entry.getName().toLowerCase().contains("SvgExe")){
					InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));

					outputJar.putNextEntry(new ZipEntry(entry.getName()));
					alreadyAdded.add(entry.getName());

					int bufferSize;
					byte[] buffer = new byte[4096];

					while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
						outputJar.write(buffer, 0, bufferSize);
					}

					in.close();
					outputJar.closeEntry();
				}
			}


			jarFile.close();
		}
	}

	/**
	 * Adds the list of classes and packages.
	 */
	public void addClasses(List<File> classes) throws Exception{
		for(File file : classes){
			if(file.isFile()){
				addClass(file);
			}
			else{
				addPackage("", file);
			}
		}
	}

	/**
	 * Recursively adds the package to the self-extracting jar.
	 */
	private void addPackage(String parent, File directory) throws IOException{

		for(File f : directory.listFiles()){

			if(f.isFile()){
				InputStream in = new FileInputStream(f);

				outputJar.putNextEntry(new ZipEntry(parent + "/" + directory.getName() + "/" + f.getName()));

				int bufferSize;
				byte[] buffer = new byte[4096];

				while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
					outputJar.write(buffer, 0, bufferSize);
				}

				in.close();
				outputJar.closeEntry();
			}
			else{
				addPackage(parent + "/" + directory.getName(), f);
			}
		}
	}

	/**
	 * Adds the class to the self-extracting jar.
	 */
	private void addClass(File file) throws Exception{

		InputStream in = new FileInputStream(file);

		outputJar.putNextEntry(new ZipEntry(file.getName()));

		int bufferSize;
		byte[] buffer = new byte[4096];

		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
			outputJar.write(buffer, 0, bufferSize);
		}

		in.close();
		outputJar.closeEntry();
	}

	/**
	 * Adds the files and directories to the self-extracting jar. These files will be extracted when the jar is run.
	 */
	public void addExternalFiles(String externalDirectoryName, List<File> files) throws IOException{
		for(File file : files){
			if(file.isFile()){
				addExternalFile(externalDirectoryName, file);
			}
			else{
				addExternalDirectory(externalDirectoryName, "", file);
			}
		}
	}

	/**
	 * Recursively adds the directory to the self-extracting jar.
	 */
	private void addExternalDirectory(String externalDirectoryName, String parent, File directory) throws IOException{

		for(File f : directory.listFiles()){

			if(f.isFile()){
				InputStream in = new FileInputStream(f);

				outputJar.putNextEntry(new ZipEntry(externalDirectoryName + "/" + parent + "/" + directory.getName() + "/" + f.getName()));

				int bufferSize;
				byte[] buffer = new byte[4096];

				while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
					outputJar.write(buffer, 0, bufferSize);
				}

				in.close();
				outputJar.closeEntry();
			}
			else{
				addExternalDirectory(externalDirectoryName, parent + "/" + directory.getName(), f);
			}
		}

	}

	/**
	 * Adds the File to the self-extracting jar. This file will NOT be extracted.
	 */
	private void addInternalFile(File file) throws IOException{
		InputStream in = new FileInputStream(file);

		outputJar.putNextEntry(new ZipEntry(file.getName()));

		int bufferSize;
		byte[] buffer = new byte[4096];

		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
			outputJar.write(buffer, 0, bufferSize);
		}

		in.close();
		outputJar.closeEntry();
	}

	/**
	 * Adds a file to the self-extracting jar. This file will be extracted.
	 */
	private void addExternalFile(String externalDirectoryName, File file) throws IOException{

		InputStream in = new FileInputStream(file);

		outputJar.putNextEntry(new ZipEntry(externalDirectoryName + "/" + file.getName()));

		int bufferSize;
		byte[] buffer = new byte[4096];

		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
			outputJar.write(buffer, 0, bufferSize);
		}

		in.close();
		outputJar.closeEntry();
	}
}
