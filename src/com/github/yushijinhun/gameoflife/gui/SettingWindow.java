package com.github.yushijinhun.gameoflife.gui;

import javax.swing.JFrame;
import com.github.yushijinhun.gameoflife.gui.event.GameStartedEvent;
import com.github.yushijinhun.gameoflife.gui.event.GameStartedListener;

public class SettingWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final SettingPanel settingPanel;
	
	public SettingWindow() {
		super("Game of Life");
		settingPanel=new SettingPanel();
		add(settingPanel);
		
		settingPanel.addGameStartedListener(new GameStartedListener() {
			
			@Override
			public void gameStarted(GameStartedEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
}
