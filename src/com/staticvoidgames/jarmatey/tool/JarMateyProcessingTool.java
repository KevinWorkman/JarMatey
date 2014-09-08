package com.staticvoidgames.jarmatey.tool;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.staticvoidgames.jarmatey.gui.JarMateyGui;

import processing.app.Editor;
import processing.app.tools.Tool;

public class JarMateyProcessingTool implements Tool{
	
	Editor e;

	@Override
	public String getMenuTitle() {
		return "JarMatey";
	}

	@Override
	public void init(Editor e) {
		this.e = e;
	}

	@Override
	public void run(){
		JarMateyGui.main(new String[]{"true"});
	}
	
	public void showProcessingInstructions() {
		
		JLabel label1 = new JLabel("<html><p><b>Step 1</b>: Select 'Export Application' from the 'File' menu. <br/>(if you haven't done that yet, cancel and go do that now)</p></html>");
		JLabel label2 = new JLabel("<html><p><b>Step 2</b>: Specify the location of the application directory output as a result. <br/>JarMatey automagically populates itself.</p></html>");
		JLabel label3 = new JLabel("<html><p><b>Step 3</b>: In the JarMatey window, specify an output jar and hit go to create your self-extracting jar!</p></html>");
		
		final JTextField applicationLocation = new JTextField();
		applicationLocation.setMaximumSize(new Dimension(1000, 40));
		
		final JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser fc = new JFileChooser(e.getSketch().getFolder());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(browseButton);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					applicationLocation.setText(file.getAbsolutePath());
				} 
				else {
					//cancelled
				}
			}
		});
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		textPanel.add(applicationLocation);
		textPanel.add(browseButton);
		
		label1.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		label2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		label3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		textPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(label1);
		panel.add(label2);
		panel.add(label3);
		panel.add(textPanel);
		
		int choice = JOptionPane.showConfirmDialog(null, panel, "JarMatey", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(JarMateyGui.class.getClassLoader().getResource("32.png")));
		
		//TODO pass in the application folder to a method that goes through and populates JarMatey
		if(choice == JOptionPane.OK_OPTION){
			JarMateyGui.main(new String[]{"true"});
		}
		
	}

}
