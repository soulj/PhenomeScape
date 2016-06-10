package org.cytoscape.phenomescape.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class GIGA {

	private List<Protein> proteinList;
	private ProteinNetwork proteinNetwork;
	private HashMap<CyNode, Protein> node2Protein;
	private CyNodeRankComparator comp1;
	private ArrayList<GIGACluster> clusters;
	private CyNetwork network;
	private int maxSize;
	private ArrayList<CyNode> localMinList;


	public ArrayList<GIGACluster> getClusters() {
		return clusters;
	}

	public void setClusters(ArrayList<GIGACluster> clusters) {
		this.clusters = clusters;
	}

	public GIGA(List<Protein> proteinList,HashMap<CyNode, Protein> node2Protein,ProteinNetwork proteinNetwork, int maxSize) {
		this.proteinList=proteinList;
		this.node2Protein = node2Protein;
		this.comp1 = new CyNodeRankComparator(this.node2Protein);
		this.proteinNetwork = proteinNetwork;
		this.network= proteinNetwork.getNetwork();
		this.maxSize = maxSize;
	}

	public void compute(){
		initiliseClusters();
		
		Iterator<GIGACluster> it = clusters.iterator();

		while (it.hasNext()){
			GIGACluster cluster = it.next();
			while (cluster.getCompleted()==Boolean.FALSE){
				addNewMin(cluster);
				expandClusters(cluster);

			}



		}
		
		finalClusters();

	}



	private void finalClusters() {
		//remove the clusters with pvalues that are too large
		Iterator<GIGACluster> it2 = clusters.iterator();
		double threshold = 1.0/(double)  network.getNodeCount();
		while (it2.hasNext()){
			GIGACluster cluster = it2.next();
			if( cluster.getPvalue()> threshold){
				it2.remove();
			}
		}
		//order the clusters by pvalue
		Collections.sort(clusters,new GIGAClusterPvalueComparator());
		
		for (GIGACluster cluster : clusters){
		ArrayList<CyNode> nodes = cluster.getCluster();
		System.out.println("new cluster");
		CyNode node = cluster.getLocalMin();
		Integer id = proteinNetwork.getNode2IndexMap().get(node);
		String name = proteinNetwork.getIndex2ProteinMap().get(id).getName();
		System.out.println("localmin=" + name );
		System.out.println("pvalue=" + cluster.getPvalue() );
		for (CyNode node1: nodes){
			id = proteinNetwork.getNode2IndexMap().get(node1);
			name = proteinNetwork.getIndex2ProteinMap().get(id).getName();
			System.out.println(name);			
		}
		
	}
	
		
		
		//iterate through the clusters and keep a track of the local min in each cluster
		//if that local min is used then remove that cluster
		Iterator<GIGACluster> it = clusters.iterator();
		ArrayList<CyNode> localMinUsed = new ArrayList<CyNode>();
		while (it.hasNext()){
			GIGACluster cluster = it.next();
			if (!localMinUsed.contains(cluster.getLocalMin())){
				for (CyNode node : cluster.getCluster()){
					if (localMinList.contains(node)){
						localMinUsed.add(node);
					}
				}
			}
			else{
				it.remove();
				
			}
			
		}
		
		
		
	}



	static class ProteinToNodeComparator implements Comparator <Object> {
		public int compare(Object p1, Object p2) {

			Protein protein;
			CyNode node;

			if(p1 instanceof Protein){

				protein = (Protein) p1;

			}
			else{
				throw new ClassCastException();
			}

			if (p2 instanceof CyNode){
				node = (CyNode) p2;
			}
			else{
				throw new ClassCastException();
			}

			if(node == null || protein == null){
				throw new NullPointerException();
			}

			return Long.compare(protein.getNode().getSUID(), node.getSUID());

		}
	};


	class CyNodeRankComparator implements Comparator<CyNode>{

		private HashMap<CyNode,Protein> node2Protein;

		public CyNodeRankComparator(HashMap<CyNode,Protein> node2Protein){

			this.node2Protein = node2Protein;
		}

		@Override
		public int compare(CyNode o1, CyNode o2) {

			int node1Rank = (int) node2Protein.get(o1).getRank();
			int node2Rank = (int)  node2Protein.get(o2).getRank();
			return Integer.compare(node1Rank, node2Rank);

		}


	}
	
	class GIGAClusterPvalueComparator implements Comparator<GIGACluster>{

		@Override
		public int compare(GIGACluster o1, GIGACluster o2) {
			return Double.compare(o1.getPvalue(),o1.getPvalue());

		}


	}

	private void findLocalMinima(){
		localMinList = new ArrayList<CyNode>();


		for (Protein protein:proteinList){
			List<CyNode> neighbours = network.getNeighborList(protein.getNode(), Type.ANY);
			neighbours.add(protein.getNode());
			Collections.sort(neighbours, comp1);
			CyNode node = neighbours.get(0);

			if (node.equals(protein.getNode())){
				localMinList.add(node);	
				
			}

		}
		
	}

	private void initiliseClusters(){
		clusters = new ArrayList<GIGACluster>();		
		
		findLocalMinima();
		
		//get the neigbours and add the node with the next lowest Rank
		for (CyNode localMin : localMinList){
			ArrayList<CyNode> nodeList=new ArrayList<CyNode>();
			nodeList.add(localMin);
			List<CyNode> neighbours = network.getNeighborList(localMin, Type.ANY);
			Collections.sort(neighbours, comp1);
			nodeList.add(neighbours.get(0));
			GIGACluster  cluster = new GIGACluster(nodeList,2,node2Protein.get(neighbours.get(0)).getRank(),proteinNetwork);
			cluster.getNodesJustAdded().add(localMin);
			cluster.getNodesJustAdded().add(neighbours.get(0));
			cluster.getNodesAddedOnLastIteration().add(localMin);
			cluster.getNodesAddedOnLastIteration().add(neighbours.get(0));

			expandClusters(cluster);
			clusters.add(cluster);

		}

	}



	private void addNewMin(GIGACluster cluster){
		ArrayList<CyNode> candidates = cluster.getNeighbours(proteinNetwork);
		cluster.getNodesAddedOnLastIteration().clear();
		Collections.sort(candidates, comp1);
		
		int oldRank = cluster.getCurrentRank();
		CyNode candidate = candidates.get(cluster.getSize());
	
		cluster.getCluster().add(candidate);
		cluster.setCurrentRank(node2Protein.get(candidate).getRank());
		if (cluster.getCurrentRank()==oldRank){
		
		}
		cluster.getNodesJustAdded().add(candidate);
		cluster.getNodesAddedOnLastIteration().add(candidate);
		



	}

	private void expandClusters(GIGACluster cluster){
		int size = cluster.getSize();
		int added=1;
		double previousPValue=cluster.getPvalue();

		while(added>0 && cluster.getSize() < maxSize){
			added=0;
			cluster.getNewNeighbours().clear();
			//get the neighbours
			cluster.getNewNeighbours(proteinNetwork,node2Protein);

			//current max rank
			int currentRank=cluster.getCurrentRank();

			//want to add Neighbours that have < rank than current rank
			for (CyNode neighbour:cluster.getNewNeighbours()){
				
				int rank = node2Protein.get(neighbour).getRank();
				if (rank<currentRank){
					cluster.getCluster().add(neighbour);
					cluster.getNodesJustAdded().add(neighbour);
					cluster.getNodesAddedOnLastIteration().add(neighbour);
					added++;
				}
			}
		}
		cluster.getNodesJustAdded().clear();
		if (cluster.getSize() >= maxSize){
			cluster.revert(size,cluster.getPvalue());
		}
		else{
			cluster.computePValue();
			if (cluster.getPvalue() > previousPValue){
				cluster.revert(size,previousPValue);			
			}
	
		}


	}

}
