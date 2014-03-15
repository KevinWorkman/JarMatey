package com.staticvoidgames.svgexe;

import java.awt.SplashScreen;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.staticvoidgames.svgexe.util.StreamGobbler;

public class SvgExeLauncher {

	public static String mainClassProperty = "mainClass";
	public static String argsProperty = "args";
	public static String jvmOptionsProperty = "jvmOptions";
	public static String nativesInJarDirectoryProperty = "nativesDirectory";
	public static String externalFilesInJarDirectoryProperty = "externalFilesDirectory";
	public static String extractToTempDirProperty = "extractToTempDir";
	public static String containsNativesProperty = "containsNatives";
	public static String containsExternalFilesProperty = "containsExternalFiles";

	public static void main(String... unusedArgs) throws InterruptedException, IOException{

		File jarFile = new File(SvgExeLauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath());

		String fullJarPath = jarFile.getAbsolutePath();

		Properties properties = new Properties();
		properties.load(SvgExeLauncher.class.getClassLoader().getResourceAsStream("SvgExeProperties.p"));

		String nativesInJarDirectory =  properties.getProperty(SvgExeLauncher.nativesInJarDirectoryProperty);
		String mainClass = properties.getProperty(SvgExeLauncher.mainClassProperty);
		String externalFilesInJarDirectory = properties.getProperty(SvgExeLauncher.externalFilesInJarDirectoryProperty);

	

		


		boolean containsNatives = Boolean.valueOf(properties.getProperty(SvgExeLauncher.containsNativesProperty));
		boolean containsExternalFiles = Boolean.valueOf(properties.getProperty(SvgExeLauncher.containsExternalFilesProperty));

		boolean extractToTempDir = Boolean.valueOf(properties.getProperty(SvgExeLauncher.extractToTempDirProperty));



		Set<String> libraryPaths = null;
		String nativesDirectory = null;
		List<String> addToClassPath = new ArrayList<String>();
		if(containsNatives){
			nativesDirectory = createAndReturnDir(extractToTempDir, nativesInJarDirectory);
			libraryPaths = extractNativesAndReturnLibraryPaths(fullJarPath, nativesInJarDirectory, nativesDirectory, addToClassPath);
		}

		String externalFilesOnComputerDirectory = null;
		if(containsExternalFiles){
			externalFilesOnComputerDirectory = createAndReturnDir(extractToTempDir, externalFilesInJarDirectory);
			extractExternalFiles(fullJarPath, externalFilesInJarDirectory, externalFilesOnComputerDirectory);
		}

		List<String> processArgs = new ArrayList<String>();
		processArgs.add("java");
		if(properties.containsKey(SvgExeLauncher.jvmOptionsProperty)){
			String jvmOptionsArray[] = properties.getProperty(SvgExeLauncher.jvmOptionsProperty).split("\\s");
			processArgs.addAll(Arrays.asList(jvmOptionsArray));
		}
		

		StringBuilder nativePathBuilder = new StringBuilder();
		StringBuilder classpathBuilder = new StringBuilder(fullJarPath + System.getProperty("path.separator"));
		if(containsNatives){

			for(String path : libraryPaths){
				nativePathBuilder.append(path);
				nativePathBuilder.append(System.getProperty("path.separator"));
			}

			for(String s : addToClassPath){
				classpathBuilder.append(s);
				classpathBuilder.append(System.getProperty("path.separator"));
			}

			//processArgs.add("-Djava.library.path=" + nativesDirectory + ";" + nativesDirectory + "/"+System.getProperty("os.arch"));
			processArgs.add("-Djava.library.path=" + nativePathBuilder.toString());
		}
		processArgs.add("-cp");
		processArgs.add(classpathBuilder.toString());
		processArgs.add(mainClass);
		
		if(properties.containsKey(SvgExeLauncher.jvmOptionsProperty)){
			String argsArray[] = properties.getProperty(SvgExeLauncher.argsProperty).split("\\s");
			processArgs.addAll(Arrays.asList(argsArray));
		}

		

		ProcessBuilder pb = new ProcessBuilder(processArgs.toArray(new String[]{}));

		//System.out.println(Arrays.toString(processArgs.toArray()));

		if(containsExternalFiles){
			pb.directory(new File(externalFilesOnComputerDirectory));
		}

		try{
			if(SplashScreen.getSplashScreen() != null){
				SplashScreen.getSplashScreen().close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

		Process p = pb.start();

		StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), System.out);
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), System.err);

		outGobbler.start();
		errorGobbler.start();

		p.waitFor();






		if(containsNatives){
			deleteDir(new File(nativesDirectory));
		}


		if(containsExternalFiles){
			deleteDir(new File(externalFilesOnComputerDirectory));
		}





	}


	public static String createAndReturnDir(boolean extractToTempDir, String svgExeDir){

		String parentDir = ".";

		if(extractToTempDir){

			String tempDir = System.getProperty("deployment.user.cachedir");

			if ((tempDir == null) || (System.getProperty("os.name").startsWith("Win"))) {
				tempDir = System.getProperty("java.io.tmpdir");
			}

			parentDir = tempDir;
		}

		int i = 0;
		String fullNativeDir = parentDir + File.separator + svgExeDir + i;
		while(new File(fullNativeDir).exists()){
			i++;
			fullNativeDir = parentDir + File.separator + svgExeDir + i;
		}

		File dir = new File(fullNativeDir);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		return fullNativeDir;
	}

	public static void deleteDir(File nativeDir){

		if(nativeDir.isDirectory()){
			for(File f : nativeDir.listFiles()){
				deleteDir(f);
			}
		}

		nativeDir.delete();
	}

	//x86_64

	/**
	 * Extract native files FROM the nativesInJarDirectory TO the workingDirectory.
	 */
	public static Set<String> extractNativesAndReturnLibraryPaths(String jar, String nativesInJarDirectory, String workingDirectory, List<String> addToClassPath) throws IOException{


		String osName = System.getProperty("os.name").replaceAll("\\s", "").toLowerCase();
		String osArch = System.getProperty("os.arch");

		Set<String> libraryPaths = new HashSet<String>();

		JarFile jarFile = new JarFile(jar, false);

		Enumeration<JarEntry> entities = jarFile.entries();

		while (entities.hasMoreElements()) {
			JarEntry entry = (JarEntry)entities.nextElement();

			boolean extractMe = false;

			if ((!entry.isDirectory())){ //&& (entry.getName().indexOf('/') == -1)
				if (isNativeFile(entry.getName(), nativesInJarDirectory)){



					String fileToExtractTo = entry.getName().replaceFirst(nativesInJarDirectory + "/", "");


					String[] pathSections = fileToExtractTo.split("/");

					System.out.print("Path sections: ");
					for(String s : pathSections){
						System.out.print(s + " / ");
					}

					//os.name/os.arch/file
					if(pathSections.length >= 2){


						//path contains an os name.. is it for the current system?

						String firstDir = pathSections[0];

						String formattedFirstDir = firstDir.replaceAll("\\s", "").toLowerCase();


						if(osName.startsWith(formattedFirstDir)){
							//os name matches current system

							if(pathSections.length >= 3){
								//path contains an os arch.. is it for the current system?

								String secondDir = pathSections[1];

								if(osArch.equals(secondDir)){
									extractMe = true;
								}
							}
							else{
								//path doesn't contain an os.arch, add it
								extractMe = true;
							}

						}
					}
					else{
						extractMe = true;
					}




					if(extractMe){
						File file = new File(workingDirectory + File.separator + fileToExtractTo);
						File parent = new File(file.getParent());
						parent.mkdirs();

						libraryPaths.add(parent.getAbsolutePath());
						if(file.getName().toLowerCase().endsWith(".jar")){
							addToClassPath.add(file.getAbsolutePath());
						}

						InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
						OutputStream out = new FileOutputStream(file);

						byte[] buffer = new byte[65536];
						int bufferSize;
						while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1){
							out.write(buffer, 0, bufferSize);
						}

						in.close();
						out.close();
					}
				}
			}
		}

		jarFile.close();

		return libraryPaths;
	}

	public static boolean isNativeFile(String entryName, String nativesInJarDirectory) {

		if(entryName.contains(nativesInJarDirectory)){
			return true;
		}
		else{
			return false;
		}

		//		String osName = System.getProperty("os.name");
		//		String name = entryName.toLowerCase();
		//
		//		if (osName.startsWith("Win")) {
		//			if (name.endsWith(".dll"))
		//				return true;
		//		}
		//		else if (osName.startsWith("Linux")) {
		//			if (name.endsWith(".so"))
		//				return true;
		//		}
		//		else if (((osName.startsWith("Mac")) || (osName.startsWith("Darwin"))) && (
		//				(name.endsWith(".jnilib")) || (name.endsWith(".dylib")))) {
		//			return true;
		//		}
		//
		//		return false;
	}

	public static void extractExternalFiles(String jar, String externalsInJarDirectory, String workingDirectory) throws IOException{

		JarFile jarFile = new JarFile(jar, false);

		Enumeration<JarEntry> entities = jarFile.entries();

		while (entities.hasMoreElements()) {
			JarEntry entry = (JarEntry)entities.nextElement();

			if ((!entry.isDirectory())){ 
				if (isExternalFile(entry.getName(), externalsInJarDirectory)){

					String fileToExtractTo = entry.getName().replaceFirst(externalsInJarDirectory, "");

					File file = new File(workingDirectory + File.separator + fileToExtractTo);
					File parent = new File(file.getParent());
					if(!parent.exists()){
						parent.mkdirs();
					}

					InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
					OutputStream out = new FileOutputStream(file);

					byte[] buffer = new byte[65536];
					int bufferSize;
					while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1){
						out.write(buffer, 0, bufferSize);
					}

					in.close();
					out.close();
				}
			}
		}

		jarFile.close();
	}

	public static boolean isExternalFile(String entryName, String externalsInJarDirectory) {
		if(entryName.contains(externalsInJarDirectory)){
			return true;
		}
		else{
			return false;
		}
	}

	public static void extractFile(String jar, String file) throws IOException{

		JarFile jarFile = new JarFile(jar, false);

		InputStream in = jarFile.getInputStream(jarFile.getEntry(file));
		OutputStream out = new FileOutputStream(file);

		byte[] buffer = new byte[65536];
		int bufferSize;
		while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1){
			out.write(buffer, 0, bufferSize);
		}

		in.close();
		out.close();
		jarFile.close();
	}
}
