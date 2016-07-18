package org.cytoscape.phenomescape.internal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;

import org.cytoscape.phenomescape.internal.util.CCInfo;
import org.cytoscape.phenomescape.internal.util.ConnectedComponentAnalyser;
import org.cytoscape.phenomescape.internal.util.NetworkUtils;


public class ProteinNetwork  {

	private int nodeCount;
	private CyNetwork network;
	private CyTable edgeTable;
	private CyTable nodeTable;
	private List<CyNode> nodes;
	private List<CyEdge> edges;
	private Map<CyNode,Integer> Node2IndexMap;
	private Map<String,Integer> Name2IndexMap;
	private Map<Integer,CyNode> Index2NodeMap;
	private SparseRowMatrix adjMatrix;
	private double [] pivalues;
	private HashMap<Integer, Protein> Index2ProteinMap;
	public ProteinNetwork(CyNetwork network){
		this.network=network;
		this.nodeTable=network.getDefaultNodeTable();
		this.edgeTable=network.getDefaultEdgeTable();

	}

	public void filterByExpression(String foldChangeName,String pvalueName,String geneNameName) throws IOException{

		try{	
			Set<CyNode> nodesToBeRemoved= NetworkUtils.getNodeswithFoldChange(this.network,this.nodeTable , foldChangeName, pvalueName,"");
			if (nodesToBeRemoved.size()>0){
				filterNetwork(nodesToBeRemoved);
			}
		}
		catch (Exception e) {
			throw new IOException("fold changes have to be numeric!");
		}


		CCInfo lcc = largestConnectedComponent();
		if (lcc.getSize()<1) throw new IOException("Error filtering expression data - are the gene names correct?");
		
		
		Set <CyNode> nodesToKeep = lcc.getNodes();

		List <CyNode> currentNodeSet = network.getNodeList();

		currentNodeSet.removeAll(nodesToKeep);
		if (currentNodeSet.size()>0){		
			filterNetwork(currentNodeSet);				
		}
		nodeTable = network.getDefaultNodeTable();

		//error checking
		if (nodeTable.getColumn(pvalueName).getType() != Double.class){
			throw new IOException("The p-values to be numeric");
		};
		if (nodeTable.getColumn(foldChangeName).getType() != Double.class){
			throw new IOException("The fold change values have to be numeric");
		};
		if (Collections.min(nodeTable.getColumn(pvalueName).getValues(Double.class))<0){
			throw new IOException("The p-values have to be positive");
		}
		if (nodeTable.getColumn(pvalueName).getValues(Double.class).contains(null)){
			throw new IOException("The pvalues can't be NA or blank");
		}
		if (nodeTable.getColumn(foldChangeName).getValues(Double.class).contains(null)){
			throw new IOException("The pvalues can't be NA or blank");
		}
		try {
			nodeTable.getColumn(geneNameName).getValues(String.class).contains(null);
		}
		catch (Exception e){
			throw new IOException("Gene Names must be a strings");
		}



		nodeTable.getColumn(foldChangeName).getValues(Double.class);
		nodeTable.getColumn(pvalueName).getValues(Double.class);
		nodeTable.getColumn(geneNameName).getValues(String.class);

		if( nodeTable.getColumn("Pi Value") == null) {
			nodeTable.createColumn("Pi Value", Double.class, false);
		}

		pivalues = new double [network.getNodeList().size()];

		List<CyNode> nodes = network.getNodeList();

		for (int i= 0;i<nodes.size(); i++) {
			CyRow row = nodeTable.getRow(nodes.get(i).getSUID());
			Double fc = row.get(foldChangeName, Double.class);
			Double pvalue = row.get(pvalueName, Double.class);
			double pivalue = Math.abs(fc) * (-1) * Math.log10(pvalue);
			pivalues[i]=pivalue;
			row.set("Pi Value",pivalue);

		}
	}


	public double[] getPivalues() {
		return pivalues;
	}

