package org.cytoscape.phenomescape.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.phenomescape.internal.listeners.LoadHumanNetworkMenuAction;
import org.cytoscape.phenomescape.internal.listeners.LoadMouseNetworkMenuAction;
import org.cytoscape.phenomescape.internal.util.TableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		
		CyServiceRegistrar cyServiceRegistrar = getService(context, CyServiceRegistrar.class);

	
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		
	

		LoadHumanNetworkMenuAction loadHumanNetworkMenuAction = new LoadHumanNetworkMenuAction(cyApplicationManager,cyServiceRegistrar);
		LoadMouseNetworkMenuAction loadMouseNetworkMenuAction = new LoadMouseNetworkMenuAction(cyApplicationManager,cyServiceRegistrar);
		
		TableManager myTableManager = new TableManager(cyServiceRegistrar);
		
		Properties properties = new Properties();
		

		registerAllServices(context, loadHumanNetworkMenuAction, properties);
		registerAllServices(context, loadMouseNetworkMenuAction, properties);
		
		CyTableManager cyTableManager = getService(context, CyTableManager.class);
		
		
		ControlPanel myControlPanel = new ControlPanel(cyServiceRegistrar,cyTableManager);
		registerService(context, myControlPanel, CytoPanelComponent.class, properties);
		
		ResultsPanel myResultsPanel = new ResultsPanel(cyServiceRegistrar);
		registerService(context, myResultsPanel, CytoPanelComponent.class, properties);
		
		
		
		
	}

		
		
		
	}


