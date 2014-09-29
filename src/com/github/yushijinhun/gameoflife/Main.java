package com.github.yushijinhun.gameoflife;

import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.swing.JOptionPane;
import com.github.yushijinhun.nbt4j.io.TagInputStream;

public final class Main {
	
	private static int width=160;
	private static int height=120;
	private static boolean random;
	private static double blockSize=4;
	private static LifeGameWindow window;
	private static boolean read=false;
	private static String filePath=null;
	
	public static void main(final String[] args) {
		new Thread(new ThreadGroup("lifegame"){
			
			public void uncaughtException(Thread t, Throwable e){
				super.uncaughtException(t, e);
				
				if (window!=null){
					window.gui.shutdown();
					window=null;
					System.gc();
				}
				
				StringBuilder sb=new StringBuilder();
				sb.append("Game of Life has crashed!\n\nException in thread \""+t.getName()+"\" ");
				
				CharArrayWriter writer=new CharArrayWriter();
				e.printStackTrace(new PrintWriter(writer));
				sb.append(writer.toCharArray());
				writer.close();
				
				JOptionPane.showMessageDialog(null, sb.toString().replaceAll("\t", "    "), "Game of Life", JOptionPane.ERROR_MESSAGE);
				
				interrupt();
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
		window=new LifeGameWindow(blockSize,createEngine());
		
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
		if (read){
			TagInputStream in=null;
			try{
				in=new TagInputStream(new FileInputStream(filePath));
				return LifeGameEngine.readFromNBT(in.readTag());
			}catch(IOException e){
				throw new IOError(e);
			}finally{
				if (in!=null){
					try {
						in.close();
					} catch (IOException e) {
						throw new IOError(e);
					}
				}
			}
		}else{
			return new LifeGameEngine(new LifeGameEngineConfiguration(width, height));
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
					
				case "-blockSize":
					blockSize=Double.parseDouble(args[i+1]);
					i+=1;
					break;
					
				case "-read":
					read=true;
					filePath=args[i+1];
					i+=1;
					break;
			}
		}
	}
	
	private Main(){
		
	}
}
