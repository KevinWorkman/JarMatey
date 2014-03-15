package com.staticvoidgames.svgexe.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Class that reads from a stream on another thread. This is a workaround for a bug with ProcessBuilder in Windows.
 * @author Kevin
 */
public class StreamGobbler extends Thread { 
	
	private InputStream is; 
	private PrintStream os;


	public StreamGobbler(InputStream is, PrintStream os) { 
		this.is = is;  
		this.os = os;
	} 

	public void run() { 
		try { 
			InputStreamReader isr = new InputStreamReader(is); 
			BufferedReader br = new BufferedReader(isr); 
			String line=null; 
			while ( (line = br.readLine()) != null) {
				os.println(line); 
			}
			isr.close();
		} 
		catch (IOException ioe) { 
			ioe.printStackTrace(); 
		} 
	} 
} 