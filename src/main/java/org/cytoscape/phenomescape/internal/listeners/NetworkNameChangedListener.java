package org.cytoscape.phenomescape.internal.listeners;

import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.phenomescape.internal.ControlPanel;
import org.cytoscape.service.util.CyServiceRegistrar;

public class NetworkNameChangedListener implements RowsSetListener{
	
	
	private JComboBox networkComboBox;
	private ControlPanel controlPanel;



	public NetworkNameChangedListener(ControlPanel controlPanel, JComboBox networkComboBox){
		this.controlPanel = controlPanel;
		this.networkComboBox = networkComboBox;
	}	
	
	
	public void handleEvent(RowsSetEvent e) {
        // First check whether the Name field of any of the rows got changed.
        if(e.containsColumn(CyNetwork.NAME)){
        	CyServiceRegistrar cyServiceRegistrar = controlPanel.cyServiceRegistrar;
    		DefaultComboBoxModel networkModel =  (DefaultComboBoxModel) networkComboBox.getModel();
    		networkModel.removeAllElements();		
    		for (CyNetwork cyNetwork : cyServiceRegistrar.getService(CyNetworkManager.class).getNetworkSet()) {
    			String cyNetworkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);
    			networkModel.addElement(cyNetworkName);
    		}
        	
        }
	}


}
