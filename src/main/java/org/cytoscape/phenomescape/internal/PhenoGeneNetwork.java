package org.cytoscape.phenomescape.internal;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Iterator;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;



public class PhenoGeneNetwork {

	private CyNetwork network;
	private Map<String, CyNode> nodeNameMap;
	private HashMap <String,CyNode> proteinNameMap;		
	private HashMap <String,CyNode> phenotypeNameMap;	
	private  SparseRowMatrix bipartiteAdjMatrix;
	private Map<CyNode,Integer> Node2IndexMap;
	private Map<String,Integer> Name2IndexMap;
	private HashMap<CyNode, Integer> PhenotypeNode2IndexMap;
	private HashMap<String, Integer> PhenotypeName2IndexMap;
	private  HashMap<String, Integer>  GeneName2IndexMap;
	private HashMap<CyNode, Integer> GeneNode2IndexMap;
	private List<CyEdge> edgeList;;
	private CyTable edgeTable ;
	private boolean proteinsNotAnnotated;
	private SparseRowMatrix bipartiteAdjMatrixT;
	private String species;


	public HashMap<String, CyNode> getProteinNameMap() {
		return proteinNameMap;
	}

	public HashMap<String, CyNode> getPhenotypeNameMap() {
		return phenotypeNameMap;
	}

	public CyNetwork getNetwork(){
		return(this.network);
	}

	public Map<String, CyNode> getNodeNames(){
		return(this.nodeNameMap);
	}

	public PhenoGeneNetwork (ControlPanel controlPanel, String species) throws FileNotFoundException{

		CyServiceRegistrar cyServiceRegistar = controlPanel.cyServiceRegistrar;
		CyNetworkFactory networkFactory = cyServiceRegistar.getService(CyNetworkFactory.class);


		network  = networkFactory.createNetwork();
		
		this.species = species;


		network.getRow(network).set(CyNetwork.NAME, "PhenoGeneNetwork");

		this.readIn();

		edgeList = network.getEdgeList();
		edgeTable = network.getDefaultEdgeTable();


	}
	public void readIn() throws FileNotFoundException{
		//read in the edge list
		Scanner sc;
		if (species.equals("Human")){
			sc= new Scanner(PhenoGeneNetwork.class.getResourceAsStream("/GenePhenoEdgeList"));	
		}		
		else{
			sc= new Scanner(PhenoGeneNetwork.class.getResourceAsStream("/GenePhenoEdgeListMouse"));	
		}
		nodeNameMap = new HashMap<String, CyNode>();
		proteinNameMap = new HashMap<String, CyNode>();
		phenotypeNameMap = new HashMap<String, CyNode>();
		while (sc.hasNextLine()){
			String line = sc.nextLine();
			String [] nodes = line.split("\t");
			CyNode node1 = null;
			CyNode node2 = null;
			// for Node1
			if (nodeNameMap.containsKey(nodes[0])){
				
				node1 = (CyNode) nodeNameMap.get(nodes[0]);
			}
			else {
				
				node1 = network.addNode();
				CyRow attributes = network.getRow(node1);
				attributes.set("name", nodes[0]);
				nodeNameMap.put(nodes[0], node1);
				phenotypeNameMap.put(nodes[0], node1);				
			}
			if (nodeNameMap.containsKey(nodes[1])){
				
				node2 = (CyNode) nodeNameMap.get(nodes[1]);
			}
			else {
				
				node2 = network.addNode();
				CyRow attributes = network.getRow(node2);
				attributes.set("name", nodes[1]);
				nodeNameMap.put(nodes[1], node2);
				proteinNameMap.put(nodes[1], node2);
			}
			if (!network.containsEdge(node1, node2)){
				
				CyEdge myEdge =network.addEdge(node1, node2, true);
				network.getRow(myEdge).set("interaction", "phenotype");
				network.getRow(myEdge).set("name", nodes[0]+ " (phenotype) " +nodes[1]);
			}
			


		}

		sc.close();

	}

	public AMatrix normalise(double transitionProb){
		
		AMatrix matrix = bipartiteAdjMatrix.copy();
		for ( int i=0;i<matrix.rowCount();i++){
					AVector row=matrix.getRow(i);
					double sum=row.elementSum();
					if(sum>0){
						row=row.mutable();
						row.multiply(transitionProb);
						row.divide(sum);
						matrix.replaceRow(i,row);

					}			
		}
		return(matrix);
	}
	public AMatrix normaliseT(double transitionProb){
		AMatrix matrix = bipartiteAdjMatrixT.copy();
		for ( int i=0;i<matrix.rowCount();i++){
					AVector row=matrix.getRow(i);
					double sum=row.elementSum();
					if(sum>0){
						row=row.mutable();
						row.multiply(transitionProb);
						row.divide(sum);
						matrix.replaceRow(i,row);

					}			
		}
		return(matrix);
	}
		
			
	
