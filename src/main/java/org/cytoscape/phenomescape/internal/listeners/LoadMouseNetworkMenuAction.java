package org.cytoscape.phenomescape.internal.listeners;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.phenomescape.internal.LoadNetworkTaskFactory;
import org.cytoscape.phenomescape.internal.util.NetworkUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

public class LoadMouseNetworkMenuAction extends AbstractCyAction{
	
	private CyServiceRegistrar cyServiceRegistrar;


	public LoadMouseNetworkMenuAction(CyApplicationManager cyApplicationManager, CyServiceRegistrar cyServiceRegistrar) {
		super("Load Mouse Network",cyApplicationManager,null,null);
		setPreferredMenu("Apps.PhenomeScape");
		this.cyServiceRegistrar = cyServiceRegistrar;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
			
		
		InputStream input = LoadMouseNetworkMenuAction.class.getResourceAsStream("/MouseNetwork.txt");
		
		LoadNetworkTaskFactory loadNetworkTaskFactory = new LoadNetworkTaskFactory(cyServiceRegistrar,input,"Mouse");
		cyServiceRegistrar.getService(DialogTaskManager.class).execute(loadNetworkTaskFactory.createTaskIterator());
	
		
	}

}
