package com.github.yushijinhun.gameoflife.core;

import java.math.BigInteger;

public interface LifeGameData {

	boolean isCellLiving(BigInteger x,BigInteger y);
	
	void setCellLiving(BigInteger x,BigInteger y,boolean living);
}
