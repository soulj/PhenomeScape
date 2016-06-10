package org.cytoscape.phenomescape.internal;

import java.util.Comparator;

import org.cytoscape.model.CyNode;

public class Protein  {
	
	private CyNode node;
	private Integer index;
	private String name;
	private double value;
	private int rank;
	private double score;
	
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Protein(int i, String name1, CyNode node2,Double score) {
		this.index=i;
		this.name=name1;
		this.node=node2;
		this.score=score;
		
	}
	public Protein() {
		
	}
	public CyNode getNode() {
		return node;
	}
	public void setNode(CyNode node) {
		this.node = node;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	 static class DescendingScoreComparator implements Comparator <Protein> {
		    @Override
		    public int compare(Protein p1, Protein p2) {
		      return Double.compare(p2.getValue(), p1.getValue());
		      }
		  }
	 
	 static class RankComparator implements Comparator <Protein> {
		    @Override
		    public int compare(Protein p1, Protein p2) {
		      return Integer.compare(p2.getRank(), p1.getRank());
		    }
		  }
	 
	 static class ProteinByNodeComparator implements Comparator <Protein> {
		 public int compare(Protein p1, Protein p2) {
			 
			 return Long.compare(p1.getNode().getSUID(), p2.getNode().getSUID());
	          
	        }
	      };
	      
	      
	 	 

	public void setRank(int rank) {
		this.rank=rank;
		
	}
	public int getRank() {
		return rank;
	}
	


}
