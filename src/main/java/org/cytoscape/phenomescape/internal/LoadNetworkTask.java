package org.cytoscape.phenomescape.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.phenomescape.internal.util.NetworkUtils;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class LoadNetworkTask extends AbstractTask implements ObservableTask  {

	private CyServiceRegistrar cyServiceRegistrar;
	private InputStream input;
	private String species;

	public LoadNetworkTask(CyServiceRegistrar cyServiceRegistrar,InputStream input, String species) {
		
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.input = input;
		this.species= species;
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		taskMonitor.setTitle("Loading " +species +  " Network");
		taskMonitor.setStatusMessage("Creating network");
		
		CyNetworkFactory networkFactory = cyServiceRegistrar.getService(CyNetworkFactory.class);
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, NetworkUtils.getUniqueNetworkName(cyServiceRegistrar, species + "Network"));
		network.getDefaultEdgeTable().createColumn("Confidence",Double.class,false);

		Map<String, CyNode>  nodeNameMap = new HashMap<String, CyNode>() ;
		Scanner sc = new Scanner(input);
		nodeNameMap = new HashMap<String,CyNode>();

		while (sc.hasNextLine()){
			String line = sc.nextLine();
			String [] nodes = line.split("\t");
			CyNode node1 = null;
			CyNode node2 = null;
			if (nodeNameMap.containsKey(nodes[0])){

				node1 = nodeNameMap.get(nodes[0]);
			}
			else {

				node1 = network.addNode();
				CyRow attributes = network.getRow(node1);
				attributes.set("name", nodes[0]);
				nodeNameMap.put(nodes[0], node1);
			}
			if (nodeNameMap.containsKey(nodes[1])){

				node2 = (CyNode) nodeNameMap.get(nodes[1]);
			}
			else {

				node2 = network.addNode();
				CyRow attributes = network.getRow(node2);
				attributes.set("name", nodes[1]);
				nodeNameMap.put(nodes[1], node2);
			}
			

				CyEdge myEdge =network.addEdge(node1, node2, false);
				network.getRow(myEdge).set("interaction", "ppi");
				network.getRow(myEdge).set("Confidence", Double.parseDouble(nodes[2]));
				network.getRow(myEdge).set("name", nodes[0]+ " (ppi) " +nodes[1]);

			

		}
		sc.close();
		CyNetworkManager networkManager = cyServiceRegistrar.getService(CyNetworkManager.class);
		networkManager.addNetwork(network);
		
	}

}
