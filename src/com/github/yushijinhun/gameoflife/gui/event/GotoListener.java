package com.github.yushijinhun.gameoflife.gui.event;

import java.util.EventListener;

public interface GotoListener extends EventListener {
	
	void onGoto(GotoEvent e);
}
