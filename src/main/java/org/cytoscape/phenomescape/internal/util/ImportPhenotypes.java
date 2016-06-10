package org.cytoscape.phenomescape.internal.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class ImportPhenotypes {

	
	
	public static ArrayList<Phenotype> importData() throws FileNotFoundException {
		
		ArrayList<Phenotype> map = new ArrayList<Phenotype>();
		BufferedReader in = null;
		//InputStream inputstream= ImportPhenotypes.class.getResourceAsStream("phenotypes.tab");
		//FileInputStream inputstream = new FileInputStream("/home/mqbpkjs2/PhenomeScape0.8/phenotypes.tab");
		InputStream inputstream= ImportPhenotypes.class.getResourceAsStream("/phenotypes.tab");
		in = new BufferedReader(new InputStreamReader(inputstream));
		String line = "";
		try {
			while ((line = in.readLine()) != null) {
				String parts[] = line.split("\t");
				Phenotype phenotype = new Phenotype(parts[0], parts[1]);
				map.add(phenotype);
				
				//System.out.println(map.size());
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(map);

	}
}





