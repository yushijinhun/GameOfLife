package com.github.yushijinhun.gameoflife.util;

import com.github.yushijinhun.gameoflife.gui.LifeGameWindow;

public class LifeGameThreadGroup extends ThreadGroup {

	private LifeGameWindow window;

	public LifeGameThreadGroup(String name) {
		super(name);
	}

	public LifeGameThreadGroup(ThreadGroup parent, String name) {
		super(parent, name);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		super.uncaughtException(t, e);

		if (window != null) {
			window.gui.shutdown();
			window = null;
			System.gc();
		}

		interrupt();
		ExceptionUtil.showExceptionDialog(e, t, "Game Of Life has crashed!\n");
		System.exit(1);
	}

	public LifeGameWindow getWindow() {
		return window;
	}

	public void setWindow(LifeGameWindow window) {
		this.window = window;
	}

}
