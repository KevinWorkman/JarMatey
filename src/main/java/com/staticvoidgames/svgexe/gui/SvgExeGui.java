package com.staticvoidgames.svgexe.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.staticvoidgames.svgexe.SvgExe;


//TODO: comments
public class SvgExeGui {

	private String previousFile = "";

	private JTextField mainClassTextField = new JTextField();
	private JTextField versionTextField = new JTextField("1.0");
	private JTextField nativesInJarDirectoryTextField = new JTextField("SvgExeNatives");
	private JTextField externalFilesDirectoryTextField = new JTextField("SvgExeExternalFiles");
	private JTextField argsTextField = new JTextField();
	private JTextField jvmOptionsTextField = new JTextField();
	private JCheckBox extractToTempDirCheckBox = new JCheckBox("Extract to temp directory", true);

	private List<File> includedJars = new ArrayList<File>();
	private List<File> includedClasses = new ArrayList<File>();
	private List<File> includedNatives = new ArrayList<File>();

	private List<File> includedExternalFiles = new ArrayList<File>();

	private JTextField outputTextField = new JTextField();

	private Dimension pathTextFieldMaxSize = new Dimension(1000, 30);

	private JEditorPane optionalSettingsInfoPane = new JEditorPane();
	private JEditorPane classesInfoPane = new JEditorPane();
	private JEditorPane externalFilesInfoPane = new JEditorPane();
	private JEditorPane nativesInfoPane = new JEditorPane();
	private JEditorPane outputInfoPane = new JEditorPane();

	private JProgressBar progressBar = new JProgressBar();
	
	private boolean showSplash = false;
	private boolean useCustomSplash = false;
	private JTextField customSplashImageLocation = new JTextField();


