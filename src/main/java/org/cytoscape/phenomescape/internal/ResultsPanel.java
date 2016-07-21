package org.cytoscape.phenomescape.internal;



import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.phenomescape.internal.listeners.BackwardsTableListener;
import org.cytoscape.phenomescape.internal.listeners.ExportMouseListener;
import org.cytoscape.phenomescape.internal.listeners.ForwardTableListener;
import org.cytoscape.phenomescape.internal.util.TableColumnResizer;
import org.cytoscape.service.util.CyServiceRegistrar;


public class ResultsPanel extends JPanel implements CytoPanelComponent{

	private static final long serialVersionUID = -4370789481040928750L;
	public CyServiceRegistrar cyServiceRegistrar;
	public JTable summaryTable;
	public DefaultTableModel model;
	public JScrollPane pane;
	public JButton exportButton;
	private BasicArrowButton rightButton;
	private BasicArrowButton leftButton;
	private ArrayList<DefaultTableModel> tableModelList;
	private ArrayList<DefaultTableModel> parameterTableModelList;
	private int currentIndex = -1;
	private JTable parameterTable;
	private JScrollPane parameterPane;
	private DefaultTableModel parameterModel;
	public TableColumnResizer summaryTableColumnResizer;
	public TableColumnResizer parameterTableColumnResizer;

	public ResultsPanel (CyServiceRegistrar cyServiceRegistrar ) {
		this.cyServiceRegistrar = cyServiceRegistrar;

		createPanelLayout();


		rightButton.addActionListener(new ForwardTableListener(this));
		leftButton.addActionListener(new BackwardsTableListener(this));

		initialiseExportButton();


		setTableModelList(new ArrayList<DefaultTableModel> ());
		setParameterTableModelList(new ArrayList<DefaultTableModel> ());

		this.setVisible(true);

	}




	private void createPanelLayout () {

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 0;
		c.ipady = 0;
		c.weightx=0.5;
		c.insets = new Insets(2, 2,2,2);
		setSize(new Dimension(450, 200));
		setPreferredSize(new Dimension(450, 200));
		c.fill = GridBagConstraints.BOTH;
		c.gridy=0;
		createTopRow(c);
		c.gridy=1;
		createSummaryTable(c);
		c.gridy=8;
		createParameterTable(c);		

	}


	private void createSummaryTable(GridBagConstraints c) {
		c.gridx=0;
		c.weighty=0.5;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth=6;

		model = new DefaultTableModel();
		model.addColumn("Network Name");
		model.addColumn("pvalue");
		summaryTable = new JTable(model){
			private static final long serialVersionUID = 1L;

			public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}
		};

		summaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		summaryTableColumnResizer = new TableColumnResizer(summaryTable);
		summaryTableColumnResizer.adjustColumns();
		pane = new JScrollPane();
		pane.setViewportView(summaryTable);
		this.add(pane,c);		
	}




	private void createTopRow(GridBagConstraints c) {
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;


		leftButton = new BasicArrowButton(BasicArrowButton.WEST);

		leftButton.putClientProperty("JComponent.sizeVariant", "large");
		this.add(leftButton,c);

		c.gridx = 1;
		rightButton = new BasicArrowButton(BasicArrowButton.EAST);
		rightButton.putClientProperty("JComponent.sizeVariant", "large");
		this.add(rightButton,c);

		c.gridwidth = 2;
		c.gridx = 2;
		exportButton = new JButton("Export Table");
		this.add(exportButton,c);


	}







	private void createParameterTable (GridBagConstraints c) {
		c.gridx=0;
		c.weighty=0.5;

		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 6;

		parameterModel = new DefaultTableModel();
		parameterModel.addColumn("Parameter");
		parameterModel.addColumn("Value");
		parameterTable = new JTable(parameterModel){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}
		};
		parameterTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		parameterTableColumnResizer = new TableColumnResizer(parameterTable);
		parameterTableColumnResizer.adjustColumns();
		parameterPane = new JScrollPane();
		parameterPane.setViewportView(parameterTable);
		this.add(parameterPane,c);


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
		model.addColumn("Network Name");
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
		parameterTableColumnResizer.adjustColumns();
		summaryTableColumnResizer.adjustColumns();


	}

	public void resizeColumns(JTable table){
		for (int column = 0; column < table.getColumnCount(); column++)
		{
			TableColumn tableColumn = table.getColumnModel().getColumn(column);
			int preferredWidth = tableColumn.getMinWidth();
			int maxWidth = tableColumn.getMaxWidth();

			for (int row = 0; row < table.getRowCount(); row++)
			{
				TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
				Component c = table.prepareRenderer(cellRenderer, row, column);
				int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
				preferredWidth = Math.max(preferredWidth, width);

				if (preferredWidth >= maxWidth)
				{
					preferredWidth = maxWidth;
					break;
				}
			}

			tableColumn.setPreferredWidth( preferredWidth );
		}
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
