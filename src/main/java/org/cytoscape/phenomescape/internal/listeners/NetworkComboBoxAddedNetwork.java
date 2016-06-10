package org.cytoscape.phenomescape.internal.listeners;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;


public class NetworkComboBoxAddedNetwork implements NetworkAddedListener {
	private JComboBox networkComboBox;


	public NetworkComboBoxAddedNetwork (JComboBox networkComboBox) {
		this.networkComboBox = networkComboBox;
	}
	
	
	@Override
	public void handleEvent(NetworkAddedEvent event) {
		CyNetwork cyNetwork = event.getNetwork();
		if (cyNetwork != null) {
			String cyNetworkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);
			((DefaultComboBoxModel)networkComboBox.getModel()).addElement(cyNetworkName);
		}
	}
}
