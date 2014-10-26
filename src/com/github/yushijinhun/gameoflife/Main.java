package com.github.yushijinhun.gameoflife;

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Random;
import com.github.yushijinhun.gameoflife.core.LifeGameEngine;
import com.github.yushijinhun.gameoflife.core.LifeGameEngineConfiguration;
import com.github.yushijinhun.gameoflife.gui.LifeGameWindow;
import com.github.yushijinhun.gameoflife.util.ExceptionUtil;
import com.github.yushijinhun.nbt4j.io.TagInputStream;

public final class Main {
	
	private static int width=160;
	private static int height=120;
	private static boolean random;
	private static double scale=4;
	private static LifeGameWindow window;
	private static String filePath=null;
	private static int threads=LifeGameEngineConfiguration.DEFAULT_THREADS;
	
	public static void main(final String[] args) {
		new Thread(new ThreadGroup("lifegame"){
			
			public void uncaughtException(Thread t, Throwable e){
				super.uncaughtException(t, e);
				
				if (window!=null){
					window.gui.shutdown();
					window=null;
					System.gc();
				}
				
				interrupt();
				ExceptionUtil.showExceptionDialog(e, t, "Game Of Life has crashed!\n");
			}
			
		}, new Runnable() {
			
			@Override
			public void run() {
				main0(args);
			}
		},"lifegame-main").start();
	}
	
	private static void main0(String[] args) {
		readArgs(args);
		window=new LifeGameWindow(scale,createEngine());
		
		if (random){
			LifeGameEngine engine=window.gui.engine;
			
			Random ran=new Random();
			synchronized (engine) {
				for (int i=0;i<engine.width;i++){
					for (int l=0;l<engine.height;l++){
						engine.set(i, l, ran.nextBoolean());
					}
				}
			}
		}
	}
	
	private static LifeGameEngine createEngine(){
		if (filePath!=null){
			TagInputStream in=null;
			try{
				in=new TagInputStream(new FileInputStream(filePath));
				return LifeGameEngine.readFromNBT(in.readTag(),threads);
			}catch(IOException e){
				ExceptionUtil.showExceptionDialog(e, Thread.currentThread(), "When loading the game, an IOException occurred.\n");
				throw new IOError(e);
			}finally{
				if (in!=null){
					try {
						in.close();
					} catch (IOException e) {
						ExceptionUtil.showExceptionDialog(e, Thread.currentThread(), "When loading the game, an IOException occurred.\n");
						throw new IOError(e);
					}
				}
			}
		}else{
			return new LifeGameEngine(new LifeGameEngineConfiguration(width, height, threads));
		}
	}
	
	private static void readArgs(String[] args){
		for (int i=0;i<args.length;i++){
			switch (args[i]){
				case "-size":
					width=Integer.parseInt(args[i+1]);
					height=Integer.parseInt(args[i+2]);
					i+=2;
					break;
					
				case "-random":
					random=true;
					break;
					
				case "-scale":
					scale=Double.parseDouble(args[i+1]);
					i+=1;
					break;
					
				case "-read":
					filePath=args[i+1];
					i+=1;
					break;
					
				case "-threads":
					threads=Integer.valueOf(args[i+1]);
					i+=1;
					break;
			}
		}
	}
	
	private Main(){
		
	}
}
