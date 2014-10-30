package com.github.yushijinhun.gameoflife.gui;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JButton;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GotoPanel extends DataInputPanel {

	private static final long serialVersionUID = 1L;
	
	private final JTextField textX;
	private final JTextField textY;
	private final JLabel lableX;
	private final JLabel lableY;
	private final JButton buttonGoto;
	private final LifeGameWindow window;
	
	public GotoPanel(LifeGameWindow window) {
		this.window=window;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		lableX = new JLabel("X pos:");
		GridBagConstraints gbc_lableX = new GridBagConstraints();
		gbc_lableX.anchor = GridBagConstraints.EAST;
		gbc_lableX.insets = new Insets(0, 0, 5, 5);
		gbc_lableX.gridx = 0;
		gbc_lableX.gridy = 0;
		add(lableX, gbc_lableX);
		
		textX = new JTextField();
		GridBagConstraints gbc_textX = new GridBagConstraints();
		gbc_textX.insets = new Insets(0, 0, 5, 0);
		gbc_textX.fill = GridBagConstraints.HORIZONTAL;
		gbc_textX.gridx = 1;
		gbc_textX.gridy = 0;
		add(textX, gbc_textX);
		textX.setColumns(10);
		
		lableY = new JLabel("Y pos:");
		GridBagConstraints gbc_lableY = new GridBagConstraints();
		gbc_lableY.anchor = GridBagConstraints.EAST;
		gbc_lableY.insets = new Insets(0, 0, 5, 5);
		gbc_lableY.gridx = 0;
		gbc_lableY.gridy = 1;
		add(lableY, gbc_lableY);
		
		textY = new JTextField();
		GridBagConstraints gbc_textY = new GridBagConstraints();
		gbc_textY.insets = new Insets(0, 0, 5, 0);
		gbc_textY.fill = GridBagConstraints.HORIZONTAL;
		gbc_textY.gridx = 1;
		gbc_textY.gridy = 1;
		add(textY, gbc_textY);
		textY.setColumns(10);
		
		buttonGoto = new JButton("Goto");
		buttonGoto.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				onButtonPressed();
			}
		});
		GridBagConstraints gbc_buttonGoto = new GridBagConstraints();
		gbc_buttonGoto.insets = new Insets(0, 0, 5, 0);
		gbc_buttonGoto.anchor = GridBagConstraints.EAST;
		gbc_buttonGoto.gridx = 1;
		gbc_buttonGoto.gridy = 2;
		add(buttonGoto, gbc_buttonGoto);
		
		NumberOnlyKeyListener numberOnly=new NumberOnlyKeyListener();
		numberOnly.addControl(buttonGoto);
		numberOnly.addListen(textX);
		numberOnly.addListen(textY);
		
		textX.addKeyListener(numberOnly);
		textY.addKeyListener(numberOnly);
		
		KeyListener enterListener=new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER&&buttonGoto.isEnabled()){
					onButtonPressed();
				}
			}
		};
		textX.addKeyListener(enterListener);
		textY.addKeyListener(enterListener);
		
		numberOnly.update();
	}
	
	private void onButtonPressed(){
		poseDataProcessEvent(new DataProcessEvent(this, GotoPanel.this.window));
		int x=Integer.parseInt(textX.getText());
		int y=Integer.parseInt(textY.getText());
		GotoPanel.this.window.gui.gotoPos(x, y);
	}
}
