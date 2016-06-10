package org.cytoscape.phenomescape.internal.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import org.cytoscape.phenomescape.internal.ResultsPanel;


public class BackwardsTableListener  implements ActionListener {

		private ResultsPanel resultsPanel;
		private int index;
		private ArrayList<DefaultTableModel> model;
		private ArrayList<DefaultTableModel> parameterModel;
		
		public BackwardsTableListener (ResultsPanel resultsPanel) {
			this.resultsPanel = resultsPanel;
			this.index = resultsPanel.getCurrentIndex();
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			this.index = resultsPanel.getCurrentIndex();	
			model = resultsPanel.getTableModelList();
			parameterModel = resultsPanel.getParameterTableModelList();
			if (index > 0){
				resultsPanel.setTableData(model.get(index-1),parameterModel.get(index-1));
				resultsPanel.setCurrentIndex(index-1);
			}
		
			
		}
	}




