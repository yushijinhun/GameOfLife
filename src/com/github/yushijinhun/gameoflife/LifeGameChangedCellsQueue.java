package com.github.yushijinhun.gameoflife;

public class LifeGameChangedCellsQueue {
	
	private static final float DEFAULT_FACTOR=0.75f;
	
	private int[] xPos;
	private int[] yPos;
	private int size;
	private int length;
	private final float factor;
	
	public LifeGameChangedCellsQueue(int length) {
		this(length,DEFAULT_FACTOR);
	}
	
	public LifeGameChangedCellsQueue(int length,float factor) {
		this.factor=factor;
		size=0;
		xPos=new int[length];
		yPos=new int[length];
		this.length=length;
	}
	
	public void add(int x,int y){
		synchronized (this) {
			if (size>=length){
				int[] newXPos=new int[(int) (length/factor)];
				int[] newYPos=new int[(int) (length/factor)];
				System.arraycopy(xPos, 0, newXPos, 0, length);
				System.arraycopy(yPos, 0, newYPos, 0, length);
				xPos=newXPos;
				yPos=newYPos;
			}
			
			xPos[size]=x;
			yPos[size]=y;
			size++;
		}
	}
	
	public int size(){
		return size;
	}
	
	public void clear(){
		synchronized (this) {
			size=0;
		}
	}
	
	public int getXAtIndex(int i){
		return xPos[i];
	}
	
	public int getYAtIndex(int i){
		return yPos[i];
	}
}
