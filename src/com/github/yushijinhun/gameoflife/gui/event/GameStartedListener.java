package com.github.yushijinhun.gameoflife.gui.event;

import java.util.EventListener;

public interface GameStartedListener extends EventListener {
	
	void gameStarted(GameStartedEvent e);
}
