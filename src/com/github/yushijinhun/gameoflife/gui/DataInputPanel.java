package com.github.yushijinhun.gameoflife.gui;

import javax.swing.JPanel;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessEvent;
import com.github.yushijinhun.gameoflife.gui.event.DataProcessListener;

public class DataInputPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public void addDataProcessListener(DataProcessListener l){
		listenerList.add(DataProcessListener.class, l);
	}
	
	public void removeDataProcessListener(DataProcessListener l){
		listenerList.remove(DataProcessListener.class, l);
	}
	
	public DataProcessListener[] getDataProcessListeners(){
		return listenerList.getListeners(DataProcessListener.class);
	}
	
	protected void poseDataProcessEvent(DataProcessEvent e){
		DataProcessListener[] ls=getDataProcessListeners();
		for (int i = 0; i < ls.length; i++) {
			ls[i].process(e);
		}
	}
}
