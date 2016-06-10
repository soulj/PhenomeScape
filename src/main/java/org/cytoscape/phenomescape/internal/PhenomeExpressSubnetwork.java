package org.cytoscape.phenomescape.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.phenomescape.internal.util.GOTerm;

public class PhenomeExpressSubnetwork {

	ArrayList<CyNode> nodeList = new ArrayList<CyNode>();
	CyNetwork network;
	public double subnetworkScore;
	private HashMap<CyNode, Protein> node2Protein;
	private double pvalue;
	private GOTerm bestGOTerm;
	private double goTermPvalue;
	private String subnetworkName;


	public String getBestGOTerm() {
		return bestGOTerm.getName();
	}



	public void setBestGOTerm(GOTerm bestGOTerm) {
		this.bestGOTerm = bestGOTerm;
	}



	public double getGoTermPvalue() {
		return goTermPvalue;
	}



	public void setGoTermPvalue(double goTermPvalue) {
		this.goTermPvalue = goTermPvalue;
	}



	public PhenomeExpressSubnetwork(HashMap<CyNode, Protein>  node2Protein){
		this.node2Protein=node2Protein;

	}



	public double getPvalue() {
		return pvalue;
	}



	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}



	public  Object sampleSubnetworks(CyNetwork network, int sampleSize){
		double [] scores = new double [sampleSize];

		for (int i = 0; i<sampleSize;i++){
			scores[i] = randomSubnetwork(network);
		}
		this.pvalue=calculatePValue(scores);
		System.out.println("the pvalue is ="+ pvalue);
		return(null);
	}

	private double randomSubnetwork(CyNetwork network){
		int netSize = nodeList.size();
		HashSet<CyNode> selected = new HashSet<CyNode>();
		ArrayList<CyNode> currentRandomNetwork = new ArrayList<CyNode>();
		List<CyNode> allNodes = network.getNodeList();
		CyNode start = allNodes.get(randInt(0,allNodes.size()-1));
		double score = node2Protein.get(start).getScore();
		selected.add(start);
		currentRandomNetwork.add(start);
		int i=1;
		while(i<netSize){
			//get a random node in the current random network
			CyNode chosen = currentRandomNetwork.get(randInt(0,currentRandomNetwork.size()-1));
			//get a random neighbour
			List<CyNode> neighbours = network.getNeighborList(chosen, Type.ANY);
			CyNode toAdd = neighbours.get(randInt(0,neighbours.size()-1));
			if(!selected.contains(toAdd)){
				selected.add(toAdd);
				currentRandomNetwork.add(toAdd);
				score += node2Protein.get(toAdd).getScore();
				i++;

			}
		}

		return score;


	}

	private double calculatePValue(double[] scores){
		int success=1;
		for (double score:scores){
			if (score>=subnetworkScore){
				success++;
			}
		}
		double pvalue= ((double)success)/ (double) scores.length;

		return (pvalue);
	}

	public ArrayList<CyNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(ArrayList<CyNode> nodeList) {
		this.nodeList = nodeList;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public void setNetwork(CyNetwork network) {
		this.network = network;
	}

	public double getSubnetworkScore() {
		return subnetworkScore;
	}

	public void setSubnetworkScore(double subnetworkScore) {
		this.subnetworkScore = subnetworkScore;
	}

	public HashMap<CyNode, Protein> getNode2Protein() {
		return node2Protein;
	}

	public void setNode2Protein(HashMap<CyNode, Protein> node2Protein) {
		this.node2Protein = node2Protein;
	}

	private int randInt(int min, int max){
		int randomNum = ThreadLocalRandom.current().nextInt(min,max+1);
		return randomNum;

	}



	public void setName(String subnetworkName) {
		this.subnetworkName=subnetworkName;

	}

	public String getSubnetworkName() {
		return subnetworkName;
	}

}
