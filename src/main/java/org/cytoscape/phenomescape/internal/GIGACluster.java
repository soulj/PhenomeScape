package org.cytoscape.phenomescape.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;

public class GIGACluster {

	private ArrayList<CyNode> cluster;
	private ArrayList<CyNode> neighbourList;
	private ArrayList<CyNode> nodesJustAdded;
	private ArrayList<CyNode> nodesAddedOnLastIteration;
	private CyNode localMin;
	private int netSize;

	public ArrayList<CyNode> getNeighbourList() {
		return neighbourList;
	}

	public void setNeighbourList(ArrayList<CyNode> neighbourList) {
		this.neighbourList = neighbourList;
	}

	private ArrayList<CyNode> newNeighbours;
	private int size;
	private double pvalue = 1.0;
	private int currentRank;
	private Boolean completed = Boolean.FALSE;
	private ProteinNetwork proteinNetwork;


	public int getCurrentRank() {
		return currentRank;
	}

	public void setCurrentRank(int currentRank) {
		this.currentRank = currentRank;
	}


	public ArrayList<CyNode> getNewNeighbours() {
		return newNeighbours;
	}

	public void setNewNeighbours(ArrayList<CyNode> newNeighbours) {
		this.newNeighbours = newNeighbours;
	}



	public GIGACluster(ArrayList<CyNode> cluster, int size,int currentRank,ProteinNetwork proteinNetwork ){
		this.cluster=cluster;
		this.localMin=cluster.get(0);
		this.size=1;
		this.neighbourList=new ArrayList();
		this.newNeighbours=new ArrayList();
		this.nodesJustAdded=new ArrayList();
		this.nodesAddedOnLastIteration=new ArrayList();
		this.currentRank = currentRank;
		this.proteinNetwork = proteinNetwork;
		this.netSize=proteinNetwork.getNetwork().getNodeCount();
	}

	public CyNode getLocalMin() {
		return localMin;
	}

	public int getNetSize() {
		return netSize;
	}

	public ProteinNetwork getProteinNetwork() {
		return proteinNetwork;
	}

	public void setLocalMin(CyNode localMin) {
		this.localMin = localMin;
	}

	public void setCluster(ArrayList<CyNode> cluster) {
		this.cluster = cluster;
	}

	public ArrayList<CyNode> getCluster() {
		return cluster;
	}


	public int getSize() {
		return cluster.size();
	}

	public double getPvalue() {
		return pvalue;
	}



	public void setSize(int size) {
		this.size = size;
	}

	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}

	public ArrayList<CyNode> getNeighbours(ProteinNetwork network) {

		for (CyNode node:this.nodesAddedOnLastIteration){
			ArrayList<CyNode> neighbours;			
			neighbours = (ArrayList<CyNode>) network.getNetwork().getNeighborList(node,Type.ANY);
			for (CyNode neighbour:neighbours){
				if (!neighbourList.contains(neighbour)) {
					neighbourList.add(neighbour);

				}					
			}

		}

		return neighbourList;


	}

	public void setNodesJustAdded(ArrayList<CyNode> nodesJustAdded) {
		this.nodesJustAdded = nodesJustAdded;
	}

	public void setNodesAddedOnLastIteration(
			ArrayList<CyNode> nodesAddedOnLastIteration) {
		this.nodesAddedOnLastIteration = nodesAddedOnLastIteration;
	}

	public void setNetSize(int netSize) {
		this.netSize = netSize;
	}

	public void setProteinNetwork(ProteinNetwork proteinNetwork) {
		this.proteinNetwork = proteinNetwork;
	}

	public void getNewNeighbours(ProteinNetwork network,HashMap<CyNode,Protein> node2Protein) {
		for (CyNode node:nodesJustAdded){
		
			ArrayList<CyNode> neighbours;			
			neighbours = (ArrayList) network.getNetwork().getNeighborList(node,Type.ANY);
			for (CyNode neighbour:neighbours){
				if (!newNeighbours.contains(neighbour) && !cluster.contains(neighbour)) {
					newNeighbours.add(neighbour);
				

				}					
			}

		}


	}

	public void revert(int size,double pvalue) {
	

		cluster = new ArrayList<CyNode>(cluster.subList(0, size-1));
		this.pvalue=pvalue;
		this.size=size;
		setCompleted(Boolean.TRUE);

	}

	public Boolean getCompleted() {
		
		return completed;
	}

	public void setCompleted(Boolean completed) {
		
		this.completed=completed;
	}

	public void computePValue(){

			double prob = (double) currentRank/ (double) netSize;
	
			for (int i = 1; i < this.getCluster().size(); i++) {
				prob = (double)(prob * (currentRank - i)) / (double) (netSize - i);
	
			}
			this.pvalue=prob;
		}
		
		

	




public ArrayList<CyNode> getNodesJustAdded() {

	return this.nodesJustAdded;
}

public ArrayList<CyNode> getNodesAddedOnLastIteration() {

	return nodesAddedOnLastIteration;
}




}
