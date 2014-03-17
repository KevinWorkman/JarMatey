package com.staticvoidgames.svgexe.tool;

import javax.swing.JOptionPane;

import com.staticvoidgames.svgexe.gui.SvgExeGui;

import processing.app.Editor;
import processing.app.tools.Tool;

public class SvgExeProcessingTool implements Tool{

	@Override
	public String getMenuTitle() {
		return "SvgExe";
	}

	@Override
	public void init(Editor e) {
		JOptionPane.showMessageDialog(null, "INIT");
		
	}

	@Override
	public void run() {
		SvgExeGui.main(new String[]{});
	}

}
