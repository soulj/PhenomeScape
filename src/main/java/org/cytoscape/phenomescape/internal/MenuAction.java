package org.cytoscape.phenomescape.internal;
import java.awt.event.ActionEvent;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


/**
 * Creates a new menu item under Apps menu section.
 *
 */
public class MenuAction extends AbstractCyAction {

	private final CyApplicationManager applicationManager;
	
	public MenuAction(final CyApplicationManager applicationManager, final String menuTitle) {
		super(menuTitle, applicationManager, null, null);
		this.applicationManager = applicationManager;
		setPreferredMenu("Apps");
	}
	
	
	public void actionPerformed(ActionEvent e) {

	    final CyNetworkView currentNetworkView = applicationManager.getCurrentNetworkView();
	    if (currentNetworkView == null)
	       return;
	    
	    // View is always associated with its model.
	    final CyNetwork network = currentNetworkView.getModel();
	    for (CyNode node : network.getNodeList()) {

	        if (network.getNeighborList(node,CyEdge.Type.ANY).isEmpty()) {
	        	currentNetworkView.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);	        	
	        }
	    }
	    currentNetworkView.updateView();
	}
	
}

