package com.github.yushijinhun.gameoflife;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LifeGameWindow extends Frame {
	
	private static final long serialVersionUID = 1L;
	
	public LifeGameGui gui;
	
	public LifeGameWindow(int blockSize,LifeGameEngine engine) {
		gui=new LifeGameGui(blockSize,engine);
		add(gui);
		
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				gui.shutdown();
				setVisible(false);
				dispose();
			}
		});
		
		addWindowFocusListener(new WindowAdapter() {
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
				gui.requestFocus();
			}
		});
		
		setTitle("Game of Life");
		pack();
		setSize(gui.getX()+gui.engine.width*gui.getCellSize()+8,gui.getY()+gui.engine.height*gui.getCellSize()+8);
		setVisible(true);
	}
}
