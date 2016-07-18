package org.cytoscape.phenomescape.internal.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import org.cytoscape.phenomescape.internal.ResultsPanel;


public class ForwardTableListener implements ActionListener {

		private ResultsPanel resultsPanel;
		private int index;
		private ArrayList<DefaultTableModel> model;
		private ArrayList<DefaultTableModel> parameterModel;
		
		public ForwardTableListener (ResultsPanel resultsPanel) {
			this.resultsPanel = resultsPanel;
			
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			this.index = resultsPanel.getCurrentIndex();		
			model = resultsPanel.getTableModelList();
			parameterModel = resultsPanel.getParameterTableModelList();
			int listSize=model.size();
			if (index+2 <= listSize){
				resultsPanel.setTableData(model.get(index+1),parameterModel.get(index+1));
				resultsPanel.setCurrentIndex(index+1);
				resultsPanel.summaryTableColumnResizer.adjustColumns();
				resultsPanel.parameterTableColumnResizer.adjustColumns();
			}
		
			
		}
	}

