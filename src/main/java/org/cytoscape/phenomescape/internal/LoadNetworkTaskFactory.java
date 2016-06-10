package org.cytoscape.phenomescape.internal;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
public class LoadNetworkTaskFactory implements TaskFactory {
	

	
	
	private CyServiceRegistrar cyServiceRegistrar;
	private InputStream input;
	private String species;

	public LoadNetworkTaskFactory (CyServiceRegistrar cyServiceRegistrar,InputStream input, String species) {
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.input = input;
		this.species = species;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		LoadNetworkTask task = null;
		task = new LoadNetworkTask(cyServiceRegistrar,input,species);
		
		return new TaskIterator(task);
	}
	
	@Override
	public boolean isReady() {
		return false;
	}
}
