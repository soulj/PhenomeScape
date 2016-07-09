package org.cytoscape.phenomescape.internal.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.TaskIterator;



import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.phenomescape.internal.CyActivator;
import org.cytoscape.phenomescape.internal.PhenoGeneNetwork;
import org.cytoscape.phenomescape.internal.PhenomeExpress;
import org.cytoscape.phenomescape.internal.listeners.PhenomeExpressTaskFactory;
import org.cytoscape.phenomescape.internal.util.CommandExecutor;




public class NetworkUtils {

	public static CyNetwork getCyNetwork (CyServiceRegistrar cyServiceRegistrar, String networkName) {
		Set<CyNetwork> networks = cyServiceRegistrar.getService(CyNetworkManager.class).getNetworkSet();
		for (CyNetwork network : networks)
			if (network.getRow(network).get(CyNetwork.NAME, String.class).equals(networkName))
				return network;
		return null;
	}
	public static CyNode getCyNode (CyNetwork cyNetwork, String nodeName) {
		for (CyNode node : cyNetwork.getNodeList())
			if (cyNetwork.getRow(node).get(CyNetwork.NAME, String.class).equals(nodeName))
				return node;
		return null;
	}
	public static CyEdge getCyEdge (CyNetwork cyNetwork, String edgeName) {
		for (CyEdge edge : cyNetwork.getEdgeList())
			if (cyNetwork.getRow(edge).get(CyNetwork.NAME, String.class).equals(edgeName))
				return edge;
		return null;
	}
	public static List<CyEdge> getCyEdges (CyServiceRegistrar cyServiceRegistrar, String cyNetworkName) {
		CyNetwork cyNetwork = getCyNetwork(cyServiceRegistrar, cyNetworkName);
		return cyNetwork.getEdgeList();
	}
	public static String getUniqueNetworkName (CyServiceRegistrar cyServiceRegistrar, String name) {
		String uniqueName = name;
		for (int i = 1; getCyNetwork(cyServiceRegistrar, uniqueName) != null; i++) {
			uniqueName = name + "_" + i;
		}
		return uniqueName;
	}

	public static void readCyNetworkFromFile (CyServiceRegistrar cyServiceRegistrar, InputStream input) {
		
		CyNetworkFactory networkFactory = cyServiceRegistrar.getService(CyNetworkFactory.class);
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, "Network");
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
			//	//System.out.println(nodes[2]);
				network.getRow(myEdge).set("Confidence", Double.parseDouble(nodes[2]));
				network.getRow(myEdge).set("name", nodes[0]+ " (ppi) " +nodes[1]);

			

		}
		sc.close();
		CyNetworkManager networkManager = cyServiceRegistrar.getService(CyNetworkManager.class);
		networkManager.addNetwork(network);
		
	}













//	CyNetworkReader networkReader = cyServiceRegistrar.getService(CyNetworkReaderManager.class).getReader(cyNetworkFile.toURI(), cyNetworkFile.getName());
//	TaskIterator taskIterator = new TaskIterator(networkReader);
//
//	cyServiceRegistrar.getService(TaskManager.class).execute(taskIterator);
//	//	//	CommandExecutor.execute(new TaskIterator(networkReader), cyServiceRegistrar);
//
//	cyServiceRegistrar.getService(LoadNetworkFileTaskFactory.class).createTaskIterator(cyNetworkFile);
//	//		cyServiceRegistrar.getService(TaskManager.class);
//	CyNetwork network = networkReader.getNetworks()[0];








public static CySubNetwork createSubNetwork(CyRootNetwork cyRootNetwork, Collection<CyNode> cyNodes) {
	Set<CyEdge> cyEdges = new HashSet<CyEdge>();
	for (CyNode n : cyNodes) {
		Set<CyEdge> adjacentEdges = new HashSet<CyEdge>(cyRootNetwork.getAdjacentEdgeList(n, CyEdge.Type.ANY));
		// Get only the edges that connect nodes that belong to the subnetwork:
		for (CyEdge e : adjacentEdges) {
			if (cyNodes.contains(e.getSource()) && cyNodes.contains(e.getTarget())) {
				cyEdges.add(e);
			}
		}

	}
	CySubNetwork subNet = cyRootNetwork.addSubNetwork(cyNodes, cyEdges);


	return subNet;
}

public static CyNode getNodeWithName(CyNetwork net,  CyTable table,String colname, String value)
{
	CyRow matchingRow = table.getMatchingRows(colname, value).iterator().next();
	String primaryKeyColname = table.getPrimaryKey().getName();
	Long nodeId = matchingRow.get(primaryKeyColname, Long.class);
	CyNode node = net.getNode(nodeId);
	return node;
}

public static Set<CyNode> getNodesWithValue(
		final CyNetwork net, final CyTable table,
		final String colname, final Object value)
		{
	final Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
	final Set<CyNode> nodes = new HashSet<CyNode>();
	final String primaryKeyColname = table.getPrimaryKey().getName();
	for (final CyRow row : matchingRows)
	{
		final Long nodeId = row.get(primaryKeyColname, Long.class);
		if (nodeId == null)
			continue;
		final CyNode node = net.getNode(nodeId);
		if (node == null)
			continue;
		nodes.add(node);
	}
	return nodes;
		}

public static Set<CyNode> getNodeswithFoldChange(
		final CyNetwork net, final CyTable table,
		final String foldChangeColname, final String pValueColname, final Object value)
		{
	List<CyRow> matchingRows = table.getAllRows()    ;    
	final Set<CyNode> nodes = new HashSet<CyNode>();
	final String primaryKeyColname = table.getPrimaryKey().getName();
	for (final CyRow row : matchingRows)
	{
		final Long nodeId = row.get(primaryKeyColname, Long.class);
		final Double foldChange = row.get(foldChangeColname, Double.class);
		final Double pValue = row.get(pValueColname, Double.class);
		if (foldChange == null || pValue == null){            
			final CyNode node = net.getNode(nodeId);
			nodes.add(node);

		}

	}
	return nodes;
		}

public static double[][] createAdjMatrix(CyNetwork network,String attributeName) {

	//make an adjacencymatrix for the current network
	int totalnodecount = network.getNodeList().size();

	CyTable edgeTable = network.getDefaultEdgeTable();

	double[][] adjacencyMatrixOfNetwork = new double[totalnodecount][totalnodecount];

	List<CyNode> cyNodes = network.getNodeList();
	Map<CyNode,Integer> cyNodeMap= new HashMap<>();
	int i =0;
	for (CyNode node: cyNodes){

		cyNodeMap.put(node,i);
		i++;

	}
	List<CyEdge> edgeList = network.getEdgeList();
	//System.out.println(edgeList.size());
	for ( CyEdge edge : edgeList){

		CyRow row = edgeTable.getRow(edge.getSUID());

		if (attributeName != null){
			adjacencyMatrixOfNetwork[cyNodeMap.get(edge.getSource())][cyNodeMap.get(edge.getTarget())]=row.get(attributeName,Double.class);
		}
		else{
			adjacencyMatrixOfNetwork[cyNodeMap.get(edge.getSource())][cyNodeMap.get(edge.getTarget())]=1;
		}

	}
	return adjacencyMatrixOfNetwork;


}
}
