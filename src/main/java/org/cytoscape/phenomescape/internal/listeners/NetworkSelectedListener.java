package org.cytoscape.phenomescape.internal.listeners;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.phenomescape.internal.ControlPanel;
import org.cytoscape.phenomescape.internal.util.CommandExecutor;
import org.cytoscape.service.util.CyServiceRegistrar;


public class NetworkSelectedListener implements ItemListener  {
	
	private ControlPanel controlPanel;



	public NetworkSelectedListener(ControlPanel controlPanel){
		this.controlPanel = controlPanel;
	}



	@Override
    public void itemStateChanged(ItemEvent event) {
		
       if (event.getStateChange() == ItemEvent.SELECTED) {
    	   CyServiceRegistrar cyServiceRegistrar = controlPanel.cyServiceRegistrar;
    	   JComboBox comboBox = (JComboBox) event.getSource();
    	   String item = (String) comboBox.getSelectedItem();
   		CommandExecutor.execute("network set current network=" + item, cyServiceRegistrar);
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
          controlPanel.setGeneName(geneNameComboModel);
          controlPanel.setFoldChange(foldChangeComboModel);
          controlPanel.setPvalue(pvalueComboModel);
          
                 }
    }       
	
	
	

}
