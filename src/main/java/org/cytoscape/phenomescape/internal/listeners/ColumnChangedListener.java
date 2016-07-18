package org.cytoscape.phenomescape.internal.listeners;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.phenomescape.internal.ControlPanel;
import org.cytoscape.phenomescape.internal.util.CommandExecutor;
import org.cytoscape.service.util.CyServiceRegistrar;


public class ColumnChangedListener implements ColumnCreatedListener, ColumnDeletedListener,ColumnNameChangedListener  {

	private ControlPanel controlPanel;




	public ColumnChangedListener(ControlPanel controlPanel){
		this.controlPanel = controlPanel;
	}



	@Override
	public void handleEvent(ColumnCreatedEvent  event) {
		updateComboOptions();

	}



	@Override
	public void handleEvent(ColumnDeletedEvent  event) {

		updateComboOptions();
	}



	@Override
	public void handleEvent(ColumnNameChangedEvent  event) {
		updateComboOptions();

	}       


	public void updateComboOptions(){
		CyServiceRegistrar cyServiceRegistrar = controlPanel.cyServiceRegistrar;
		if (controlPanel.getNetworkValue()!=null){
			CommandExecutor.execute("network set current network=" + controlPanel.getNetworkValue(), cyServiceRegistrar);
			CyNetwork selectedNetwork = cyServiceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

			CyTable selectedTable = selectedNetwork.getDefaultNodeTable();
			Collection<CyColumn> columns = selectedTable.getColumns();
			DefaultComboBoxModel geneNameComboModel = new DefaultComboBoxModel();
			DefaultComboBoxModel foldChangeComboModel = new DefaultComboBoxModel();
			DefaultComboBoxModel pvalueComboModel = new DefaultComboBoxModel();
			for (CyColumn column: columns){
				geneNameComboModel.addElement(column.getName());
				foldChangeComboModel.addElement(column.getName());
				pvalueComboModel.addElement(column.getName());
			}
			String geneName = controlPanel.getGeneNameValue();
			String foldChange = controlPanel.getFoldChangeValue();
			String pvalue = controlPanel.getpvalueValue();
			
			if (geneNameComboModel.getIndexOf(geneName)>=0){
				geneNameComboModel.setSelectedItem(geneName);
			}
			if (foldChangeComboModel.getIndexOf(foldChange)>=0){
				foldChangeComboModel.setSelectedItem(foldChange);
			}
			if (pvalueComboModel.getIndexOf(pvalue)>=0){
				pvalueComboModel.setSelectedItem(pvalue);
			}
			
			
			
			controlPanel.setGeneName(geneNameComboModel);
			controlPanel.setFoldChange(foldChangeComboModel);
			controlPanel.setPvalue(pvalueComboModel);
		}

	}




}