	public SvgExeGui(){

		JFrame frame = new JFrame("SvgExe");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try{
			List<Image> images = new ArrayList<Image>();
			images.add(new ImageIcon(SvgExeGui.class.getClassLoader().getResource("16.png")).getImage());
			images.add(new ImageIcon(SvgExeGui.class.getClassLoader().getResource("32.png")).getImage());
			frame.setIconImages(images);
		}
		catch(Exception e){
			//who cares
		}

		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		frame.setContentPane(tabbedPane);

		tabbedPane.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {

				if(tabbedPane.getSelectedIndex() == 0){

					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<h3>Included Classes</h3>");
					sb.append("<p>Use this tab to add classes to your self-extracting jar.</p>");
					sb.append("<p>To add the classes from an existing jar, simply add the jar to the list on the left.</p>");
					sb.append("<p>You can also add .class files directly. If a class is in a package, add the top-level package instead of the class.</p>");
					sb.append("<ul><li>If your class's package is com.example.test, add the 'com' directory.</li>");
					sb.append("<li>If your class is not in a package, add the class directly.</li></ul>");
					sb.append("Note: These should be .class files, not .java files!");

					sb.append("</html>");

					classesInfoPane.setText(sb.toString());
					classesInfoPane.setCaretPosition(0);
				}
				else if(tabbedPane.getSelectedIndex() == 1){


					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<h3>External Files</h3>");
					sb.append("<p>Use this tab to add external files to your jar. These files will be automatically extracted to an external folder that your program can access.</p>");
					sb.append("<p>To add a file, simply add it to the list.</p>");
					sb.append("<p>You can also add entire folders to the list. The folder name will be retained.</p>");

					sb.append("</html>");

					externalFilesInfoPane.setText(sb.toString());
					externalFilesInfoPane.setCaretPosition(0);
				}
				else if(tabbedPane.getSelectedIndex() == 2){


					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<h3>Natives</h3>");
					sb.append("<p>Use this tab to add native libraries to your jar. These files will be automatically extracted to an external folder and your java.library.path will be set to that folder.</p>");
					sb.append("<p>To add a file that will be extracted on every system your jar is run on, simply add it to the list here.</p>");
					sb.append("<p>To target specific systems and extract only the correct natives for the user's system, you can use SvgExe's system detection feature by adding OS-specific folders.</p>");
					sb.append("<p>For example, if you have native files for Windows, Linux, and Mac, do the following:</p>");
					
					sb.append("<ul><li>Create a directory named Windows, a directory named Linux, and a directory named Mac.</li>");
					sb.append("<li>Put the native files in the corresponding directory.</li>");
					sb.append("<li>Add each of those directories here.</li>");
					sb.append("<li>Note: Matching for the top-level OS-specific directories uses the startsWith() function, so a directory named Windows will be extracted for Windows XP, Windows 7, Windows 8, etc. Case and whitespace doesn't matter.</li></ul>");
							
					sb.append("<p>For even more customization, you can include subdirectories that correspond to the os.arch system property.");
					sb.append("<p>For example, to include 32-bit natives as well as 64-bit natives for Windows systems, you would:");
					sb.append("<ul><li>Find out the os.arch system property for your target systems (in our case, 32-bit is x86 and 64-bit is amd64)</li>");
					sb.append("<li>Create subdirectories x86 and amd64 within the Windows directory.</li>");
					sb.append("<li>Add your native files to the corresponding subdirectory.</li>");
					sb.append("<li>Add the top-level OS-specific directory (Windows, Mac, Linux) here.</li>");
		
					sb.append("<li>Note: Matching for these subdirectories uses the equals() function, so substrings will not match, and case matters.</li></ul>");
					
					sb.append("<p>You can also use this feature to include OS-specific jars on the classpath. Include them on this tab, not on the first tab.</p>");
					
					sb.append("</html>");

					nativesInfoPane.setText(sb.toString());
					nativesInfoPane.setCaretPosition(0);
				}
				else if(tabbedPane.getSelectedIndex() == 3){

					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<h3>Optional Settings</h3>");
					sb.append("<p>Use this tab to specify optional settings.</p>");
					sb.append("<p>You probably don't have to worry about these.</p>");
					sb.append("</html>");

					optionalSettingsInfoPane.setText(sb.toString());
					optionalSettingsInfoPane.setCaretPosition(0);
				}
				else if(tabbedPane.getSelectedIndex() == 4){

					StringBuilder sb = new StringBuilder("<html>");
					sb.append("<h3>Output</h3>");
					sb.append("<p>Specify the main class, any arguments, and the output jar here.</p>");
					sb.append("</html>");

					outputInfoPane.setText(sb.toString());
					outputInfoPane.setCaretPosition(0);
				}
			}
		});

		tabbedPane.addTab("Step 1: Classes", createIncludeJarTab());
		tabbedPane.addTab("Step 2: External Files", createExternalFilesTab());
		tabbedPane.addTab("Step 3: Natives", createNativesTab());
		tabbedPane.addTab("Step 4: Optional", createOptionalSettingsTab());
		tabbedPane.addTab("Step 5: Output", createOutputTab());

		frame.setSize(600, 550);
		frame.setVisible(true);
	}

	private JPanel createIncludeJarTab(){

		classesInfoPane.setEditable(false);
		classesInfoPane.setContentType("text/html");

		JScrollPane infoScrollPane = new JScrollPane(classesInfoPane);

		JPanel includedClassesBorderPanel = createFileList("Classes and Packages:", includedClasses, true);
		JPanel includedJarsBorderPanel = createFileList("Jars:", includedJars, false);

		JPanel topPanel= new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

		topPanel.add(includedJarsBorderPanel);
		topPanel.add(includedClassesBorderPanel);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(topPanel);
		panel.add(infoScrollPane);

		return panel;
	}


	private JPanel createFileList(String title, final List<File> fileList, final boolean directories){

		JPanel includedBorderPanel = new JPanel();
		includedBorderPanel.setLayout(new BoxLayout(includedBorderPanel, BoxLayout.Y_AXIS));
		includedBorderPanel.setBorder(BorderFactory.createTitledBorder(title));

		final JPanel includedFilesPanel = new JPanel();
		includedFilesPanel.setLayout(new BoxLayout(includedFilesPanel, BoxLayout.Y_AXIS));


		final JButton addIncludedFileButton = new JButton("Add...");
		addIncludedFileButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser fc = new JFileChooser(previousFile);
				if(directories){
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				}
				fc.setMultiSelectionEnabled(true);
				int returnVal = fc.showOpenDialog(addIncludedFileButton);

				if (returnVal == JFileChooser.APPROVE_OPTION) {

					for(final File file : fc.getSelectedFiles()){

						previousFile = file.getAbsolutePath();

						fileList.add(file);


						final JPanel includedFilePanel = new JPanel();
						includedFilePanel.setLayout(new BoxLayout(includedFilePanel, BoxLayout.X_AXIS));

						JLabel label = new JLabel(file.getName());
						JButton removeButton = new JButton("X");
						removeButton.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {

								fileList.remove(file);

								includedFilesPanel.remove(includedFilePanel);
								includedFilesPanel.revalidate();
								includedFilesPanel.repaint();
							}
						});

						includedFilePanel.add(label);
						includedFilePanel.add(removeButton);

						includedFilePanel.setToolTipText(file.getAbsolutePath());

						includedFilesPanel.add(includedFilePanel);
						includedFilesPanel.revalidate();
						includedFilesPanel.repaint();

					}
				} 
			}
		});

		JScrollPane includedScrollPane = new JScrollPane(includedFilesPanel);

		includedBorderPanel.add(includedScrollPane);
		includedBorderPanel.add(addIncludedFileButton);

		includedBorderPanel.setMinimumSize(new Dimension(200, 300));
		includedBorderPanel.setPreferredSize(new Dimension(200, 300));

		return includedBorderPanel;
	}

	private JPanel createExternalFilesTab(){

		externalFilesInfoPane.setEditable(false);
		externalFilesInfoPane.setContentType("text/html");

		JPanel includedBorderPanel = createFileList("Include these files:", includedExternalFiles, true);

		JScrollPane infoScrollPane = new JScrollPane(externalFilesInfoPane);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(includedBorderPanel);
		panel.add(infoScrollPane);

		return panel;
	}

	private JPanel createNativesTab(){

		nativesInfoPane.setEditable(false);
		nativesInfoPane.setContentType("text/html");

		JPanel includedBorderPanel = createFileList("Natives:", includedNatives, true);

		JScrollPane infoScrollPane = new JScrollPane(nativesInfoPane);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(includedBorderPanel);
		panel.add(infoScrollPane);

		return panel;
	}

	public JPanel createOptionalSettingsTab(){

		optionalSettingsInfoPane.setEditable(false);
		optionalSettingsInfoPane.setContentType("text/html");

		versionTextField.setMaximumSize(pathTextFieldMaxSize);
		nativesInJarDirectoryTextField.setMaximumSize(pathTextFieldMaxSize);
		externalFilesDirectoryTextField.setMaximumSize(pathTextFieldMaxSize);

		JPanel versionPanel = createLabeledComponent("Version: ", versionTextField);
		JPanel nativesDirectoryPanel = createLabeledComponent("Natives in jar Directory: ", nativesInJarDirectoryTextField);
		JPanel externalFilesDirectoryPanel = createLabeledComponent("External Files in jar Directory: ", externalFilesDirectoryTextField);

		versionPanel.addMouseListener(new MouseAdapter(){

			public void mouseEntered(MouseEvent me){

				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Version</h3>");
				sb.append("<p>Specify the version. This goes in the manifest of the generated jar.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);
			}
		});

		nativesDirectoryPanel.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				optionalSettingsInfoPane.setText("<html><h3>In-Jar Natives Directory</h3><p>Set the natives directory.</p></html>");

				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>In-Jar Natives Directory</h3>");
				sb.append("<p>Specify the directory (inside the jar) the natives should be added to. You shouldn't have to change this unless you have a package with the same name as the native directory for some strange reason.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);

			}
		});

		externalFilesDirectoryPanel.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>In-Jar External Files Directory</h3>");
				sb.append("<p>Specify the directory (inside the jar) the external files should be added to. You shouldn't have to change this unless you have a package with the same name as the external files directory for some strange reason.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);
			}
		});
		
		customSplashImageLocation.setMaximumSize(pathTextFieldMaxSize);
		
		final JButton browseCustomSplashImage = new JButton("Choose...");
		browseCustomSplashImage.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(previousFile);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(browseCustomSplashImage);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					
					previousFile = file.getAbsolutePath();
					customSplashImageLocation.setText(file.getAbsolutePath());
				} 
			}

		});
		
		customSplashImageLocation.setEnabled(false);
		browseCustomSplashImage.setEnabled(false);
		
		final JCheckBox useCustomSplashImage = new JCheckBox("Use custom splash image:");
		useCustomSplashImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				customSplashImageLocation.setEnabled(useCustomSplashImage.isSelected());
				browseCustomSplashImage.setEnabled(useCustomSplashImage.isSelected());
				useCustomSplash = useCustomSplashImage.isSelected();
			}
		});
		
		final JCheckBox showSplashCheckBox = new JCheckBox("Show Splash Screen", showSplash);
		showSplashCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				browseCustomSplashImage.setEnabled(showSplashCheckBox.isSelected());
				customSplashImageLocation.setEnabled(showSplashCheckBox.isSelected());
				useCustomSplashImage.setEnabled(showSplashCheckBox.isSelected());
				showSplash = showSplashCheckBox.isSelected();
			}
		});
		
		JPanel splashPanel = new JPanel();
		splashPanel.setLayout(new BoxLayout(splashPanel, BoxLayout.X_AXIS));
		splashPanel.add(showSplashCheckBox);
		splashPanel.add(useCustomSplashImage);
		splashPanel.add(customSplashImageLocation);
		splashPanel.add(browseCustomSplashImage);
		
		MouseListener splashMouseListener = new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Splash Screen</h3>");
				sb.append("<p>A splash screen can be shown to the user before your program starts.</p>");
				sb.append("<p>If your program contains a lot of external files or natives, it can take a few seconds to extract them before your program starts. This is when the splash screen will be shown.</p>");
				sb.append("<p>You can specify a custom splash image, or a default one will be shown.</p>");
				sb.append("<p>If your jar only contains a few external files or natives, a splash screen might not be necessary so you can deactivate it.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);
			}
		};
		splashPanel.addMouseListener(splashMouseListener);
		showSplashCheckBox.addMouseListener(splashMouseListener);
		useCustomSplashImage.addMouseListener(splashMouseListener);
		customSplashImageLocation.addMouseListener(splashMouseListener);
		browseCustomSplashImage.addMouseListener(splashMouseListener);
		
		extractToTempDirCheckBox.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		extractToTempDirCheckBox.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Extract to Temp Directory</h3>");
				sb.append("<p>By default, your natives and external files will be extracted to a temp directory.</p>");
				sb.append("<p>End users will not see this directory, and it will be deleted after your program exits.</p>");
				sb.append("<p>If you uncheck this, your natives and files will be extracted to a visible directory next to the self-extracting jar.</p>");
				sb.append("<p>This is mainly used for testing purposes, and you probably don't need to change it.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);
			}
		});
		
		JPanel argsPanel = createLabeledComponent("Program Arguments:", argsTextField);
		MouseListener argsMouseListener = new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Program Arguments</h3>");
				sb.append("<p>These arguments are passed into the main() method of your program.</p>");
				sb.append("<p>Arguments are separated by spaces and fill the args[] array parameter of the main() method.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);
			}
		};
		argsPanel.addMouseListener(argsMouseListener);
		argsTextField.addMouseListener(argsMouseListener);
		
		JPanel jvmOptionsPanel = createLabeledComponent("JVM Options:", jvmOptionsTextField);
		MouseListener jvmMouseListener = new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>JVM Options</h3>");
				sb.append("<p>These arguments are passed into the JVM.</p>");
				sb.append("<p>Note: DO NOT specify a classpath option here. Anything on your classpath should be added to your self-extracting jar on the first tab.</p>");
				sb.append("</html>");

				optionalSettingsInfoPane.setText(sb.toString());
				optionalSettingsInfoPane.setCaretPosition(0);
			}
		};
		jvmOptionsPanel.addMouseListener(jvmMouseListener);
		jvmOptionsTextField.addMouseListener(jvmMouseListener);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(splashPanel);
		topPanel.add(argsPanel);
		topPanel.add(jvmOptionsPanel);
		topPanel.add(versionPanel);
		topPanel.add(nativesDirectoryPanel);
		topPanel.add(externalFilesDirectoryPanel);
		topPanel.add(extractToTempDirCheckBox);
		
		JScrollPane infoScrollPane = new JScrollPane(optionalSettingsInfoPane);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(topPanel);
		panel.add(infoScrollPane);

		return panel;
	}

	private JPanel createLabeledComponent(String s, JComponent c){
		JLabel label = new JLabel(s);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(label);
		panel.add(c);

		return panel;
	}

	private JPanel createOutputTab(){

		outputInfoPane.setContentType("text/html");
		outputInfoPane.setEditable(false);

		outputInfoPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					}
					catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JLabel outputLocationLabel = new JLabel("Output to file:");

		outputTextField.setMaximumSize(new Dimension(1000, 40));
		final JButton outputLocationButton = new JButton("Browse...");

		outputLocationButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(previousFile);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showSaveDialog(outputLocationButton);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if(! (file.getName().endsWith(".jar") || file.getName().endsWith(".JAR")) ){
						file = new File (file.getAbsolutePath() + ".jar");
					}
					previousFile = file.getAbsolutePath();
					outputTextField.setText(file.getAbsolutePath());
				} 
				else {
					//cancelled
				}
			}
		});

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.X_AXIS));
		outputPanel.add(outputLocationLabel);
		outputPanel.add(outputTextField);
		outputPanel.add(outputLocationButton);
		
		JLabel mainClassLabel = new JLabel("Main class:");
		mainClassTextField.setMaximumSize(new Dimension(1000, 40));
		JPanel mainClassPanel = new JPanel();
		mainClassPanel.setLayout(new BoxLayout(mainClassPanel, BoxLayout.X_AXIS));
		mainClassPanel.add(mainClassLabel);
		mainClassPanel.add(mainClassTextField);

		MouseListener mainClassListener = new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Main Class</h3>");
				sb.append("<p>Specify the class that contains the main method. This is the entry point to your program.</p>");
				sb.append("<p>Processing developers: This is just the name of your sketch.");
				sb.append("<p>Java developers: Be sure to include package information here.</p>");
				sb.append("<ul><li>If your main class is named Main and in class com.example.test, you should put com.example.test.Main here.</li>");
				sb.append("<li>If your main class is named Main and not in a package, you should put Main here.</li></ul>");
				sb.append("<p>You should not include the .class extension, just the name of the class.</p>");
				sb.append("</html>");

				outputInfoPane.setText(sb.toString());
				outputInfoPane.setCaretPosition(0);
			}
		};

		mainClassPanel.addMouseListener(mainClassListener);
		mainClassTextField.addMouseListener(mainClassListener);

		MouseListener outputListener = new MouseAdapter(){
			public void mouseEntered(MouseEvent me){
				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Output</h3>");
				sb.append("<p>Specify the name of the self-extracting jar to be generated.</p>");
				sb.append("<p>This is the file you should send to users or upload to <a href=\"http://StaticVoidGames.com\">Static Void Games</a>.</p>");

				sb.append("</html>");

				outputInfoPane.setText(sb.toString());
				outputInfoPane.setCaretPosition(0);
			}
		};

		outputPanel.addMouseListener(outputListener);
		outputLocationButton.addMouseListener(outputListener);
		outputTextField.addMouseListener(outputListener);

		final JButton okayButton = new JButton("Go!");
		okayButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(new File(outputTextField.getText()).exists()){
					int choice = JOptionPane.showConfirmDialog(okayButton, "Output file already exists. Overwrite?", "Overwrite file?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(SvgExeGui.class.getClassLoader().getResource("32.png")));
					if(choice == JOptionPane.CANCEL_OPTION){
						return;
					}
				}
				
				progressBar.setVisible(true);

				try {
					new Thread(){
						public void run(){

							final boolean created = createOutputJar(outputTextField.getText());

							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									progressBar.setVisible(false);
									if(created){
										JOptionPane.showMessageDialog(okayButton, "Your self-extracting jar was succesfully created!", "Success!", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(SvgExeGui.class.getClassLoader().getResource("32.png")));
									}
								}
							});
						}
					}.start();
				} 
				catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});
		okayButton.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent me){

				StringBuilder sb = new StringBuilder("<html>");
				sb.append("<h3>Output</h3>");

				boolean goodToGo = true;

				if(includedJars.isEmpty() && includedClasses.isEmpty()){

					sb.append("<p><span style=\"color:red\">Warning:</span> You didn't add any classes!</p>");

					goodToGo = false;
				}
				if("".equals(outputTextField.getText())){

					sb.append("<p><span style=\"color:red\">Warning:</span> You didn't specify an output file!</p>");

					goodToGo = false;
				}

				if(goodToGo){
					sb.append("<p>Give everything a once-over, and click okay to create the self-extracting jar.</p>");
				}
				else{
					sb.append("<p>Please fix the above errors before continuing.</p>");
				}
				sb.append("<p>Once the self-extracting jar is created, make sure you test it! This program is still experimental.</p>");
				sb.append("</html>");
				
				okayButton.setEnabled(goodToGo);

				outputInfoPane.setText(sb.toString());
				outputInfoPane.setCaretPosition(0);
			}
		});

		progressBar.setVisible(false);
		progressBar.setStringPainted(true);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		topPanel.add(mainClassPanel);
		topPanel.add(outputPanel);
		topPanel.add(okayButton);

		JScrollPane infoScrollPane = new JScrollPane(outputInfoPane);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(topPanel);
		panel.add(infoScrollPane);
		panel.add(progressBar);

		return panel;
	}

	private void setProgressOnEdt(final int progress, final String text){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				progressBar.setValue(progress);
				progressBar.setString(text);
			}
		});
	}

	private boolean createOutputJar(String jarFile){

		SvgExe svgExe = null;
		
		try {
			setProgressOnEdt(0, "Creating self-extracting jar.");
			svgExe = new SvgExe(jarFile, mainClassTextField.getText(), versionTextField.getText(), nativesInJarDirectoryTextField.getText(), externalFilesDirectoryTextField.getText(), showSplash, useCustomSplash, new File(customSplashImageLocation.getText()), extractToTempDirCheckBox.isSelected(), argsTextField.getText(), jvmOptionsTextField.getText(), !includedNatives.isEmpty(), !includedExternalFiles.isEmpty());
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(outputInfoPane, "There was a problem creating the self-extracting jar.\n" + e.getMessage(), "Problem creating self-extracting jar", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try{
			setProgressOnEdt(20, "Adding classes from you jars.");
			svgExe.addFilesFromJars(includedJars);
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(outputInfoPane, "There was a problem adding classes from your included jars to the self-extracting jar.\n" + e.getMessage(), "Problem adding jars", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try{
			setProgressOnEdt(40, "Adding your classes.");
			svgExe.addClasses(includedClasses);
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(outputInfoPane, "There was a problem adding your classes to the self-extracting jar.\n" + e.getMessage(), "Problem adding classes", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try{
			setProgressOnEdt(60, "Adding your natives.");
			svgExe.addNatives(nativesInJarDirectoryTextField.getText(), includedNatives);
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(outputInfoPane, "There was a problem adding your natives to the self-extracting jar.\n" + e.getMessage(), "Problem adding natives", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try{
			setProgressOnEdt(80, "Adding your external files.");
			svgExe.addExternalFiles(externalFilesDirectoryTextField.getText(), includedExternalFiles);
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(outputInfoPane, "There was a problem adding your external files to the self-extracting jar.\n" + e.getMessage(), "Problem adding external files", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try{
			setProgressOnEdt(0, "Finalizing your self-extracting jar.");
			svgExe.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(outputInfoPane, "There was a problem closing the self-extracting jar.\n" + e.getMessage(), "Problem closing self-extracting jar", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}

	public static void main(String... args){

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} 
		catch (Exception e) {
			//no nimbus
		}

		new SvgExeGui();
	}
}