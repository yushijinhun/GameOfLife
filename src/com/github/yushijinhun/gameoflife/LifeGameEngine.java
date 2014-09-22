package com.github.yushijinhun.gameoflife;

import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameEngine {
	
	public static LifeGameEngine readFromNBT(NbtTagCompound comp){
		LifeGameEngine game=new LifeGameEngine(comp.getInt("width"), comp.getInt("height"));
		game.ticks=comp.getLong("ticks");
		for (int x=0;x<game.width;x++){
			for (int y=0;y<game.height;y++){
				game.lifes[x][y]=comp.getBoolean("lifes-"+x+","+y);
			}
		}
		return game;
	}
	
	public final int width;
	public final int height;
	public int[] changedXPos;
	public int[] changedYPos;
	public int changedPosHead;
	
	private int cells;
	private long ticks=0;
	private boolean[][] lifes;
	private boolean[][] bufferLifes;
	
	public LifeGameEngine(int width,int height) {
		this.width=width;
		this.height=height;
		cells=width*height;
		changedXPos=new int[cells];
		changedYPos=new int[cells];
		changedPosHead=0;
		
		lifes=new boolean[width][];
		for (int i=0;i<width;i++){
			lifes[i]=new boolean[height];
			for (int l=0;l<height;l++){
				set(i, l, false);
			}
		}
		
		bufferLifes=new boolean[width][];
		for (int i=0;i<width;i++){
			bufferLifes[i]=new boolean[height];
		}
	}
	
	public boolean get(int x,int y){
		return lifes[x][y];
	}
	
	public void set(int x,int y,boolean value){
		lifes[x][y]=value;
		changed(x,y);
	}
	
	public void changed(int x,int y){
		if (changedPosHead>=changedXPos.length){
			int[] changedXPos_=new int[(int) (changedXPos.length*1.5)];
			int[] changedYPos_=new int[(int) (changedYPos.length*1.5)];
			System.arraycopy(changedXPos, 0, changedXPos_, 0, changedPosHead);
			System.arraycopy(changedYPos, 0, changedYPos_, 0, changedPosHead);
			changedXPos=changedXPos_;
			changedYPos=changedYPos_;
		}
		
		changedXPos[changedPosHead]=x;
		changedYPos[changedPosHead]=y;
		changedPosHead++;
	}
	
	public void nextFrame(){
		for (int i=0;i<width;i++){
			System.arraycopy(lifes[i], 0, bufferLifes[i], 0, height);
			for (int l=0;l<height;l++){
				int nearby=getAroundLifes(i,l);
				
				if ((nearby<2||nearby>3)&&lifes[i][l]){
					bufferLifes[i][l]=false;
					changed(i, l);
				}else if(nearby==3&&!lifes[i][l]){
					bufferLifes[i][l]=true;
					changed(i,l);
				}
			}
		}
		
		boolean[][] bufferLifes_=lifes;
		lifes=bufferLifes;
		bufferLifes=bufferLifes_;
		
		ticks++;
	}
	
	public int getAroundLifes(int x,int y){
		int num=0;
		num+=safeGet(x-1,y-1)?1:0;
		num+=safeGet(x-1,y)?1:0;
		num+=safeGet(x-1,y+1)?1:0;
		num+=safeGet(x,y-1)?1:0;
		num+=safeGet(x,y+1)?1:0;
		num+=safeGet(x+1,y-1)?1:0;
		num+=safeGet(x+1,y)?1:0;
		num+=safeGet(x+1,y+1)?1:0;
		return num;
	}
	
	private boolean safeGet(int x,int y){
		if (x>=width||y>=height||x<0||y<0){
			return false;
		}
		
		return lifes[x%width][y%height];
	}
	
	public long getTicks() {
		return ticks;
	}
	
	public void writeToNBT(NbtTagCompound comp){
		comp.setInt("width", width);
		comp.setInt("height", height);
		comp.setLong("ticks", ticks);
		for (int x=0;x<width;x++){
			for (int y=0;y<height;y++){
				comp.setBoolaen("lifes-"+x+","+y, lifes[x][y]);
			}
		}
	}
}
