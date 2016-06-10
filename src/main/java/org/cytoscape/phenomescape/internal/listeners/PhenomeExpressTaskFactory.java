package org.cytoscape.phenomescape.internal.listeners;

import java.io.FileNotFoundException;

import org.cytoscape.phenomescape.internal.ControlPanel;
import org.cytoscape.phenomescape.internal.PhenomeExpress;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
public class PhenomeExpressTaskFactory implements TaskFactory {
	
	private ControlPanel controlPanel;
	
	
	public PhenomeExpressTaskFactory (ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
		
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		PhenomeExpress task = null;
		try {
			task = new PhenomeExpress(controlPanel);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new TaskIterator(task);
	}
	
	@Override
	public boolean isReady() {
		return false;
	}
}
