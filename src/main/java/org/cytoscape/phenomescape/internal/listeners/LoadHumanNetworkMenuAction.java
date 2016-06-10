package org.cytoscape.phenomescape.internal.listeners;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.phenomescape.internal.LoadNetworkTaskFactory;
import org.cytoscape.phenomescape.internal.util.NetworkUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.swing.DialogTaskManager;

public class LoadHumanNetworkMenuAction extends AbstractCyAction{
		
	private CyServiceRegistrar cyServiceRegistrar;

	public LoadHumanNetworkMenuAction(CyApplicationManager cyApplicationManager, CyServiceRegistrar cyServiceRegistrar) {
		super("Load Human Network",cyApplicationManager,null,null);
		setPreferredMenu("Apps.PhenomeScape");
		this.cyServiceRegistrar = cyServiceRegistrar;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		InputStream input = LoadHumanNetworkMenuAction.class.getResourceAsStream("/HumanNetwork.txt");
		LoadNetworkTaskFactory loadNetworkTaskFactory = new LoadNetworkTaskFactory(cyServiceRegistrar,input,"Human");
		cyServiceRegistrar.getService(DialogTaskManager.class).execute(loadNetworkTaskFactory.createTaskIterator());
	
		
	}

}
