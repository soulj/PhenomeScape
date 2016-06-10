package org.cytoscape.phenomescape.internal.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.cytoscape.model.CyTableManager;
import org.cytoscape.phenomescape.internal.ControlPanel;
import org.cytoscape.work.swing.DialogTaskManager;



public class AnalyseMouseListener implements ActionListener {

	private ControlPanel controlPanel;
	
	public AnalyseMouseListener (ControlPanel controlPanel,CyTableManager cyTableManager) {
		this.controlPanel = controlPanel;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		PhenomeExpressTaskFactory phenomeExpress = new PhenomeExpressTaskFactory(controlPanel);
		controlPanel.cyServiceRegistrar.getService(DialogTaskManager.class).execute(phenomeExpress.createTaskIterator());
	
	}
}
