package org.cytoscape.phenomescape.internal.util;


import java.util.Hashtable;
import java.util.Set;

import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

public class TableManager {
	
	public CyServiceRegistrar cyServiceRegistrar;
	public static CyTableManager cyTableManager;
	public static Hashtable<String, Long> tableRefs = new Hashtable<String, Long>();


	
	public TableManager(CyServiceRegistrar cyServiceRegistrar) {
		this.cyServiceRegistrar = cyServiceRegistrar;
		TableManager.cyTableManager = cyServiceRegistrar.getService(CyTableManager.class);
		initilseGlobalTables();
	}


	public static void initilseGlobalTables() {
		
		tableRefs.clear();
		Set<CyTable> myTable = cyTableManager.getGlobalTables();
		for (CyTable table : myTable) {
			String title = table.getTitle();
			Long suid = table.getSUID();
			tableRefs.put(title, suid);
			
		}
		
		
	}
	
	public static Set<String> getGlobalTableTitles(){
		
		
		return (tableRefs.keySet());
	}
	
	public static long getGlobalTableSUID(String title){
		return(TableManager.tableRefs.get(title));
				
	}


	
}
	


