package org.cytoscape.phenomescape.internal.listeners;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JTable;

import org.cytoscape.phenomescape.internal.ResultsPanel;
import org.cytoscape.phenomescape.internal.util.ExportTable;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.phenomescape.internal.util.CytoPanelUtils;


public class ExportMouseListener implements ActionListener {

	private ResultsPanel resultsPanel;
	private JTable summaryjTable;
	private JTable parameterjTable;
	private FileChooserFilter filter ;
	private Collection<FileChooserFilter> filters = new ArrayList<FileChooserFilter>();

	public  ExportMouseListener (ResultsPanel resultsPanel) {
		this.resultsPanel = resultsPanel;
		this.filter=  new FileChooserFilter ("TXT files (.txt", "txt");
		this.filters.add(filter);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.summaryjTable = resultsPanel.getSummaryTable();
		this.parameterjTable = resultsPanel.getParameterTable();
		if (summaryjTable != null) {
			FileUtil fileUtil = resultsPanel.cyServiceRegistrar.getService(org.cytoscape.util.swing.FileUtil.class);
			File file = fileUtil.getFile(CytoPanelUtils.getJFrame(resultsPanel.cyServiceRegistrar),"Export Table",FileUtil.SAVE,filters);
			if (file != null) {
				try {
					ExportTable.toTSV(summaryjTable,parameterjTable,file );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
