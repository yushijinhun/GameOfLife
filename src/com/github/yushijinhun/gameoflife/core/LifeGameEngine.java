package com.github.yushijinhun.gameoflife.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.github.yushijinhun.nbt4j.tags.NbtTagBooleanArray;
import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameEngine {
	
	public static LifeGameEngine readFromNBT(NbtTagCompound comp,int threads){
		LifeGameEngine game=new LifeGameEngine(new LifeGameEngineConfiguration(comp.getInt("width"), comp.getInt("height"),threads));
		game.ticks=comp.getLong("ticks");
		for (int x=0;x<game.width;x++){
			game.lifes[x]=((NbtTagBooleanArray)comp.get("xline-"+x)).value;
		}
		return game;
	}
	
	public static LifeGameEngine readFromNBT(NbtTagCompound comp){
		return readFromNBT(comp,LifeGameEngineConfiguration.DEFAULT_THREADS);
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
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			for (int i=xstart;i<xend;i++){
				System.arraycopy(lifes[i], 0, bufferLifes[i], 0, height);
				for (int l=0;l<height;l++){
					int nearby=getAroundLifes(i,l);
					
					if ((nearby<2||nearby>3)&&lifes[i][l]){
						bufferLifes[i][l]=false;
						changed(i, l,false);
					}else if(nearby==3&&!lifes[i][l]){
						bufferLifes[i][l]=true;
						changed(i,l,true);
					}
				}
			}
			finish=true;
		}
		
	}
	
	public final int width;
	public final int height;
	
	private final int threads;
	private final boolean singleThread;
	private LifeGameCellHandler cellHandler;
	private long ticks=0;
	private boolean[][] lifes;
	private boolean[][] bufferLifes;
	private ExecutorService threadPool;
	private ComputingUnit[] computingUnits;
	
	public LifeGameEngine(LifeGameEngineConfiguration config) {
		this.width=config.width;
		this.height=config.height;
		threads=config.threads;
		singleThread=threads==1;
		if (!singleThread){
			threadPool=Executors.newFixedThreadPool(threads);
		}
		
		lifes=new boolean[width][];
		for (int i=0;i<width;i++){
			lifes[i]=new boolean[height];
			for (int l=0;l<height;l++){
				lifes[i][l]=false;
			}
		}
		
		bufferLifes=new boolean[width][];
		for (int i=0;i<width;i++){
			bufferLifes[i]=new boolean[height];
		}
		
		if (!singleThread){
			computingUnits=new ComputingUnit[threads];
			int rowsEveryUnit=width/threads;
			for (int i=0;i<threads;i++){
				if (i==threads-1){
					computingUnits[i]=new ComputingUnit(i*rowsEveryUnit, width);
				}else{
					computingUnits[i]=new ComputingUnit(i*rowsEveryUnit, rowsEveryUnit*(i+1));
				}
			}
		}
	}
	
	public boolean get(int x,int y){
		return lifes[x][y];
	}
	
	public void set(int x,int y,boolean value){
		lifes[x][y]=value;
		changed(x,y,value);
	}
	
	private void changed(int x, int y,boolean to) {
		if (cellHandler!=null){
			cellHandler.onChanged(this, x, y, to);
		}
	}
	
	public void nextFrame(){
		if (singleThread){
			for (int i=0;i<width;i++){
				System.arraycopy(lifes[i], 0, bufferLifes[i], 0, height);
				for (int l=0;l<height;l++){
					int nearby=getAroundLifes(i,l);
					
					if ((nearby<2||nearby>3)&&lifes[i][l]){
						bufferLifes[i][l]=false;
						changed(i, l,false);
					}else if(nearby==3&&!lifes[i][l]){
						bufferLifes[i][l]=true;
						changed(i,l,true);
					}
				}
			}
		}else{
			for (int i=0;i<threads;i++){
				computingUnits[i].finish=false;
				threadPool.execute(computingUnits[i]);
			}
			
			for (int i=0;i<threads;i++){
				while(!computingUnits[i].finish){
					Thread.yield();
				}
			}
		}
		
		boolean[][] usedBufferLifes=lifes;
		lifes=bufferLifes;
		bufferLifes=usedBufferLifes;
		
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
		
		return lifes[x][y];
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
		if (!singleThread){
			threadPool.shutdown();
			threadPool=null;
			computingUnits=null;
			bufferLifes=null;
			cellHandler=null;
		}
	}

	public LifeGameCellHandler getCellHandler() {
		return cellHandler;
	}

	public void setCellHandler(LifeGameCellHandler cellHandler) {
		this.cellHandler = cellHandler;
	}
	
	public void resendAllChangeEvent(){
		for (int x=0;x<width;x++){
			for (int y=0;y<height;y++){
				changed(x, y, lifes[x][y]);
			}
		}
	}
}
