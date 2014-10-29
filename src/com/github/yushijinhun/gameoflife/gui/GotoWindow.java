package com.github.yushijinhun.gameoflife.gui;

import javax.swing.JFrame;
import com.github.yushijinhun.gameoflife.gui.event.GotoEvent;
import com.github.yushijinhun.gameoflife.gui.event.GotoListener;

public class GotoWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final GotoPanel gotoPanel;
	
	public GotoWindow(LifeGameWindow window) {
		super("Game of Life");
		gotoPanel=new GotoPanel(window);
		add(gotoPanel);
		
		gotoPanel.addGotoListener(new GotoListener() {
			
			@Override
			public void onGoto(GotoEvent e) {
				setVisible(false);
				dispose();
			}
		});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
}
