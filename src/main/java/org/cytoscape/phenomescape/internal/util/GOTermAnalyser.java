package org.cytoscape.phenomescape.internal.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.cytoscape.model.CyNode;
import org.cytoscape.phenomescape.internal.PhenomeExpressSubnetwork;
import org.cytoscape.phenomescape.internal.Protein;
import org.cytoscape.phenomescape.internal.ProteinNetwork;

public class GOTermAnalyser {
	
	public GOTermAnalyser(ProteinNetwork proteinNetwork,String species) throws FileNotFoundException{
		readinAnnotations(proteinNetwork,species);
	}
	
	private Map<String,List<GOTerm>> proteinName2GOTerms = new HashMap<String,List<GOTerm>>();
	private Map<String,GOTerm> annotatedGOTerm = new HashMap<String,GOTerm>();
	
	
	
	public void calculateGOTermPValues(PhenomeExpressSubnetwork subnetwork,ProteinNetwork proteinNetwork) throws NotPositiveException, NotStrictlyPositiveException, NumberIsTooLargeException, Exception{

		Map<GOTerm, Integer> subnetworkGoTerms = goTermsinSubnetwork(proteinNetwork,subnetwork);
		int networkSize = proteinNetwork.getNodeCount();
		
		double minProb= 2.0;
		GOTerm bestGOTerm = null;
		for (GOTerm goterm : subnetworkGoTerms.keySet()){
			HypergeometricDistribution hypergeometricDistribution = new HypergeometricDistribution(networkSize,goterm.getNumAnnotation(),subnetwork.getNodeList().size());
			double prob = 1.0 - hypergeometricDistribution.cumulativeProbability(subnetworkGoTerms.get(goterm));
			if (prob< minProb){
				minProb=prob;
				bestGOTerm = goterm;
			}
			else if (prob == minProb){
				if (subnetworkGoTerms.get(goterm) > subnetworkGoTerms.get(bestGOTerm)){
					minProb=prob;
					bestGOTerm = goterm;
				}
			}
		}
		
		subnetwork.setBestGOTerm(bestGOTerm);
		subnetwork.setGoTermPvalue(minProb);
		if (bestGOTerm==null){
			throw new Exception("No matching GOTerms - Wrong species selected?");
		}
			
	}
	
	
	private Map<GOTerm, Integer> goTermsinSubnetwork(ProteinNetwork proteinNetwork, PhenomeExpressSubnetwork subnetwork){
		Collection<CyNode> nodes = subnetwork.getNodeList();
		List<GOTerm> goTerms = new ArrayList<GOTerm>();
		for(CyNode node :nodes){
			Protein protein = subnetwork.getNode2Protein().get(node);
			List<GOTerm> proteinGOTerms = proteinName2GOTerms.get(protein.getName());
			if (proteinGOTerms!=null) {
				goTerms.addAll(proteinName2GOTerms.get(protein.getName()));
			}
		}
		Map<GOTerm,Integer> counts = new LinkedHashMap<GOTerm,Integer>();
		for (GOTerm goterm : goTerms){
			Integer sum = counts.get(goterm);
			if (!counts.containsKey(goterm)){
				counts.put(goterm, 1);
			}
			else{
				counts.put(goterm,counts.get(goterm)+1);
			}
		}
		
		return(counts);	
	}
	

	private void readinAnnotations(ProteinNetwork proteinNetwork,String species) throws FileNotFoundException{
		
		BufferedReader in = null;
		InputStream inputstream;
		if(species.equals("Human")){
			inputstream = GOTermAnalyser.class.getResourceAsStream("/GO.tab");
		}
		else{
			inputstream = GOTermAnalyser.class.getResourceAsStream("/GO_Mouse.tab");
		}
		in = new BufferedReader(new InputStreamReader(inputstream));
		Set<String> proteinNames = proteinNetwork.getName2IndexMap().keySet();
		String line = "";
		try {
			while ((line = in.readLine()) != null) {
				String parts[] = line.split("\t");
				if (proteinNames.contains(parts[0])){
					GOTerm goTerm = annotatedGOTerm.get(parts[1]);
					if(goTerm==null){
						goTerm = new GOTerm(parts[1],parts[2]);
						annotatedGOTerm.put(parts[1],goTerm);
					}
					List<GOTerm> list = proteinName2GOTerms.get(parts[0]);
					if (list == null){
						list = new ArrayList<GOTerm>();
						list.add(goTerm);
						proteinName2GOTerms.put(parts[0], list);
					}
					else{
						if(!list.contains(goTerm)) list.add(goTerm);
					}
					goTerm.addAnnotation(parts[0]);
				}
				
				
			}
			
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	

}


