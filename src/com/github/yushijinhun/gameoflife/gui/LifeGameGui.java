package com.github.yushijinhun.gameoflife.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.github.yushijinhun.gameoflife.Main;
import com.github.yushijinhun.gameoflife.core.LifeGameCellHandler;
import com.github.yushijinhun.gameoflife.core.LifeGameEngine;
import com.github.yushijinhun.gameoflife.util.ExceptionUtil;
import com.github.yushijinhun.nbt4j.io.TagOutputStream;
import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameGui extends Canvas{
	
	public static final double SCALE_FACTOR = 15d/16d;
	
	public static final double MAX_SCALE=256d;
	
	private static final long serialVersionUID = 1L;
	private static final Font font=new Font("Dialog",Font.BOLD,12);
	private static final Font fontBig=new Font("Dialog",Font.BOLD,16);
	private static final int deadColor=Color.DARK_GRAY.getRGB();
	private static final int livingColor=Color.GREEN.getRGB();
	
	private class SetTask implements Runnable{

		private final int x;
		private final int y;
		private final boolean set;

		public SetTask(int x, int y,boolean set) {
			super();
			this.x = x;
			this.y = y;
			this.set=set;
		}

		@Override
		public void run() {			
			synchronized (engine) {
				engine.set(x, y, set);
			}
		}
		
	}
	
	public final LifeGameEngine engine;
	public int fps=0;
	public long lastTickingTime=0;
	public boolean showInfo=true;
	
	private double scale;
	private BufferedImage buffer;
	private BufferedImage cellsBuffer;
	private boolean fistOpen=true;
	private boolean stopped=false;
	private int xOffset=0;
	private int yOffset=0;
	private boolean isDragging=false;
	private int dragBeginX;
	private int dragBeginY;
	private int mouseRelativeX;
	private int mouseRelativeY;
	private int mouseX;
	private int mouseY;
	private final ExecutorService threadPool;
	private boolean isComputing=false;
	private boolean isSaving=false;
	private boolean pressSet;
	private boolean isPressing=false;
	private final SavingThread savingThread=new SavingThread();
	private final JFileChooser chooser = new JFileChooser();
	
	private final Runnable nextFrameComputingUnit=new Runnable() {
		
		@Override
		public void run() {
			isComputing=true;
			long start=System.currentTimeMillis();

			synchronized (engine) {
				engine.nextFrame();
			}
			
			lastTickingTime=System.currentTimeMillis()-start;
			isComputing=false;
		}
	};
	
	private final Thread renderThread=new Thread("Render Thread"){
		
		public void run(){
			while(buffer==null){
				if (getGraphicsConfiguration()!=null){
					Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
					buffer=getGraphicsConfiguration().createCompatibleImage((int)d.getWidth(), (int)d.getHeight());
					cellsBuffer=getGraphicsConfiguration().createCompatibleImage(engine.width, engine.height);
					
					engine.setCellHandler(new LifeGameCellHandler() {
						
						private final Object deadData;
						private final Object livingData;
						private final SampleModel sampleModel;
						private final DataBuffer dataBuffer;
						
						{
							ColorModel colorModel=cellsBuffer.getColorModel();
							deadData=colorModel.getDataElements(deadColor, null);
							livingData=colorModel.getDataElements(livingColor, null);
							sampleModel=cellsBuffer.getRaster().getSampleModel();
							dataBuffer=cellsBuffer.getRaster().getDataBuffer();
						}
						
						@Override
						public void onChanged(LifeGameEngine engine, int x, int y, boolean to) {
							sampleModel.setDataElements(x, y, to?livingData:deadData,dataBuffer);
						}
					});
					
					new Thread("Repaint Thread"){
						public void run(){
							engine.resendAllChangeEvent();
						}
					}.start();
					break;
				}
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					return;
				}
			}
			
			Graphics2D g2d=buffer.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			
			int fps=0;
			long nextSecond=System.currentTimeMillis();
			long lastRenderTimeBegin=0;
			long lastRenderTime=0;
			long sleepTime;
			
			while(!stopped){
				if (nextSecond<=System.currentTimeMillis()){
					nextSecond=System.currentTimeMillis()+1000;
					LifeGameGui.this.fps=fps;
					fps=0;
				}
				
				lastRenderTimeBegin=System.currentTimeMillis();
				
				synchronized (buffer) {
					g2d.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
					render(g2d);
				}
				repaint();
				lastRenderTime=System.currentTimeMillis()-lastRenderTimeBegin;
				
				fps++;
				
				sleepTime=1000/200-lastRenderTime;
				if (sleepTime>0){
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
			
		}
	};
	
	private class SavingThread implements Runnable {
		
		private File file;
		
		public void setFile(File file){
			this.file=file;
		}
		
		@Override
		public void run() {
			NbtTagCompound comp=new NbtTagCompound("root");

			synchronized (engine) {
				engine.writeToNBT(comp);
			}
			
			TagOutputStream out=null;
			try{
				out=new TagOutputStream(new FileOutputStream(file));
				out.writeTag(comp, true);
			}catch(IOException e1){
				ExceptionUtil.showExceptionDialog(e1, Thread.currentThread(), "When saving the game, an IOException occurred.\n");
			}finally{
				if (out!=null){
					try {
						out.close();
					} catch (IOException e1) {
						ExceptionUtil.showExceptionDialog(e1, Thread.currentThread(), "When saving the game, an IOException occurred.\n");
						return;
					}
				}
			}
			
			JOptionPane.showMessageDialog(LifeGameGui.this, "The game saved to "+file.getPath(), "Game of Life", JOptionPane.INFORMATION_MESSAGE);
			isSaving=false;
		}
	};
	
	public LifeGameGui(double blockSize,LifeGameEngine theEngine) {
		scale=blockSize;
		threadPool=Executors.newSingleThreadExecutor();
		engine=theEngine;
		
		if (scale>256){
			scale=256;
		}
		
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Save");
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				fistOpen=false;
				
				switch(e.getButton()){
					case MouseEvent.BUTTON1:
						if (!isPressing){
							isPressing=true;
							int x=(int) (Math.rint(e.getX()-xOffset)/scale);
							int y=(int) (Math.rint(e.getY()-yOffset)/scale);
							pressSet=!engine.get(x, y);
							
							if (x>=engine.width||y>=engine.height||x<0||y<0){
								return;
							}
							
							threadPool.execute(new SetTask(x, y,pressSet));
						}
						
						break;
						
					case MouseEvent.BUTTON3:
						if (!isDragging){
							isDragging=true;
							dragBeginX=e.getX()-xOffset;
							dragBeginY=e.getY()-yOffset;
							break;
						}
						break;
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3){
					isDragging=false;
				}else if (e.getButton()==MouseEvent.BUTTON1){
					isPressing=false;
				}
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			
			public void mouseMoved(MouseEvent e){
				updateMousePos();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				updateMousePos();
				
				if (isDragging){
					xOffset=e.getX()-dragBeginX;
					yOffset=e.getY()-dragBeginY;
				}
				
				if (isPressing){
					final int x=(int) (Math.rint(e.getX()-xOffset)/scale);
					final int y=(int) (Math.rint(e.getY()-yOffset)/scale);
					
					if (x>=engine.width||y>=engine.height||x<0||y<0){
						return;
					}
					
					threadPool.execute(new SetTask(x, y,pressSet));
				}
			}
		});
		
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				fistOpen=false;
				
				switch(e.getKeyCode()){
					case KeyEvent.VK_N:
						if (isComputing){
							break;
						}
						
						threadPool.execute(nextFrameComputingUnit);
						break;
						
					case KeyEvent.VK_F1:
						showInfo=!showInfo;
						break;
						
					case KeyEvent.VK_Z:
						double nextScale1=scale/SCALE_FACTOR;
						if (nextScale1>MAX_SCALE){
							nextScale1=MAX_SCALE;
						}
						
						gotoPos(nextScale1,mouseRelativeX,mouseRelativeY);
						break;
						
					case KeyEvent.VK_X:
						double nextScale2=scale*SCALE_FACTOR;
						gotoPos(nextScale2,mouseRelativeX,mouseRelativeY);
						break;
						
					case KeyEvent.VK_0:
						if (e.isControlDown()){
							gotoPos(1d,mouseRelativeX,mouseRelativeY);
						}
						break;
						
					case KeyEvent.VK_S:
						if (e.isControlDown()&&!isSaving){
							chooser.showSaveDialog(LifeGameGui.this);
							final File file = chooser.getSelectedFile();
							if (file == null) {
								JOptionPane.showMessageDialog(LifeGameGui.this, "You did not select any files.", "Game of Life", JOptionPane.WARNING_MESSAGE);
								return;
							}
							
							isSaving=true;
							savingThread.setFile(file);
							threadPool.execute(savingThread);
						}
						break;
				}
			}
		});
		
		setSize((int) (Math.rint(engine.width*scale)), (int) (Math.rint(engine.height*scale)));
		renderThread.start();
	}
	
	public void paint(Graphics g){
		if (buffer==null){
			return;
		}
		
		synchronized (buffer) {
			g.drawImage(buffer, 0, 0, null);
		}
	}
	
	public void update(Graphics g){
		paint(g);
	}
	
	private void render(Graphics2D g2d){
		g2d.drawImage(cellsBuffer, xOffset, yOffset,(int) (Math.rint(cellsBuffer.getWidth()*scale)),(int) (Math.rint(cellsBuffer.getHeight())*scale), null);
		
		if (showInfo){
			g2d.setFont(font);
			renderString(g2d,"computing threads: "+engine.getThreads(), 0, 60);
			renderString(g2d,"memory: "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024/1024+"m used", 0, 50);
			renderString(g2d,"cell size: "+scale, 0, 40);
			renderString(g2d,"last ticking time: "+(isComputing?"computing...":lastTickingTime), 0, 30);
			renderString(g2d,"ticks: "+engine.getTicks(), 0, 20);
			renderString(g2d,"fps: "+fps, 0, 10);
			renderString(g2d, "("+mouseRelativeX+", "+mouseRelativeY+")", mouseX+10, mouseY+10);
		}
		
		if (fistOpen){
			g2d.setFont(fontBig);
			renderString(g2d,Main.name+" "+Main.version, 80, 80);
			renderString(g2d,"by "+Main.by, 80, 95);
			renderString(g2d,"Press N to show next frame", 80, 120);
			renderString(g2d,"Press Z to enlarge", 80, 135);
			renderString(g2d,"Press X to shrink", 80, 150);
			renderString(g2d,"Press F1 to show/hide information", 80, 165);
			renderString(g2d,"Press Ctrl+S to save the game", 80, 180);
			renderString(g2d,"Press Ctrl+0 to set scale to 1", 80, 195);
			renderString(g2d,"Press Ctrl+G to show goto window", 80, 210);
			renderString(g2d,"Press Ctrl+D to show scale window", 80, 225);
			renderString(g2d,"Click to change one cell's status", 80, 240);
		}
		
	}
	
	private void renderString(Graphics g,String str,int x,int y){
		g.setColor(Color.DARK_GRAY);
		g.drawString(str, x+1, y+1);
		g.setColor(Color.WHITE);
		g.drawString(str, x, y);
	}
	
	public Thread getRenderThread(){
		return renderThread;
	}
	
	public void shutdown(){
		stopped=true;
		threadPool.shutdown();
		engine.shutdown();
	}
	
	public double getCellSize() {
		return scale;
	}
	
	private void gotoPos(double scale,int x,int y){
		this.scale=scale;
		xOffset=(int) (mouseX-x*scale);
		yOffset=(int) (mouseY-y*scale);
	}
	
	public void gotoPos(int x,int y){
		updateMousePos();
		gotoPos(scale,x,y);
		updateMousePos();
	}
	
	public void setScale(double scale){
		Point mouse=getMousePosition();
		if (mouse!=null){
			mouseX=mouse.x;
			mouseY=mouse.y;
		}
		gotoPos(scale,mouseRelativeX,mouseRelativeY);
		updateMousePos();
	}
	
	public void updateMousePos(){
		Point mouse=getMousePosition();
		if (mouse!=null){
			mouseX=mouse.x;
			mouseY=mouse.y;
			mouseRelativeX=(int) (Math.rint(mouseX-xOffset)/scale);
			mouseRelativeY=(int) (Math.rint(mouseY-yOffset)/scale);
		}
	}
}
