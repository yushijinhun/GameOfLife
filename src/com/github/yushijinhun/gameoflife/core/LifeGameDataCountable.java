package com.github.yushijinhun.gameoflife.core;

import java.util.Iterator;

public interface LifeGameDataCountable extends LifeGameData {

	Iterator<Point> getLifes();

	@Override
	LifeGameDataCountable clone();
}
