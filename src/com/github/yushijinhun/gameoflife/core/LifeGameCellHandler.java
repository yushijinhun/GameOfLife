package com.github.yushijinhun.gameoflife.core;

public interface LifeGameCellHandler {

	void onChanged(LifeGameEngine engine, int x, int y, boolean to);
}
