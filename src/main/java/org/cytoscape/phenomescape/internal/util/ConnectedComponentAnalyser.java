package org.cytoscape.phenomescape.internal.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.phenomescape.internal.PhenomeExpressSubnetwork;
import org.cytoscape.phenomescape.internal.Protein;
import org.cytoscape.phenomescape.internal.ProteinNetwork;

import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.vectorz.AVector;

public class ConnectedComponentAnalyser {

	private CyNetwork network;
	private HashMap<CyNode,Protein> node2Protein;



	public ConnectedComponentAnalyser(CyNetwork network){
		this.network=network;

	}
	public ConnectedComponentAnalyser(HashMap<CyNode,Protein> node2Protein){
		this.node2Protein=node2Protein;

	}




	public ArrayList <PhenomeExpressSubnetwork> ccFromAdjMatrix(AMatrix cooccuranceMat,ProteinNetwork proteinNetwork) {
		
		
		HashMap<Integer, Protein> index2Protein = proteinNetwork.getIndex2ProteinMap();
				
		int notVisitedCount = cooccuranceMat.rowCount();
		Set<Integer> traversed = new HashSet<Integer>(notVisitedCount);
		ArrayList<PhenomeExpressSubnetwork> components = new ArrayList<PhenomeExpressSubnetwork>();

		for (int i=0;i<cooccuranceMat.rowCount();i++){
			if (cooccuranceMat.getRow(i).elementSum()>0){
				if (!traversed.contains(i)) {
					PhenomeExpressSubnetwork newComponent = traverseReachable(Integer.valueOf(i),index2Protein,cooccuranceMat, traversed);
					components.add(newComponent);
				}
			}
		}
			return components;
		}
		




	public Set<CCInfo> findComponents() {
		int notVisitedCount = network.getNodeCount();

		Set<CyNode> traversed = new HashSet<CyNode>(notVisitedCount);
		Set<CCInfo> components = new HashSet<CCInfo>();

		for ( CyNode node : network.getNodeList()) {
			if (!traversed.contains(node)) {
				Set<CyNode> currentComponentNodes = new HashSet<CyNode>();
				CCInfo newComponent = traverseReachable(node, traversed,currentComponentNodes);
				components.add(newComponent);
			}
		}
		return components;
	}



	public CCInfo findLargestComponent() {
		CCInfo largest = new CCInfo(0, null);
		final Set<CCInfo> comps = findComponents();
		for (CCInfo current : comps) {
			if (current.getSize() > largest.getSize()) {
				largest = current;
			}
		}
		return largest;
	}


	private PhenomeExpressSubnetwork traverseReachable(Integer index,HashMap<Integer, Protein> index2Protein, AMatrix matrix, Set<Integer> aTraversed){
		LinkedList<Integer> toTraverse = new LinkedList<Integer>();
		aTraversed.add(index);
		toTraverse.add(index);
		PhenomeExpressSubnetwork subnetwork = new PhenomeExpressSubnetwork(node2Protein);
		Protein protein = index2Protein.get(index);
		subnetwork.getNodeList().add(protein.getNode());
		subnetwork.setSubnetworkScore(subnetwork.getSubnetworkScore() + protein.getScore());
		while (!toTraverse.isEmpty()) {
			final Integer currentNode = toTraverse.removeFirst();
			final AVector neighbours = matrix.getRow(currentNode);
			for (int i=0;i<neighbours.length();i++) {
				if(neighbours.get(i)!=0.0){
					if (!aTraversed.contains(i)) {
						toTraverse.add(i);
						protein = index2Protein.get(i);
						subnetwork.getNodeList().add(protein.getNode());
						subnetwork.setSubnetworkScore(subnetwork.getSubnetworkScore() + protein.getScore());
						aTraversed.add(i);
					}
				}
			}
		}
		
		
		return subnetwork;
		
	}


	private CCInfo traverseReachable(CyNode aNode, Set<CyNode> aTraversed, Set<CyNode> currentComponentNodes) {
		int size = 1;
		LinkedList<CyNode> toTraverse = new LinkedList<CyNode>();
		aTraversed.add(aNode);
		toTraverse.add(aNode);
		currentComponentNodes.add(aNode);
		while (!toTraverse.isEmpty()) {
			CyNode currentNode = toTraverse.removeFirst();
			List<CyNode> neighbors = network.getNeighborList(currentNode, Type.ANY);
			for (CyNode nb : neighbors) {
				if (!aTraversed.contains(nb)) {
					size++;
					toTraverse.add(nb);
					currentComponentNodes.add(nb);
					aTraversed.add(nb);
				}
			}
		}
		return new CCInfo(size,currentComponentNodes);
	}
	
	

}