	public void filter(Map<String, Integer> name2IndexMap) {
		List <CyNode> NodesToBeRemoved = new ArrayList<CyNode>();
		Iterator<String> it = proteinNameMap.keySet().iterator();
		while (it.hasNext()){
			String nodeName = it.next();
			Integer index= name2IndexMap.get(nodeName);
			if (index == null){				
				NodesToBeRemoved.add(proteinNameMap.get(nodeName));
				it.remove();
			}
			
		}

		

		List<CyEdge> edgesToBeRemoved = new ArrayList<CyEdge>();
		for (CyNode node:NodesToBeRemoved) {

			edgesToBeRemoved.addAll(network.getAdjacentEdgeList(node, CyEdge.Type.ANY));

		}

		this.network.removeNodes(NodesToBeRemoved);
		this.network.removeEdges(edgesToBeRemoved);
		List<Long> pNKeys = new ArrayList<>();


		for(CyNode n : NodesToBeRemoved){
			pNKeys.add(n.getSUID());
		}		
		List<Long> pEKeys = new ArrayList<>();
		for(CyEdge n : edgesToBeRemoved){
			pEKeys.add(n.getSUID());
		}	

		network.removeNodes(NodesToBeRemoved);
		network.removeEdges(edgesToBeRemoved);
		network.getDefaultNodeTable().deleteRows(pNKeys);
		network.getDefaultEdgeTable().deleteRows(pEKeys);
		edgeList=network.getEdgeList();


	}

	public void createSparseBiPartAdjMatrix(ProteinNetwork proteinNetwork,PhenomeNetwork phenomeNetwork) throws Exception {


		GeneNode2IndexMap= new HashMap<>();
		GeneName2IndexMap= new HashMap<>();
		PhenotypeNode2IndexMap= new HashMap<CyNode,Integer>();
		PhenotypeName2IndexMap= new HashMap<>();
		
			
		
		Iterator<String> it = proteinNameMap.keySet().iterator();
		while (it.hasNext()){
			String protein = it.next();
			Integer index=proteinNetwork.getName2IndexMap().get(protein);
			if (index!=null){
			GeneNode2IndexMap.put(proteinNameMap.get(protein),index);
			GeneName2IndexMap.put(protein,index);
			}
			else{
				it.remove();
			}
		}
		

		
		//System.out.println("Number of genes ="+ GeneName2IndexMap.size());
		if(GeneNode2IndexMap.isEmpty()){
			throw new Exception("No matching gene-phenotype associations - did you select the right species?");
		}
		

		//Build a Phenotype index to match the PhenomeNetwork
		for (String phenotype: phenotypeNameMap.keySet()){

			Integer index=phenomeNetwork.getName2IndexMap().get(phenotype);
			PhenotypeNode2IndexMap.put(phenotypeNameMap.get(phenotype),index);
			PhenotypeName2IndexMap.put(phenotype,index);
		}

		bipartiteAdjMatrix = SparseRowMatrix.create(phenomeNetwork.getName2IndexMap().size(),proteinNetwork.getName2IndexMap().size());
		bipartiteAdjMatrixT = SparseRowMatrix.create (proteinNetwork.getName2IndexMap().size(),phenomeNetwork.getName2IndexMap().size());


		for ( CyEdge edge : edgeList){
			if(GeneNode2IndexMap.get(edge.getTarget())!=null){
			bipartiteAdjMatrix.set(PhenotypeNode2IndexMap.get(edge.getSource()),GeneNode2IndexMap.get(edge.getTarget()),1.0);
			bipartiteAdjMatrixT.set(GeneNode2IndexMap.get(edge.getTarget()),PhenotypeNode2IndexMap.get(edge.getSource()),1.0);
			}
		}
		
		


	}

	public Map<String, CyNode> getNodeNameMap() {
		return nodeNameMap;
	}

	public SparseRowMatrix getBipartiteAdjMatrix() {
		return bipartiteAdjMatrix;
	}
	
	public SparseRowMatrix getBipartiteAdjMatrixT() {
		return bipartiteAdjMatrixT;
	}

	public Map<CyNode, Integer> getNode2IndexMap() {
		return Node2IndexMap;
	}

	public Map<String, Integer> getName2IndexMap() {
		return Name2IndexMap;
	}

	public HashMap<CyNode, Integer> getPhenotypeNode2IndexMap() {
		return PhenotypeNode2IndexMap;
	}

	public HashMap<String, Integer> getPhenotypeName2IndexMap() {
		return PhenotypeName2IndexMap;
	}

	public HashMap<String, Integer> getGeneName2IndexMap() {
		return GeneName2IndexMap;
	}

	public HashMap<CyNode, Integer> getGeneNode2IndexMap() {
		return GeneNode2IndexMap;
	}

	public List<CyEdge> getEdgeList() {
		return edgeList;
	}

	public CyTable getEdgeTable() {
		return edgeTable;
	}

	public boolean isProteinsNotAnnotated() {
		return proteinsNotAnnotated;
	}

	
	
}
