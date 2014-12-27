package com.github.yushijinhun.gameoflife.core;

import java.math.BigInteger;

public interface LifeGameData extends Cloneable {

	boolean isCellLiving(BigInteger x,BigInteger y);
	
	void setCellLiving(BigInteger x,BigInteger y,boolean living);
	
	LifeGameData clone();
}
