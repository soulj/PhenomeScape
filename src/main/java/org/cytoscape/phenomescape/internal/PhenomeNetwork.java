package org.cytoscape.phenomescape.internal;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.phenomescape.internal.util.ImportPhenotypes;
import org.cytoscape.service.util.CyServiceRegistrar;

import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;



public class PhenomeNetwork implements Serializable {
	

	private static final long serialVersionUID = 1L;

    	private Map<String,Integer> Name2IndexMap;
		private SparseRowMatrix adjMatrix;
		


		public CyNetwork initilise (ControlPanel controlPanel){
			
			
			CyServiceRegistrar cyServiceRegistar = controlPanel.cyServiceRegistrar;
			CyNetworkFactory networkFactory = cyServiceRegistar.getService(CyNetworkFactory.class);
			CyNetwork network = networkFactory.createNetwork();

			network.getRow(network).set(CyNetwork.NAME, "PhenomeNetwork");
			network.getDefaultEdgeTable().createColumn("SemanticSimilarity",Double.class,false);

			
			try {
				readIn(network);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			createSparseAdjMatrix(network);
			
			return(network);
			


		}
		public void save() {
			try{
				FileOutputStream fileOut = new FileOutputStream("/home/mqbpkjs2/PhenomeScape0.8/Phenome2.ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(this);
				out.close();
				fileOut.close();
			}
			catch(IOException i){
				i.printStackTrace();
										
			}
			
		}
		public PhenomeNetwork load(){
			PhenomeNetwork e = null;
			try {
				InputStream fileIn1= PhenomeNetwork.class.getResourceAsStream("/Phenome2.ser");
				ObjectInputStream in = new ObjectInputStream(fileIn1);
				e = (PhenomeNetwork) in.readObject();
				in.close();
				fileIn1.close();
				
			} catch (IOException e1) {
				e1.printStackTrace();
				
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			return e;
			
			
			
			
			
		}


		private void readIn(CyNetwork network) throws FileNotFoundException {
			Map<String, CyNode>  nodeNameMap = new HashMap<String, CyNode>() ;
			Scanner sc = new Scanner(new FileReader("/home/mqbpkjs2/PhenomeScape0.8/PhenotypeEdgeList"));
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
				if (!network.containsEdge(node1, node2)){
					
					CyEdge myEdge =network.addEdge(node1, node2, false);
					network.getRow(myEdge).set("interaction", "phenotype");
					network.getRow(myEdge).set("SemanticSimilarity", Double.parseDouble(nodes[2]));
					network.getRow(myEdge).set("name", nodes[0]+ " (phenotype) " +nodes[1]);
				
				}
				
			}
			sc.close();
		}
		
		 public void createSparseAdjMatrix(CyNetwork network) {
		    	
		    
		    	int totalnodecount = network.getNodeList().size();
		    	HashMap<CyNode, Integer> cyNodeMap = new HashMap<CyNode, Integer>();
		    	Name2IndexMap= new HashMap<>();
		    	CyTable edgeTable = network.getDefaultEdgeTable();
		    	CyTable nodeTable = network.getDefaultNodeTable();
		   
		    	adjMatrix = SparseRowMatrix.create(totalnodecount,totalnodecount);
		    	List<CyNode> cyNodes = network.getNodeList();

		    	int i =0;
		    	for (CyNode node: cyNodes){
		    		
		    		cyNodeMap.put(node,i);
		    		Name2IndexMap.put(nodeTable.getRow(node.getSUID()).get("name",String.class),i);
		    		i++;
		    		
		    	}
		    	List<CyEdge> edgeList = network.getEdgeList();
		    	
		    	for ( CyEdge edge : edgeList){
		   		    		
		       		
		    		CyRow row = edgeTable.getRow(edge.getSUID());		    		
		    			    		
		    		adjMatrix.set(cyNodeMap.get(edge.getSource()),cyNodeMap.get(edge.getTarget()),row.get("SemanticSimilarity",Double.class));
		    		adjMatrix.set(cyNodeMap.get(edge.getTarget()),cyNodeMap.get(edge.getSource()),row.get("SemanticSimilarity",Double.class));
		    		

		    		
		    		}

		    	

		}

		public AMatrix normalise(double transitionProb,PhenoGeneNetwork phenoGeneNetwork){
			Collection<Integer> annotated = phenoGeneNetwork.getPhenotypeName2IndexMap().values();
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

		public Map<String, Integer> getName2IndexMap() {
			return Name2IndexMap;
		}


		public mikera.matrixx.impl.SparseRowMatrix getAdjMatrix() {
			return adjMatrix;
		}
		

	}



