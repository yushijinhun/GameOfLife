package com.github.yushijinhun.gameoflife.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import com.github.yushijinhun.gameoflife.core.LifeGameEngine;
import com.github.yushijinhun.gameoflife.core.LifeGameEngineConfiguration;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;
import com.github.yushijinhun.gameoflife.util.ExceptionUtil;
import com.github.yushijinhun.nbt4j.io.TagInputStream;

public class SettingPanel extends DataInputPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField textWidth;
	private final JTextField textHeight;
	private final JTextField textFilePath;
	private final JTextField textThreads;
	private final JTextField textScale;
	private final JRadioButton buttonNewGame;
	private final JCheckBox chckbxRandom;
	private final JLabel labelWidth;
	private final JLabel labelHeight;
	private final JRadioButton buttonLoadGame;
	private final JButton buttonOpenDialog;
	private final JLabel labelThreads;
	private final JLabel labelScale;
	private final JButton buttonStart;
	
	private final ButtonGroup gameModeGroup;
	private final NumberOnlyKeyListener numberOnly;
	private final Runnable starter;
	
	public SettingPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		buttonNewGame = new JRadioButton("New Game");
		GridBagConstraints gbc_buttonNewGame = new GridBagConstraints();
		gbc_buttonNewGame.gridwidth = 3;
		gbc_buttonNewGame.insets = new Insets(0, 0, 5, 5);
		gbc_buttonNewGame.gridx = 0;
		gbc_buttonNewGame.gridy = 0;
		add(buttonNewGame, gbc_buttonNewGame);
		
		chckbxRandom = new JCheckBox("Random Generate");
		GridBagConstraints gbc_chckbxRandom = new GridBagConstraints();
		gbc_chckbxRandom.gridwidth = 2;
		gbc_chckbxRandom.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRandom.gridx = 3;
		gbc_chckbxRandom.gridy = 0;
		add(chckbxRandom, gbc_chckbxRandom);
		
		labelWidth = new JLabel("Width:");
		GridBagConstraints gbc_labelWidth = new GridBagConstraints();
		gbc_labelWidth.insets = new Insets(0, 0, 5, 5);
		gbc_labelWidth.anchor = GridBagConstraints.EAST;
		gbc_labelWidth.gridx = 6;
		gbc_labelWidth.gridy = 0;
		add(labelWidth, gbc_labelWidth);
		
		textWidth = new JTextField();
		GridBagConstraints gbc_textWidth = new GridBagConstraints();
		gbc_textWidth.gridwidth = 5;
		gbc_textWidth.insets = new Insets(0, 0, 5, 5);
		gbc_textWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_textWidth.gridx = 7;
		gbc_textWidth.gridy = 0;
		add(textWidth, gbc_textWidth);
		textWidth.setColumns(10);
		
		labelHeight = new JLabel("Height:");
		GridBagConstraints gbc_labelHeight = new GridBagConstraints();
		gbc_labelHeight.anchor = GridBagConstraints.EAST;
		gbc_labelHeight.insets = new Insets(0, 0, 5, 5);
		gbc_labelHeight.gridx = 6;
		gbc_labelHeight.gridy = 1;
		add(labelHeight, gbc_labelHeight);
		
		textHeight = new JTextField();
		GridBagConstraints gbc_textHeight = new GridBagConstraints();
		gbc_textHeight.gridwidth = 5;
		gbc_textHeight.insets = new Insets(0, 0, 5, 5);
		gbc_textHeight.fill = GridBagConstraints.HORIZONTAL;
		gbc_textHeight.gridx = 7;
		gbc_textHeight.gridy = 1;
		add(textHeight, gbc_textHeight);
		textHeight.setColumns(10);
		
		buttonLoadGame = new JRadioButton("Load Game");
		GridBagConstraints gbc_buttonLoadGame = new GridBagConstraints();
		gbc_buttonLoadGame.gridwidth = 3;
		gbc_buttonLoadGame.insets = new Insets(0, 0, 5, 5);
		gbc_buttonLoadGame.gridx = 0;
		gbc_buttonLoadGame.gridy = 3;
		add(buttonLoadGame, gbc_buttonLoadGame);
		
		textFilePath = new JTextField();
		GridBagConstraints gbc_textFilePath = new GridBagConstraints();
		gbc_textFilePath.gridwidth = 9;
		gbc_textFilePath.insets = new Insets(0, 0, 5, 5);
		gbc_textFilePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFilePath.gridx = 3;
		gbc_textFilePath.gridy = 3;
		add(textFilePath, gbc_textFilePath);
		textFilePath.setColumns(10);
		
		buttonOpenDialog = new JButton("...");
		GridBagConstraints gbc_buttonOpenDialog = new GridBagConstraints();
		gbc_buttonOpenDialog.insets = new Insets(0, 0, 5, 0);
		gbc_buttonOpenDialog.gridx = 12;
		gbc_buttonOpenDialog.gridy = 3;
		add(buttonOpenDialog, gbc_buttonOpenDialog);
		
		labelThreads = new JLabel("Threads:");
		GridBagConstraints gbc_labelThreads = new GridBagConstraints();
		gbc_labelThreads.gridwidth = 2;
		gbc_labelThreads.insets = new Insets(0, 0, 5, 5);
		gbc_labelThreads.gridx = 0;
		gbc_labelThreads.gridy = 5;
		add(labelThreads, gbc_labelThreads);
		
		textThreads = new JTextField();
		GridBagConstraints gbc_textThreads = new GridBagConstraints();
		gbc_textThreads.gridwidth = 3;
		gbc_textThreads.insets = new Insets(0, 0, 5, 5);
		gbc_textThreads.fill = GridBagConstraints.HORIZONTAL;
		gbc_textThreads.gridx = 2;
		gbc_textThreads.gridy = 5;
		add(textThreads, gbc_textThreads);
		textThreads.setColumns(10);
		
		labelScale = new JLabel("Scale:");
		GridBagConstraints gbc_labelScale = new GridBagConstraints();
		gbc_labelScale.gridwidth = 2;
		gbc_labelScale.insets = new Insets(0, 0, 0, 5);
		gbc_labelScale.gridx = 0;
		gbc_labelScale.gridy = 6;
		add(labelScale, gbc_labelScale);
		
		textScale = new JTextField();
		GridBagConstraints gbc_textScale = new GridBagConstraints();
		gbc_textScale.gridwidth = 3;
		gbc_textScale.insets = new Insets(0, 0, 0, 5);
		gbc_textScale.fill = GridBagConstraints.HORIZONTAL;
		gbc_textScale.gridx = 2;
		gbc_textScale.gridy = 6;
		add(textScale, gbc_textScale);
		textScale.setColumns(10);
		
		buttonStart = new JButton("Start");
		GridBagConstraints gbc_buttonStart = new GridBagConstraints();
		gbc_buttonStart.gridx = 12;
		gbc_buttonStart.gridy = 6;
		add(buttonStart, gbc_buttonStart);
		
		gameModeGroup=new ButtonGroup();
		gameModeGroup.add(buttonLoadGame);
		gameModeGroup.add(buttonNewGame);
		
		numberOnly=new NumberOnlyKeyListener();
		buttonLoadGame.setSelected(false);
		buttonNewGame.setSelected(true);
		updateGameModeStatus();
		
		textWidth.setText("160");
		textHeight.setText("120");
		textScale.setText("1");
		textThreads.setText(String.valueOf(LifeGameEngineConfiguration.DEFAULT_THREADS));
		
		ActionListener gameModeActionListener=new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateGameModeStatus();
			}
		};
		buttonLoadGame.addActionListener(gameModeActionListener);
		buttonNewGame.addActionListener(gameModeActionListener);
		
		buttonOpenDialog.addActionListener(new ActionListener() {
			
			private final JFileChooser chooser=new JFileChooser();
			
			{
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Open");
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.showSaveDialog(SettingPanel.this);
				
				File file=chooser.getSelectedFile();
				
				if (file == null) {
					JOptionPane.showMessageDialog(SettingPanel.this, "You did not select any files.", "Game of Life", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				textFilePath.setText(file.getPath());
			}
		});
		
		numberOnly.addControl(buttonStart);
		numberOnly.addListen(textScale);
		numberOnly.addListen(textThreads);
		
		textWidth.addKeyListener(numberOnly);
		textHeight.addKeyListener(numberOnly);
		textScale.addKeyListener(numberOnly);
		textThreads.addKeyListener(numberOnly);
		
		buttonStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onButtonPressed();
			}
		});
		
		KeyListener enterListener=new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER&&buttonStart.isEnabled()){
					onButtonPressed();
				}
			}
		};
		
		textWidth.addKeyListener(enterListener);
		textHeight.addKeyListener(enterListener);
		textScale.addKeyListener(enterListener);
		textThreads.addKeyListener(enterListener);
		textFilePath.addKeyListener(enterListener);
		
		starter=new Runnable() {
			
			@Override
			public void run() {
				try{
					setAllEnabled(false, SettingPanel.this);
					start();
				}finally{
					setAllEnabled(true, SettingPanel.this);
				}
			}
		};
	}

	private void updateGameModeStatus() {
		boolean isNewGame=buttonNewGame.isSelected();
		boolean isLoadGame=buttonLoadGame.isSelected();

		labelWidth.setEnabled(isNewGame);
		labelHeight.setEnabled(isNewGame);
		textWidth.setEnabled(isNewGame);
		textHeight.setEnabled(isNewGame);
		chckbxRandom.setEnabled(isNewGame);
			
		textFilePath.setEnabled(isLoadGame);
		buttonOpenDialog.setEnabled(isLoadGame);
		
		if (isNewGame){
			numberOnly.addListen(textWidth);
			numberOnly.addListen(textHeight);
		}else{
			numberOnly.removeListen(textWidth);
			numberOnly.removeListen(textHeight);
		}
		numberOnly.update();
	}
	
	private void onButtonPressed(){
		new Thread(starter).start();
	}
	
	private void start(){
		int threads=Integer.parseInt(textThreads.getText());
		double scale=Double.parseDouble(textScale.getText());
		LifeGameEngine engine=null;
		
		if (buttonNewGame.isSelected()){
			int width=Integer.parseInt(textWidth.getText());
			int height=Integer.parseInt(textHeight.getText());
			engine=new LifeGameEngine(new LifeGameEngineConfiguration(width, height, threads));
		}else{
			File file=new File(textFilePath.getText());
			
			TagInputStream in=null;
			try {
				in=new TagInputStream(new BufferedInputStream(new FileInputStream(file)));
				engine=LifeGameEngine.readFromNBT(in.readTag(), threads);
			} catch (IOException e1) {
				ExceptionUtil.showExceptionDialog(e1, Thread.currentThread(), "An I/O exception occurred when read data.");
			} finally {
				if (in!=null){
					try {
						in.close();
					} catch (IOException e1) {
						ExceptionUtil.showExceptionDialog(e1, Thread.currentThread(), "An I/O exception occurred when close stream.");
					}
				}
			}
		}
		
		if (engine!=null){
			LifeGameWindow window=new LifeGameWindow(scale, engine);
			poseDataProcessEvent(new DataProcessEvent(this, window));
			window.setVisible(true);
		}
	}
	
	private void setAllEnabled(boolean e,Container con){
		Component[] coms=con.getComponents();
		for (int i = 0; i < coms.length; i++) {
			Component com = coms[i];
			com.setEnabled(e);
			if (com instanceof Container){
				setAllEnabled(e, (Container) com);
			}
		}
	}
}
