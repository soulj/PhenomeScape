package org.cytoscape.phenomescape.internal;



import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.phenomescape.internal.listeners.BackwardsTableListener;
import org.cytoscape.phenomescape.internal.listeners.ExportMouseListener;
import org.cytoscape.phenomescape.internal.listeners.ForwardTableListener;
import org.cytoscape.service.util.CyServiceRegistrar;


public class ResultsPanel extends JPanel implements CytoPanelComponent{
	
	private static final long serialVersionUID = -4370789481040928750L;
	public CyServiceRegistrar cyServiceRegistrar;
	public JTable summaryTable;
	public DefaultTableModel model;
	public JScrollPane pane;
	public JButton exportButton;
	private FlowLayout layout;
	private BasicArrowButton rightButton;
	private BasicArrowButton leftButton;
	private ArrayList<DefaultTableModel> tableModelList;
	private ArrayList<DefaultTableModel> parameterTableModelList;
	private int currentIndex = -1;
	private JTable parameterTable;
	private JScrollPane parameterPane;
	private DefaultTableModel parameterModel;
	
	public ResultsPanel (CyServiceRegistrar cyServiceRegistrar ) {
		this.cyServiceRegistrar = cyServiceRegistrar;

		createPanelLayout();
		rightButton = new BasicArrowButton(BasicArrowButton.EAST);
		rightButton.putClientProperty("JComponent.sizeVariant", "large");
		
		leftButton = new BasicArrowButton(BasicArrowButton.WEST);
		leftButton.putClientProperty("JComponent.sizeVariant", "large");
		this.add(leftButton);
		this.add(rightButton);

		rightButton.addActionListener(new ForwardTableListener(this));
		leftButton.addActionListener(new BackwardsTableListener(this));
		
		exportButton = new JButton("Export Table");
  	    initialiseExportButton();
  	    this.add(exportButton);
		summaryTable = createTable();
		pane = new JScrollPane();
	    pane.setViewportView(summaryTable);
  	    this.add(pane);
  	    parameterTable = createParameterTable();
		parameterPane = new JScrollPane();
		parameterPane.setViewportView(parameterTable);
	    this.add(parameterPane);
  	    

  	   setTableModelList(new ArrayList<DefaultTableModel> ());
  	   setParameterTableModelList(new ArrayList<DefaultTableModel> ());

		this.setVisible(true);
		
	}
	
		
			
	
	private void createPanelLayout () {

	this.layout = new FlowLayout();
	setLayout(layout);
	setSize(new Dimension(450, 200));
	setPreferredSize(new Dimension(450, 200));
	}

	
	private JTable createTable () {
		model = new DefaultTableModel();
		model.addColumn("Network Number");
		model.addColumn("pvalue");
		summaryTable = new JTable(model);
		return (summaryTable);
		
	}
	
	private JTable createParameterTable () {
		parameterModel = new DefaultTableModel();
		parameterModel.addColumn("Parameter");
		parameterModel.addColumn("Value");
		parameterTable = new JTable(parameterModel);
		return (parameterTable);
		
	}
	
	private void initialiseExportButton () {
		exportButton.addActionListener(new ExportMouseListener(this));
	}



	@Override
	public Component getComponent() {
	return this;
	}
	@Override
	public CytoPanelName getCytoPanelName() {
	return CytoPanelName.EAST;
	}
	@Override
	public Icon getIcon() {
	return null;
	}
	@Override
	public String getTitle() {
	return "PhenomeScape";
	}
	
	public void newTableData(ArrayList<String[]> tableData,ArrayList<String[]> parameterData) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Network Number");
		model.addColumn("p-value");
		model.addColumn("Enriched GO Term");
		DefaultTableModel parameterModel = new DefaultTableModel();
	
		for (String[] rowData : tableData){
			model.addRow(rowData);			
			
		}
		
		String [] parameters = parameterData.get(0);
		String [] values = parameterData.get(1);
		parameterModel.addColumn("Parameters",parameters);
		parameterModel.addColumn("Values",values);
		
		this.setTableData(model,parameterModel);
		this.getTableModelList().add(model);
		this.getParameterTableModelList().add(parameterModel);
		this.setCurrentIndex(this.getCurrentIndex() + 1);
					
	}
	
	
	
	public void setTableData(DefaultTableModel model,DefaultTableModel parameterModel) {
		this.summaryTable.setModel(model);		
		this.parameterTable.setModel(parameterModel);		
	}
	
	public JTable getSummaryTable() {
		return(this.summaryTable);
			

	}
	
	public JTable getParameterTable() {
		return(this.parameterTable);
			

	}
	
	public DefaultTableModel getModel() {
		
		return this.model;
	}
	
	public DefaultTableModel getParameterModel() {
		
		return this.parameterModel;
	}



	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}


	public ArrayList<DefaultTableModel> getTableModelList() {
		return tableModelList;
	}


	public void setTableModelList(ArrayList<DefaultTableModel> tableModelList) {
		this.tableModelList = tableModelList;
	}

	public void setParameterTableModelList(ArrayList<DefaultTableModel> parameterTableModelList) {
		this.parameterTableModelList = parameterTableModelList;
	}


	public ArrayList<DefaultTableModel> getParameterTableModelList() {
		return parameterTableModelList;
		
	}





	
	

}
