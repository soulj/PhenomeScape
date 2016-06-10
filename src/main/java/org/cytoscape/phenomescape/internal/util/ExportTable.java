package org.cytoscape.phenomescape.internal.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class ExportTable {

public static void toTSV(JTable summaryTable,JTable parameterTable, File file) throws Exception{
    try{
        TableModel summaryModel = summaryTable.getModel();
        TableModel parameterModel = parameterTable.getModel();
        FileWriter outFile = new FileWriter(file);
        writeTable(summaryModel,outFile);
        outFile.write("\n");   
        writeTable(parameterModel,outFile);
     
        outFile.close();

    }catch(IOException e){ 
    	throw new Exception("Problem writing out table file!");
    }
}




public static void writeTable (TableModel model,FileWriter outFile) throws IOException {
	
    for(int i = 0; i < model.getColumnCount(); i++){
    	outFile.write(model.getColumnName(i) + "\t");
    }

    outFile.write("\n");

    for(int i=0; i< model.getRowCount(); i++) {
        for(int j=0; j < model.getColumnCount(); j++) {
        	outFile.write(model.getValueAt(i,j).toString()+"\t");
        }
        outFile.write("\n");
    }
}
}