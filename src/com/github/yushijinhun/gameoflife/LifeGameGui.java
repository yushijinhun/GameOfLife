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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import com.github.yushijinhun.nbt4j.io.TagOutputStream;
import com.github.yushijinhun.nbt4j.tags.NbtTagCompound;

public class LifeGameGui extends Canvas {
	
	private static final long serialVersionUID = 1L;
	private static final Font font=new Font("Dialog",Font.BOLD,12);
	private static final Font fontBig=new Font("Dialog",Font.BOLD,16);
	
	public final LifeGameEngine engine;
	public int fps=0;
	public long lastTickingTime=0;
	public boolean showInfo=true;
	public boolean drawFull;
	
	private int cellSize=8;
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
	
	public LifeGameGui(int blockSize,LifeGameEngine theEngine) {
		cellSize=blockSize;
		drawFull=true;
		
		if (cellSize<1){
			cellSize=1;
		}
		
		if (cellSize>16){
			cellSize=16;
		}
		
		engine=theEngine;
		renderThread=new Thread("Render Thread"){
			
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
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				fistOpen=false;

				switch(e.getButton()){
					case MouseEvent.BUTTON1:
						int x=(e.getX()-xShift)/cellSize;
						int y=(e.getY()-yShift)/cellSize;
						
						if (x>=engine.width||y>=engine.height||x<0||y<0){
							return;
						}
						
						synchronized (engine) {
							engine.set(x, y, !engine.get(x, y));
						}
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
				mouseTipX=(mouseX-xShift)/cellSize;
				mouseTipY=(mouseY-yShift)/cellSize;
			}
		});
		
		addKeyListener(new KeyAdapter() {
			
			private long lastPressedTime=System.currentTimeMillis();
			
			@Override
			public void keyPressed(KeyEvent e) {
				fistOpen=false;
				
				switch(e.getKeyCode()){
					case KeyEvent.VK_N:
						if ((lastPressedTime+10)>System.currentTimeMillis()){
							return;
						}
						long start=System.currentTimeMillis();
						synchronized (engine) {
							engine.nextFrame();
						}
						lastTickingTime=System.currentTimeMillis()-start;
						lastPressedTime=System.currentTimeMillis();
						break;
				
					case KeyEvent.VK_F1:
						showInfo=!showInfo;
						break;
						
					case KeyEvent.VK_Z:
						if (cellSize>1){
							cellSize--;
						}
						break;
						
					case KeyEvent.VK_X:
						cellSize++;
						break;
						
					case KeyEvent.VK_S:
						if (e.isControlDown()){
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
							synchronized (engine) {
								engine.writeToNBT(comp);
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
						break;
				}
			}
		});
		
		setSize(engine.width*cellSize, engine.height*cellSize);
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
		synchronized (engine) {
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
			
			g2d.drawImage(cellsBuffer, xShift, yShift,cellsBuffer.getWidth()*cellSize,cellsBuffer.getHeight()*cellSize, null);
			
			if (showInfo){
				g2d.setFont(font);
				renderString(g2d,"cell size: "+cellSize, 0, 40);
				renderString(g2d,"last ticking time: "+lastTickingTime, 0, 30);
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
		
		try {
			renderThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public int getCellSize() {
		return cellSize;
	}
}
