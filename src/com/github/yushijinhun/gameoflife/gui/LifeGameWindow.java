package com.github.yushijinhun.gameoflife.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.github.yushijinhun.gameoflife.core.LifeGameEngine;

public class LifeGameWindow extends Frame {
	
	private static final long serialVersionUID = 1L;
	private static final float MAXIMIZE_FACTOR=0.75f;
	
	public LifeGameGui gui;
	
	public LifeGameWindow(double blockSize,LifeGameEngine engine) {
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
		setSize((int) (Math.rint(gui.getX()+gui.engine.width*gui.getCellSize()+8)),(int) (Math.rint(gui.getY()+gui.engine.height*gui.getCellSize()+8)));
		setVisible(true);
		
		//when the window's size > 3/4 the screen's size, set the window maximized.
		Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
		if (d.getWidth()*MAXIMIZE_FACTOR<=getWidth()||d.getHeight()*MAXIMIZE_FACTOR<=getHeight()){
			setExtendedState(MAXIMIZED_BOTH);
		}
	}
}
