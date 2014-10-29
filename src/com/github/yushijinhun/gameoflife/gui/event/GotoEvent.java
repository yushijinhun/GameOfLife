package com.github.yushijinhun.gameoflife.gui.event;

import java.util.EventObject;
import com.github.yushijinhun.gameoflife.gui.LifeGameWindow;

public class GotoEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private final LifeGameWindow window;
	
	public GotoEvent(Object source,LifeGameWindow window) {
		super(source);
		this.window=window;
	}

	public LifeGameWindow getWindow() {
		return window;
	}
	
}