	public void createSparseAdjMatrix() {

		int totalnodecount = network.getNodeList().size();
		nodeCount=totalnodecount;

		this.edgeTable = network.getDefaultEdgeTable();
		this.nodeTable = network.getDefaultNodeTable();
		adjMatrix= SparseRowMatrix.create(totalnodecount,totalnodecount);

		List<CyNode> cyNodes = network.getNodeList();
		Node2IndexMap= new HashMap<>();
		Name2IndexMap= new HashMap<>();
		Index2ProteinMap= new HashMap<>();

		int i =0;
		for (CyNode node: cyNodes){

			Node2IndexMap.put(node,i);
			String name1 =nodeTable.getRow(node.getSUID()).get("name",String.class);
			Name2IndexMap.put(name1,i);
			Index2ProteinMap.put(i,new Protein(i,name1,node,pivalues[i]));
			i++;

		}


		List<CyEdge> edgeList = network.getEdgeList();

		
		if( edgeTable.getColumn("Confidence") == null) {
			
			for ( CyEdge edge : edgeList){

				CyRow row = edgeTable.getRow(edge.getSUID());

				adjMatrix.set(Node2IndexMap.get(edge.getSource()),Node2IndexMap.get(edge.getTarget()),1.0);
				adjMatrix.set(Node2IndexMap.get(edge.getTarget()),Node2IndexMap.get(edge.getSource()),1.0);

			}
						
		}
		else{
		
			for ( CyEdge edge : edgeList){
	
				CyRow row = edgeTable.getRow(edge.getSUID());
	
				adjMatrix.set(Node2IndexMap.get(edge.getSource()),Node2IndexMap.get(edge.getTarget()),row.get("Confidence",Double.class));
				adjMatrix.set(Node2IndexMap.get(edge.getTarget()),Node2IndexMap.get(edge.getSource()),row.get("Confidence",Double.class));
			}
	
		}
	}


	public CCInfo largestConnectedComponent() {

		ConnectedComponentAnalyser ccAnalyser = new ConnectedComponentAnalyser(this.network);
		CCInfo Lcc = ccAnalyser.findLargestComponent();
		return Lcc;

	}


	public AMatrix normalise(double transitionProb,PhenoGeneNetwork phenoGeneNetwork) throws IOException{

		Collection<Integer> annotated = phenoGeneNetwork.getGeneName2IndexMap().values();
		AMatrix matrix = adjMatrix.copy();
		for ( int i=0;i<matrix.rowCount();i++){
			AVector row=matrix.getRow(i);
			double sum=row.elementSum();
			row=row.mutable();
			if (annotated.contains(i)){
				row.multiply(1.0-transitionProb);
				row.divide(sum);
				matrix.replaceRow(i, row);
			}
			else{
				row.divide(sum);
				matrix.replaceRow(i, row);
			}
		}
		return(matrix);
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public SparseRowMatrix getAdjMatrix() {
		return adjMatrix;
	}

	public CyTable getEdgeTable() {
		return edgeTable;
	}

	public CyTable getNodeTable() {
		return nodeTable;
	}

	public List<CyNode> getNodes() {
		return nodes;
	}

	public List<CyEdge> getEdges() {
		return edges;
	}

	public Map<CyNode, Integer> getNode2IndexMap() {
		return Node2IndexMap;
	}

	public Map<String, Integer> getName2IndexMap() {
		return Name2IndexMap;
	}


	public void filterNetwork (Collection<CyNode> nodesToBeRemoved){
		List<CyEdge> edgesToBeRemoved = new ArrayList<CyEdge>();
		for (CyNode node:nodesToBeRemoved) {

			edgesToBeRemoved.addAll(network.getAdjacentEdgeList(node, CyEdge.Type.ANY));

		}


		List<Long> pNKeys = new ArrayList<>();

		for(CyNode n : nodesToBeRemoved){
			pNKeys.add(n.getSUID());
		}		
		List<Long> pEKeys = new ArrayList<>();
		for(CyEdge n : edgesToBeRemoved){
			pEKeys.add(n.getSUID());
		}	

		network.removeNodes(nodesToBeRemoved);
		network.removeEdges(edgesToBeRemoved);
		network.getDefaultNodeTable().deleteRows(pNKeys);
		network.getDefaultEdgeTable().deleteRows(pEKeys);
	}

	public Map<Integer, CyNode> getIndex2NodeMap() {
		return Index2NodeMap;
	}

	public HashMap<Integer, Protein> getIndex2ProteinMap() {
		return Index2ProteinMap;
	}

}

