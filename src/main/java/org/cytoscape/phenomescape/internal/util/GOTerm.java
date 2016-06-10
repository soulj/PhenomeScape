package org.cytoscape.phenomescape.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GOTerm {
	
	private String ID;
	private String Name;
	private HashSet<String> proteinNameAnnotated = new HashSet<String>();
	
	
	
	public HashSet<String> getProteinNameAnnotated() {
		return proteinNameAnnotated;
	}
	public void setProteinNameAnnotated(HashSet<String> proteinNameAnnotated) {
		this.proteinNameAnnotated = proteinNameAnnotated;
	}
	public GOTerm(String ID, String Name){
		this.ID=ID;
		this.Name=Name;
		
	}
	public GOTerm(String ID) {
		this.ID=ID;
	}
	public void addAnnotation(String proteinName){
		proteinNameAnnotated.add(proteinName);
	}
	public int getNumAnnotation() {
		return this.proteinNameAnnotated.size();
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}


}
