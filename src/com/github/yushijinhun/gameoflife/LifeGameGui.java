package com.github.yushijinhun.gameoflife;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.github.yushijinhun.nbt4j.io.TagOutputStream;
import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameGui extends Canvas{
	
	public static final double SCALE_FACTOR = 0.75;
	
	public static final double MAX_SCALE=256d;
	
	private static final long serialVersionUID = 1L;
	private static final Font font=new Font("Dialog",Font.BOLD,12);
	private static final Font fontBig=new Font("Dialog",Font.BOLD,16);
	
	public final LifeGameEngine engine;
	public final Lock engineLock;
	public int fps=0;
	public long lastTickingTime=0;
	public boolean showInfo=true;
	public boolean drawFull;
	
	private double cellSize=8;
	private BufferedImage buffer;
	private BufferedImage cellsBuffer;
	private Graphics2D cellsBufferG;
	private final Thread renderThread;
	private boolean fistOpen=true;
	private boolean stopped=false;
	private int xShift=0;
	private int yShift=0;
	private boolean isDragging=false;
	private int dragStartX;
	private int dragStartY;
	private int mouseTipX;
	private int mouseTipY;
	private int mouseX;
	private int mouseY;
	private final ExecutorService threadPool;
	private boolean isComputing=false;
	private boolean isSaving=false;
	
	public LifeGameGui(double blockSize,LifeGameEngine theEngine) {
		cellSize=blockSize;
		drawFull=true;
		engineLock=new ReentrantLock();
		threadPool=Executors.newFixedThreadPool(4);
		
		if (cellSize<1){
			cellSize=1;
		}
		
		if (cellSize>16){
			cellSize=16;
		}
		
		engine=theEngine;
		renderThread=new Thread("Render Thread"){
			
			class CellsRender implements Runnable{
				
				public boolean rendering=false;
				
				public void run() {
					rendering=true;
					if (engineLock.tryLock()){
						try{
							if (drawFull){
								cellsBufferG.setColor(Color.DARK_GRAY);
								cellsBufferG.fillRect(0, 0, cellsBuffer.getWidth(), cellsBuffer.getHeight());
								cellsBufferG.setColor(Color.GREEN);
								for (int x=0;x<engine.width;x++){
									for (int y=0;y<engine.height;y++){
										if (engine.get(x, y)){
											cellsBufferG.fillRect(x, y, 1, 1);
										}
									}
								}
								
								drawFull=false;
							}else{
								int x;
								int y;
								for (int i=0;i<engine.changedPosHead;i++){
									x=engine.changedXPos[i];
									y=engine.changedYPos[i];
									cellsBufferG.setColor(engine.get(x, y)?Color.GREEN:Color.DARK_GRAY);
									cellsBufferG.fillRect(x, y, 1, 1);
								}
								engine.changedPosHead=0;
							}
						}finally{
							engineLock.unlock();
						}
					}
					rendering=false;
				}
			};
			
			private final ExecutorService renderThreadPool=Executors.newCachedThreadPool();
			private final CellsRender cellsRender=new CellsRender();
			
			public void run(){
				while(buffer==null){
					if (getGraphicsConfiguration()!=null){
						Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
						buffer=getGraphicsConfiguration().createCompatibleImage((int)d.getWidth(), (int)d.getHeight());
						cellsBuffer=getGraphicsConfiguration().createCompatibleImage(engine.width, engine.height);
						cellsBufferG=cellsBuffer.createGraphics();
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
							renderThreadPool.shutdown();
							return;
						}
					}
				}
				renderThreadPool.shutdown();
			}
			
		};
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				fistOpen=false;
				
				switch(e.getButton()){
					case MouseEvent.BUTTON1:
						final int x=(int) (Math.rint(e.getX()-xShift)/cellSize);
						final int y=(int) (Math.rint(e.getY()-yShift)/cellSize);
						
						if (x>=engine.width||y>=engine.height||x<0||y<0){
							return;
						}
						
						threadPool.execute(new Runnable() {
							
							public void run() {
								engineLock.lock();
								try{
									engine.set(x, y, !engine.get(x, y));
								}finally{
									engineLock.unlock();
								}
							}
						});
						
						break;
						
					case MouseEvent.BUTTON3:
						if (isDragging==false){
							isDragging=true;
							dragStartX=e.getX()-xShift;
							dragStartY=e.getY()-yShift;
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
				updataMousePos(e);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				updataMousePos(e);
				
				if (!isDragging){
					return;
				}
				
				xShift=e.getX()-dragStartX;
				yShift=e.getY()-dragStartY;
			}
			
			private void updataMousePos(MouseEvent e){
				mouseX=e.getX();
				mouseY=e.getY();
				mouseTipX=(int) (Math.rint(mouseX-xShift)/cellSize);
				mouseTipY=(int) (Math.rint(mouseY-yShift)/cellSize);
			}
		});
		
		addKeyListener(new KeyAdapter() {
			
			private long lastPressedTime=System.currentTimeMillis();
			
			@Override
			public void keyPressed(KeyEvent e) {
				fistOpen=false;
				
				switch(e.getKeyCode()){
					case KeyEvent.VK_N:
						if (isComputing||(lastPressedTime+10)>System.currentTimeMillis()){
							break;
						}
						
						threadPool.execute(new Runnable() {
								
							@Override
							public void run() {
								isComputing=true;
								long start=System.currentTimeMillis();
								
								engineLock.lock();
								try{
									engine.nextFrame();
								}finally{
									engineLock.unlock();
								}
								
								lastTickingTime=System.currentTimeMillis()-start;
								lastPressedTime=System.currentTimeMillis();
								isComputing=false;
							}
						});
					
						break;
						
					case KeyEvent.VK_F1:
						showInfo=!showInfo;
						break;
						
					case KeyEvent.VK_Z:
						double nextScale1=cellSize/SCALE_FACTOR;
						if (nextScale1>MAX_SCALE){
							nextScale1=MAX_SCALE;
						}
						
						setCenter(nextScale1,mouseTipX,mouseTipY);
						cellSize=nextScale1;
						break;
						
					case KeyEvent.VK_X:
						double nextScale2=cellSize*SCALE_FACTOR;
						setCenter(nextScale2,mouseTipX,mouseTipY);
						cellSize=nextScale2;
						break;
						
					case KeyEvent.VK_0:
						if (e.isControlDown()){
							setCenter(1d,mouseTipX,mouseTipY);
							cellSize=1d;
						}
						break;
						
					case KeyEvent.VK_S:
						if (e.isControlDown()&&!isSaving){
							threadPool.execute(new Runnable() {
								
								@Override
								public void run() {
									JFileChooser chooser = new JFileChooser();
									chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
									chooser.setDialogTitle("Save");
									chooser.showSaveDialog(LifeGameGui.this);
									
									File file = chooser.getSelectedFile();
									if (file == null) {
										JOptionPane.showMessageDialog(LifeGameGui.this, "You did not select any files.", "Game of Life", JOptionPane.WARNING_MESSAGE);
										return;
									}
									
									NbtTagCompound comp=new NbtTagCompound("root");
									
									engineLock.lock();
									try{
										engine.writeToNBT(comp);
									}finally{
										engineLock.unlock();
									}
									
									
									TagOutputStream out=null;
									try{
										out=new TagOutputStream(new FileOutputStream(file));
										out.writeTag(comp, true);
									}catch(IOException e1){
										StringBuilder sb=new StringBuilder("When saving game, an IOException occurred.\n\n");
										
										CharArrayWriter writer=new CharArrayWriter();
										e1.printStackTrace(new PrintWriter(writer));
										sb.append(writer.toCharArray());
										writer.close();
										
										JOptionPane.showMessageDialog(LifeGameGui.this, sb.toString().replaceAll("\t", "    "), "Game of Life", JOptionPane.ERROR_MESSAGE);
										return;
									}finally{
										if (out!=null){
											try {
												out.close();
											} catch (IOException e1) {
												StringBuilder sb=new StringBuilder("When saving game, an IOException occurred.\n\n");
												
												CharArrayWriter writer=new CharArrayWriter();
												e1.printStackTrace(new PrintWriter(writer));
												sb.append(writer.toCharArray());
												writer.close();
												
												JOptionPane.showMessageDialog(LifeGameGui.this, sb.toString().replaceAll("\t", "    "), "Game of Life", JOptionPane.ERROR_MESSAGE);
												return;
											}
										}
									}
									
									JOptionPane.showMessageDialog(LifeGameGui.this, "The game saved to "+file.getPath(), "Game of Life", JOptionPane.INFORMATION_MESSAGE);
								}
							});
						}
						break;
				}
			}
		});
		
		setSize((int) (Math.rint(engine.width*cellSize)), (int) (Math.rint(engine.height*cellSize)));
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
		g2d.drawImage(cellsBuffer, xShift, yShift,(int) (Math.rint(cellsBuffer.getWidth()*cellSize)),(int) (Math.rint(cellsBuffer.getHeight())*cellSize), null);
		
		if (showInfo){
			g2d.setFont(font);
			renderString(g2d,"cell size: "+cellSize, 0, 40);
			renderString(g2d,"last ticking time: "+(isComputing?"computing...":lastTickingTime), 0, 30);
			renderString(g2d,"ticks: "+engine.getTicks(), 0, 20);
			renderString(g2d,"fps: "+fps, 0, 10);
			renderString(g2d, "("+mouseTipX+", "+mouseTipY+")", mouseX+10, mouseY+10);
		}
		
		if (fistOpen){
			g2d.setFont(fontBig);
			renderString(g2d,"Game of Life", 80, 90);
			renderString(g2d,"by yushijinhun", 80, 105);
			renderString(g2d,"Press N to show next frame", 80, 120);
			renderString(g2d,"Click to change one cell's status", 80, 135);
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
	}
	
	public double getCellSize() {
		return cellSize;
	}
	
	public void setCenter(double scale,int x,int y){
		xShift=(int) (mouseX-x*scale);
		yShift=(int) (mouseY-y*scale);
	}
}
