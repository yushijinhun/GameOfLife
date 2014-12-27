package com.github.yushijinhun.gameoflife.core;

import java.math.BigInteger;

public interface CellChangingHandler {

	void onChanging(BigInteger x,BigInteger y,boolean from,boolean to);

}
