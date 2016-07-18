package org.cytoscape.phenomescape.internal.util;

import java.awt.Component;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;

public class CytoPanelUtils {
	
	public static CytoPanelComponent getCytoPanel (CyServiceRegistrar cyServiceRegistrar, Class<? extends CytoPanelComponent> panelClass, CytoPanelName panelName) {
		CytoPanel cytoPanel = cyServiceRegistrar.getService(CySwingApplication.class).getCytoPanel(panelName);
		for (int i = 0; i < cytoPanel.getCytoPanelComponentCount(); i++) {
			Component panel = cytoPanel.getComponentAt(i);
			if (panelClass.isInstance(panel))
				return (CytoPanelComponent) panel;
		}
		return null;
	}
	
	public static CytoPanelComponent getCytoPanel (CyServiceRegistrar cyServiceRegistrar, Class<? extends CytoPanelComponent> panelClass) {
		for (CytoPanelName panelName : CytoPanelName.values()) {
			CytoPanelComponent panel = getCytoPanel(cyServiceRegistrar, panelClass, panelName);
			if (panel != null) return panel;
		}
		return null;
	}
	
	public static void showCytoPanel(CyServiceRegistrar cyServiceRegistrar,CytoPanelName panelName){
		CytoPanel cytoPanel = cyServiceRegistrar.getService(CySwingApplication.class).getCytoPanel(panelName);
		if (cytoPanel.getState()==CytoPanelState.HIDE){
			cytoPanel.setState(CytoPanelState.DOCK);
		}
	}
}


