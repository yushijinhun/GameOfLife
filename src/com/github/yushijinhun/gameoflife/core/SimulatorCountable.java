package com.github.yushijinhun.gameoflife.core;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SimulatorCountable implements LifeGameSimulator {

	@Override
	public LifeGameData nextFrame(LifeGameData data,CellChangingHandler changingHandler) {
		if (!(data instanceof LifeGameDataCountable)){
			throw new IllegalArgumentException("SimulatorCountable only accepts LifeGameDataCountable");
		}
		
		LifeGameData newData=data.clone();
		
		Set<Point> mayChange=new HashSet<>();
		Iterator<Point> lifes=((LifeGameDataCountable)data).getLifes();
		while (lifes.hasNext()) {
			Point point = lifes.next();
			mayChange.add(new Point(point.x.subtract(BigInteger.ONE), point.y.subtract(BigInteger.ONE)));
			mayChange.add(new Point(point.x.subtract(BigInteger.ONE), point.y));
			mayChange.add(new Point(point.x.subtract(BigInteger.ONE), point.y.add(BigInteger.ONE)));
			mayChange.add(new Point(point.x, point.y.subtract(BigInteger.ONE)));
			mayChange.add(new Point(point.x, point.y));
			mayChange.add(new Point(point.x, point.y.add(BigInteger.ONE)));
			mayChange.add(new Point(point.x.add(BigInteger.ONE), point.y.subtract(BigInteger.ONE)));
			mayChange.add(new Point(point.x.add(BigInteger.ONE), point.y));
			mayChange.add(new Point(point.x.add(BigInteger.ONE), point.y.add(BigInteger.ONE)));
		}
		
		Iterator<Point> mayChanges=mayChange.iterator();
		while (mayChanges.hasNext()) {
			Point point = mayChanges.next();
			boolean isLiving=data.isCellLiving(point.x, point.y);
			
			int near=0;
			near+=data.isCellLiving(point.x.subtract(BigInteger.ONE), point.y.subtract(BigInteger.ONE))?1:0;
			near+=data.isCellLiving(point.x.subtract(BigInteger.ONE), point.y)?1:0;
			near+=data.isCellLiving(point.x.subtract(BigInteger.ONE), point.y.add(BigInteger.ONE))?1:0;
			near+=data.isCellLiving(point.x, point.y.subtract(BigInteger.ONE))?1:0;
			near+=data.isCellLiving(point.x, point.y.add(BigInteger.ONE))?1:0;
			near+=data.isCellLiving(point.x.add(BigInteger.ONE), point.y.subtract(BigInteger.ONE))?1:0;
			near+=data.isCellLiving(point.x.add(BigInteger.ONE), point.y)?1:0;
			near+=data.isCellLiving(point.x.add(BigInteger.ONE), point.y.add(BigInteger.ONE))?1:0;
			
			if (near<2){
				if (isLiving){
					if (changingHandler!=null){
						changingHandler.onChanging(point.x, point.y, true, false);
					}
					newData.setCellLiving(point.x, point.y, false);
				}
			}else if (near==3){
				if (!isLiving){
					if (changingHandler!=null){
						changingHandler.onChanging(point.x, point.y, false, true);
					}
					newData.setCellLiving(point.x, point.y, true);
				}
			}else if (near>3){
				if (isLiving){
					if (changingHandler!=null){
						changingHandler.onChanging(point.x, point.y, true, false);
					}
					newData.setCellLiving(point.x, point.y, false);
				}
			}
		}
		
		return newData;
	}

}
