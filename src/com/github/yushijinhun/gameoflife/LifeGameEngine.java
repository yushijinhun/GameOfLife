package com.github.yushijinhun.gameoflife;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.github.yushijinhun.nbt4j.tags.NbtTagBooleanArray;
import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameEngine {
	
	private static final int THREADS=Runtime.getRuntime().availableProcessors();
	
	public static LifeGameEngine readFromNBT(NbtTagCompound comp){
		LifeGameEngine game=new LifeGameEngine(comp.getInt("width"), comp.getInt("height"));
		game.ticks=comp.getLong("ticks");
		for (int x=0;x<game.width;x++){
			game.lifes[x]=((NbtTagBooleanArray)comp.get("xline-"+x)).value;
		}
		return game;
	}
	
	private class ComputingUnit implements Runnable{
		
		private final int xstart;
		private final int xend;
		
		public boolean finish=false;
		
		public ComputingUnit(int xstart,int xend){
			this.xstart=xstart;
			this.xend=xend;
		}
		
		@Override
		public void run() {
			for (int i=xstart;i<xend;i++){
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
			finish=true;
		}
		
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
	private ExecutorService threadPool=Executors.newFixedThreadPool(THREADS);
	private ComputingUnit[] computingUnits;
	
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
		
		computingUnits=new ComputingUnit[THREADS];
		int rowsEveryUnit=width/THREADS;
		for (int i=0;i<THREADS;i++){
			if (i==THREADS-1){
				computingUnits[i]=new ComputingUnit(i*rowsEveryUnit, width);
			}else{
				computingUnits[i]=new ComputingUnit(i*rowsEveryUnit, rowsEveryUnit*(i+1));
			}
		}
	}
	
	public boolean get(int x,int y){
		return lifes[x][y];
	}
	
	public void set(int x,int y,boolean value){
		lifes[x][y]=value;
		changed(x,y);
	}
	
	public synchronized void changed(int x,int y){
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
		for (int i=0;i<THREADS;i++){
			computingUnits[i].finish=false;
			threadPool.execute(computingUnits[i]);
		}
		
		for (int i=0;i<THREADS;i++){
			while(!computingUnits[i].finish){
				Thread.yield();
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
			comp.add(new NbtTagBooleanArray("xline-"+x, lifes[x]));
		}
	}
	
	public void shutdown(){
		threadPool.shutdown();
	}
}
