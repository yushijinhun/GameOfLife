package com.github.yushijinhun.gameoflife.gui.event;

import java.util.EventObject;
import com.github.yushijinhun.gameoflife.gui.LifeGameWindow;

public class DataProcessEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private final LifeGameWindow gameWindow;

	public DataProcessEvent(Object source, LifeGameWindow gameWindow) {
		super(source);
		this.gameWindow = gameWindow;
	}

	public LifeGameWindow getGameWindow() {
		return gameWindow;
	}
}
