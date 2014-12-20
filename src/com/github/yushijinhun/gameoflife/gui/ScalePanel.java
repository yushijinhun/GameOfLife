package com.github.yushijinhun.gameoflife.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;

public class ScalePanel extends DataInputPanel {

	private static final long serialVersionUID = 1L;

	private final LifeGameWindow window;
	private final JTextField textScale;
	private final JLabel labelScale;

	private final JButton buttonSet;

	public ScalePanel(LifeGameWindow window) {
		super();
		this.window = window;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		labelScale = new JLabel("Scale:");
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

		buttonSet = new JButton("Set");
		buttonSet.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onButtonPressed();
			}
		});
		GridBagConstraints gbc_buttonSet = new GridBagConstraints();
		gbc_buttonSet.anchor = GridBagConstraints.EAST;
		gbc_buttonSet.gridx = 1;
		gbc_buttonSet.gridy = 1;
		add(buttonSet, gbc_buttonSet);

		NumberOnlyKeyListener numberOnly = new NumberOnlyKeyListener();
		numberOnly.addControl(buttonSet);
		numberOnly.addListen(textScale);
		textScale.addKeyListener(numberOnly);

		KeyListener enterListener = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER)
						&& buttonSet.isEnabled()) {
					onButtonPressed();
				}
			}
		};
		textScale.addKeyListener(enterListener);

		numberOnly.update();
	}

	private void onButtonPressed() {
		DataProcessEvent event = new DataProcessEvent(this, window);
		poseDataProcessEvent(event);

		double scale = Double.parseDouble(textScale.getText());
		if (scale > LifeGameGui.MAX_SCALE) {
			scale = LifeGameGui.MAX_SCALE;
		}

		window.gui.setScale(scale);
	}
}
