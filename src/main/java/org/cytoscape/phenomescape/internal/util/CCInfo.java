package org.cytoscape.phenomescape.internal.util;

import java.util.Set;

import org.cytoscape.model.CyNode;

public class CCInfo {
	
	private int ccSize;
	private Set<CyNode> nodes;

	public CCInfo(int ccSize, Set<CyNode> currentComponentNodes) {
		this.ccSize = ccSize;
		this.nodes = currentComponentNodes;
	}

	public int getSize() {
		return this.ccSize;
	}
	
	public Set<CyNode> getNodes() {
		return this.nodes;
	}
	
	

}
