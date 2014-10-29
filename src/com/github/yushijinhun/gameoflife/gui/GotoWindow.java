package com.github.yushijinhun.gameoflife.gui;

import javax.swing.JDialog;
import com.github.yushijinhun.gameoflife.gui.event.GotoEvent;
import com.github.yushijinhun.gameoflife.gui.event.GotoListener;

public class GotoWindow extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private final GotoPanel gotoPanel;
	
	public GotoWindow(LifeGameWindow window) {
		super(window, "Goto",true);
		gotoPanel=new GotoPanel(window);
		add(gotoPanel);
		
		gotoPanel.addGotoListener(new GotoListener() {
			
			@Override
			public void onGoto(GotoEvent e) {
				setVisible(false);
			}
		});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
}
