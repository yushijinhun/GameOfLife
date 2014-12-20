package com.github.yushijinhun.gameoflife.gui.event;

import java.util.EventListener;

public interface DataProcessListener extends EventListener {

	void process(DataProcessEvent e);
}
