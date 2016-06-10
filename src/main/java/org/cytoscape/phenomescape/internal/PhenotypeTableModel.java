package org.cytoscape.phenomescape.internal;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.cytoscape.phenomescape.internal.util.ImportPhenotypes;
import org.cytoscape.phenomescape.internal.util.Phenotype;

public class PhenotypeTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayList<Phenotype> phenotypes;
		
	private String[] columnNames = { "Phenotype ID","Phenotype Name","Selected" };

	PhenotypeTableModel(){	 
		try {
			this.phenotypes=ImportPhenotypes.importData();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}
	}

	@Override
	public int getRowCount() {
		return phenotypes.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		Object value = "??";
		Phenotype phenotype = phenotypes.get(rowIndex);
		switch (columnIndex) {
		case 0:
			value = phenotype.getID();
			break;
		case 1:
			value = phenotype.getName();
			break;
		case 2:
			value = phenotype.getSelected();
			break;
		}
		return value;

	}


	 public Class getColumnClass(int column) {
		    return (getValueAt(0, column).getClass());
		  }

	public Phenotype getPhenotypeAt(int row) {
		return phenotypes.get(row);
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	    if (columnIndex == 2) {
	    	Phenotype phenotype = phenotypes.get(rowIndex);
	    	phenotype.setSelected((Boolean) aValue);
	        fireTableCellUpdated(rowIndex, columnIndex);
	    }
	}

	  public boolean isCellEditable(int row, int column) {
		    return column == 2;
		  	}
		  

	  @Override
	  public String getColumnName(int index) {
	      return columnNames[index];
	  }




}
