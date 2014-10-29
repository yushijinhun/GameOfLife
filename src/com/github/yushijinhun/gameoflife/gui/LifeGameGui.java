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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.github.yushijinhun.gameoflife.core.LifeGameChangedCellsQueue;
import com.github.yushijinhun.gameoflife.core.LifeGameEngine;
import com.github.yushijinhun.gameoflife.util.ExceptionUtil;
import com.github.yushijinhun.nbt4j.io.TagOutputStream;
import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameGui extends Canvas{
	
	public static final double SCALE_FACTOR = 0.75;
	
	public static final double MAX_SCALE=256d;
	
	private static final long serialVersionUID = 1L;
	private static final Font font=new Font("Dialog",Font.BOLD,12);
	private static final Font fontBig=new Font("Dialog",Font.BOLD,16);
	
	public final LifeGameEngine engine;
	public int fps=0;
	public long lastTickingTime=0;
	public boolean showInfo=true;
	public boolean drawFull;
	
	private double scale;
	private BufferedImage buffer;
	private BufferedImage cellsBuffer;
	private Graphics2D cellsBufferGraphics;
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
		
		class CellsRender implements Runnable{
			
			public boolean rendering=false;
			
			public void run() {
				synchronized (engine) {
					if (drawFull){
						cellsBufferGraphics.setColor(Color.DARK_GRAY);
						cellsBufferGraphics.fillRect(0, 0, cellsBuffer.getWidth(), cellsBuffer.getHeight());
						cellsBufferGraphics.setColor(Color.GREEN);
						for (int x=0;x<engine.width;x++){
							for (int y=0;y<engine.height;y++){
								if (engine.get(x, y)){
									cellsBufferGraphics.fillRect(x, y, 1, 1);
								}
							}
						}
						
						drawFull=false;
					}else{
						cellsBufferGraphics.setColor(Color.GREEN);
						renderChangedCellsQueue(engine.trueQueue);
						cellsBufferGraphics.setColor(Color.DARK_GRAY);
						renderChangedCellsQueue(engine.falseQueue);
					}
				}
				rendering=false;
			}
			
			private void renderChangedCellsQueue(LifeGameChangedCellsQueue q){
				synchronized (q) {
					for (int i=0;i<q.size();i++){
						cellsBufferGraphics.fillRect(q.getXAtIndex(i), q.getYAtIndex(i), 1, 1);
					}
					q.clear();
				}
			}
		};
		
		private final ExecutorService renderThreadPool=Executors.newSingleThreadExecutor();
		private final CellsRender cellsRender=new CellsRender();
		
		public void run(){
			try{
				while(buffer==null){
					if (getGraphicsConfiguration()!=null){
						Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
						buffer=getGraphicsConfiguration().createCompatibleImage((int)d.getWidth(), (int)d.getHeight());
						cellsBuffer=getGraphicsConfiguration().createCompatibleImage(engine.width, engine.height);
						cellsBufferGraphics=cellsBuffer.createGraphics();
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
					if (!cellsRender.rendering){
						cellsRender.rendering=true;
						renderThreadPool.execute(cellsRender);
					}
					
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
			}finally{
				renderThreadPool.shutdown();
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
		drawFull=true;
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
						final int x=(int) (Math.rint(e.getX()-xOffset)/scale);
						final int y=(int) (Math.rint(e.getY()-yOffset)/scale);
						
						if (x>=engine.width||y>=engine.height||x<0||y<0){
							return;
						}
						
						threadPool.execute(new Runnable() {
							
							public void run() {
								synchronized (engine) {
									engine.set(x, y, !engine.get(x, y));
								}
							}
						});
						
						break;
						
					case MouseEvent.BUTTON3:
						if (isDragging==false){
							isDragging=true;
							dragBeginX=e.getX()-xOffset;
							dragBeginY=e.getY()-yOffset;
							break;
						}
						break;
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				if (e.getButton()!=MouseEvent.BUTTON3){
					return;
				}
				
				isDragging=false;
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			
			public void mouseMoved(MouseEvent e){
				updataMousePos(e.getX(), e.getY());
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				updataMousePos(e.getX(), e.getY());
				
				if (!isDragging){
					return;
				}
				
				xOffset=e.getX()-dragBeginX;
				yOffset=e.getY()-dragBeginY;
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
						scale=nextScale1;
						break;
						
					case KeyEvent.VK_X:
						double nextScale2=scale*SCALE_FACTOR;
						gotoPos(nextScale2,mouseRelativeX,mouseRelativeY);
						scale=nextScale2;
						break;
						
					case KeyEvent.VK_0:
						if (e.isControlDown()){
							gotoPos(1d,mouseRelativeX,mouseRelativeY);
							scale=1d;
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
			renderString(g2d,"cell size: "+scale, 0, 40);
			renderString(g2d,"last ticking time: "+(isComputing?"computing...":lastTickingTime), 0, 30);
			renderString(g2d,"ticks: "+engine.getTicks(), 0, 20);
			renderString(g2d,"fps: "+fps, 0, 10);
			renderString(g2d, "("+mouseRelativeX+", "+mouseRelativeY+")", mouseX+10, mouseY+10);
		}
		
		if (fistOpen){
			g2d.setFont(fontBig);
			renderString(g2d,"Game of Life v0.2", 80, 80);
			renderString(g2d,"by yushijinhun", 80, 95);
			renderString(g2d,"Press N to show next frame", 80, 120);
			renderString(g2d,"Press Z to enlarge", 80, 135);
			renderString(g2d,"Press X to shrink", 80, 150);
			renderString(g2d,"Press F1 to show/hide information", 80, 165);
			renderString(g2d,"Press Ctrl+S to save the game", 80, 180);
			renderString(g2d,"Press Ctrl+0 to set scale to 1", 80, 195);
			renderString(g2d,"Click to change one cell's status", 80, 210);
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
	
	public void gotoPos(double scale,double x,double y){
		xOffset=(int) (mouseX-x*scale);
		yOffset=(int) (mouseY-y*scale);
		
		Point mouse=getMousePosition();
		if (mouse!=null){
			mouseX=mouse.x;
			mouseY=mouse.y;
			mouseRelativeX=(int) (Math.rint(mouseX-xOffset)/scale);
			mouseRelativeY=(int) (Math.rint(mouseY-yOffset)/scale);
		}
	}
	
	public void gotoPos(double x,double y){
		gotoPos(scale,x,y);
	}
	
	private void updataMousePos(int x,int y){
		mouseX=x;
		mouseY=y;
		mouseRelativeX=(int) (Math.rint(mouseX-xOffset)/scale);
		mouseRelativeY=(int) (Math.rint(mouseY-yOffset)/scale);
	}
}
