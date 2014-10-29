package com.github.yushijinhun.gameoflife.gui;

import javax.swing.JFrame;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessListener;

public class SettingWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final SettingPanel settingPanel;
	
	public SettingWindow() {
		super("Game of Life");
		settingPanel=new SettingPanel();
		add(settingPanel);
		
		settingPanel.addDataProcessListener(new DataProcessListener() {
			
			@Override
			public void process(DataProcessEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
}
