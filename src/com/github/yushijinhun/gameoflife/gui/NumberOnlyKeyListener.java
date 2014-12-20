package com.github.yushijinhun.gameoflife.gui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.text.JTextComponent;

public class NumberOnlyKeyListener extends KeyAdapter {

	private static final char[] availableChars = new char[] { '0', '1', '2',
		'3', '4', '5', '6', '7', '8', '9', 8, 127 };

	private Set<Component> controlComponents = new HashSet<Component>();
	private Set<JTextComponent> listenComponents = new HashSet<JTextComponent>();

	public void addControl(Component comp) {
		if (comp == null) {
			throw new NullPointerException("comp cannot be null");
		}
		controlComponents.add(comp);
	}

	public void addListen(JTextComponent comp) {
		if (comp == null) {
			throw new NullPointerException("comp cannot be null");
		}
		listenComponents.add(comp);
	}

	public void removeControl(Component comp) {
		controlComponents.remove(comp);
	}

	public void removeListen(JTextComponent comp) {
		listenComponents.remove(comp);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char theChar = e.getKeyChar();

		boolean available = false;
		for (int i = 0; i < availableChars.length; i++) {
			if (theChar == availableChars[i]) {
				available = true;
				break;
			}
		}

		if (!available || e.isAltDown() || e.isControlDown()) {
			e.consume();
			return;
		}

		boolean flag = true;
		Iterator<JTextComponent> it1 = listenComponents.iterator();
		while (it1.hasNext()) {
			JTextComponent text = it1.next();
			if (!((theChar == 8) || (theChar == 127))
					&& (text == e.getComponent())) {
				continue;
			}

			if (text.getText().length() == 0) {
				flag = false;
				break;
			}
		}

		Iterator<Component> it2 = controlComponents.iterator();
		while (it2.hasNext()) {
			Component comp = it2.next();
			comp.setEnabled(flag);
		}
	}

	public void update() {
		boolean flag = true;
		Iterator<JTextComponent> it1 = listenComponents.iterator();
		while (it1.hasNext()) {
			JTextComponent text = it1.next();
			if (text.getText().length() == 0) {
				flag = false;
				break;
			}
		}

		Iterator<Component> it2 = controlComponents.iterator();
		while (it2.hasNext()) {
			Component comp = it2.next();
			comp.setEnabled(flag);
		}
	}
}
