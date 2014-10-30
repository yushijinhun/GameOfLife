package com.github.yushijinhun.gameoflife.gui;

import javax.swing.JDialog;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessListener;

public class ScaleWindow extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	private final ScalePanel scalePanel;
	
	public ScaleWindow(LifeGameWindow window) {
		super(window, "Scale", true);
		scalePanel = new ScalePanel(window);
		add(scalePanel);
		
		scalePanel.addDataProcessListener(new DataProcessListener() {
			
			@Override
			public void process(DataProcessEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
	
}
