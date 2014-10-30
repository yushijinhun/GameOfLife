package com.github.yushijinhun.gameoflife.gui;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import javax.swing.JButton;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ScalePanel extends DataInputPanel {

	private static final long serialVersionUID = 1L;
	
	private final LifeGameWindow window;
	private JTextField textScale;

	public ScalePanel(LifeGameWindow window) {
		super();
		this.window = window;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel labelScale = new JLabel("Scale:");
		GridBagConstraints gbc_labelScale = new GridBagConstraints();
		gbc_labelScale.insets = new Insets(0, 0, 5, 5);
		gbc_labelScale.anchor = GridBagConstraints.EAST;
		gbc_labelScale.gridx = 0;
		gbc_labelScale.gridy = 0;
		add(labelScale, gbc_labelScale);
		
		textScale = new JTextField();
		GridBagConstraints gbc_textScale = new GridBagConstraints();
		gbc_textScale.insets = new Insets(0, 0, 5, 0);
		gbc_textScale.fill = GridBagConstraints.HORIZONTAL;
		gbc_textScale.gridx = 1;
		gbc_textScale.gridy = 0;
		add(textScale, gbc_textScale);
		textScale.setColumns(10);
		
		JButton buttonSet = new JButton("Set");
		buttonSet.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				onButtonPressed();
			}
		});
		GridBagConstraints gbc_buttonSet = new GridBagConstraints();
		gbc_buttonSet.anchor = GridBagConstraints.EAST;
		gbc_buttonSet.gridx = 1;
		gbc_buttonSet.gridy = 1;
		add(buttonSet, gbc_buttonSet);
	}
	
	private void onButtonPressed(){
		DataProcessEvent event=new DataProcessEvent(this,window);
		poseDataProcessEvent(event);
		
		double scale=Double.parseDouble(textScale.getText());
		if (scale>LifeGameGui.MAX_SCALE){
			scale=LifeGameGui.MAX_SCALE;
		}
		
		window.gui.setScale(scale);
	}
}
