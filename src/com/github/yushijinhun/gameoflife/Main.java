package com.github.yushijinhun.gameoflife;

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Random;
import com.github.yushijinhun.gameoflife.core.LifeGameEngine;
import com.github.yushijinhun.gameoflife.core.LifeGameEngineConfiguration;
import com.github.yushijinhun.gameoflife.gui.LifeGameWindow;
import com.github.yushijinhun.gameoflife.gui.SettingWindow;
import com.github.yushijinhun.gameoflife.util.ExceptionUtil;
import com.github.yushijinhun.gameoflife.util.LifeGameThreadGroup;
import com.github.yushijinhun.nbt4j.io.TagInputStream;

public final class Main {
	
	public static final String name="Game Of Life";
	public static final String version="v0.3-pre1";
	public static final String github="https://www.github.com/yushijinhun/GameOfLife";
	public static final String by="yushijinhun";
	
	public static LifeGameThreadGroup threadGroup;
	
	private static int width=160;
	private static int height=120;
	private static boolean random;
	private static double scale=4;
	private static String filePath=null;
	private static int threads=LifeGameEngineConfiguration.DEFAULT_THREADS;
	
	public static void main(final String[] args) {
		threadGroup=new LifeGameThreadGroup("lifegame");
		
		new Thread(threadGroup,new Runnable() {
			
			@Override
			public void run() {
				if (args.length==0){
					startFromGUI();
				}else{
					startFromCommandline(args);
				}
			}
		},"lifegame-main").start();
	}
	
	private static void startFromCommandline(String[] args) {
		readArgs(args);
		LifeGameWindow window=new LifeGameWindow(scale,createEngine());
		threadGroup.setWindow(window);
		
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
	
	private static void startFromGUI(){
		new SettingWindow().setVisible(true);
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
