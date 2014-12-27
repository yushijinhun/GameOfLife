package com.github.yushijinhun.gameoflife.core;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class DataLifesSet implements LifeGameData {
	
	protected Set<Point> lifes;
	
	public DataLifesSet() {
		lifes=new HashSet<>();
	}

	@Override
	public boolean isCellLiving(BigInteger x, BigInteger y) {
		return lifes.contains(new Point(x,y));
	}

	@Override
	public void setCellLiving(BigInteger x, BigInteger y, boolean living) {
		if (living){
			lifes.add(new Point(x,y));
		}else{
			lifes.remove(new Point(x,y));
		}
	}

}
