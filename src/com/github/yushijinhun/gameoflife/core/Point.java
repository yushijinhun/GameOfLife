package com.github.yushijinhun.gameoflife.core;

import java.math.BigInteger;

public class Point {

	public BigInteger x;
	public BigInteger y;
	
	public Point(BigInteger x, BigInteger y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode(){
		int hash=17;
		hash=31*hash+x.hashCode();
		hash=31*hash+y.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj==this) {
			return true;
		}
		
		if (obj instanceof Point){
			Point another=(Point) obj;
			return another.x.equals(x)&&another.y.equals(y);
		}
		
		return false;
	}
}
