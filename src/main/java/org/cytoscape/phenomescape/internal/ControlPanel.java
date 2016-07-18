package org.cytoscape.phenomescape.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.phenomescape.internal.listeners.AnalyseMouseListener;
import org.cytoscape.phenomescape.internal.listeners.ColumnChangedListener;
import org.cytoscape.phenomescape.internal.listeners.NetworkComboBoxAddedNetwork;
import org.cytoscape.phenomescape.internal.listeners.NetworkComboBoxRemovedNetwork;
import org.cytoscape.phenomescape.internal.listeners.NetworkSelectedListener;
import org.cytoscape.phenomescape.internal.util.TableManager;
import org.cytoscape.service.util.CyServiceRegistrar;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements CytoPanelComponent {
	public CyServiceRegistrar cyServiceRegistrar;
	public JLabel networkLabel;
	public JComboBox networkCombo;
	public JScrollPane phenotypePane;
	public JTable phenotypeTable;
	public DefaultComboBoxModel networkModel;
	public JLabel dataLabel;
	public JTextField dataTextField;
	public File dataFile;
	public JButton preprocessButton;
	public JLabel formalismLabel;
	public JComboBox formalismCombo;
	public DefaultComboBoxModel dataModel;
	public JLabel dataTimePointLabel;
	public JComboBox dataCombo;
	public JButton optimiseButton;
	public JPanel algorithmPanel;
	public Map<String, JTextField> configurationsMap;
	public JTextField maxSizeTextField;
	public JTextField permTextField;
	public JComboBox<String> speciesCombo;
	public Hashtable<String, Long> tableRefs;
	public TableManager tableManager;
	private Set<String> tableTitles;
	public CyTableManager cyTableManager;
	public TableRowSorter<PhenotypeTableModel> sorter;
	public JLabel filterLabel;
	public JTextField filterText;
	public PhenotypeTableModel phenotypeTableModel ;
	private JLabel geneNameLabel;
	private JLabel pvalueLabel;
	private JLabel foldchangeLabel;
	private JComboBox geneNameCombo;
	private JComboBox pvalueCombo;
	private JComboBox foldchangeCombo;
	private JPanel selectionPanel;
	private JTextField thresholdTextField;
	private JTextField minSizeTextField;
	
	public ControlPanel (CyServiceRegistrar cyServiceRegistrar,CyTableManager cyTableManager ) {
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.configurationsMap = new HashMap<String, JTextField>();
		this.cyTableManager=cyTableManager;

		createPanelLayout();
		
		ColumnChangedListener columnChangedListener = new ColumnChangedListener(this);
		cyServiceRegistrar.registerAllServices(columnChangedListener, new Properties());
		

		this.setVisible(true);
		this.setPreferredSize(new Dimension(800,800));
	}
			
	
	private void createPanelLayout () {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 0;
		c.ipady = 0;
		c.weightx=0;
		c.insets = new Insets(2, 2,2,2);
		c.fill = GridBagConstraints.HORIZONTAL;
		// Initialise panel components
		c.gridy = 0;
		createFilterText(c);
		c.gridy = 1;
		createPhenotypeTable(c);	
		c.gridy = 6;
		createNetworkRow (c);
		c.gridy = 7;
		createSelectionRow(c);
		c.gridy = 8;
		createAlgorithmConfigurations (c);
		c.gridy=9;
		createPreprocessButtonRow (c);
		initialiseNetworkRow();
		initialisePreprocessButtonRow();
		
	}



	private void createFilterText(GridBagConstraints c) {
		c.gridx = 0;
		c.weightx=0.05;
		filterLabel = new JLabel("Filter Text:");
        this.add(filterLabel,c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx=0.95;
        filterText = new JTextField();
        filterText.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        newFilter();
                    }
                    public void insertUpdate(DocumentEvent e) {
                        newFilter();
                    }
                    public void removeUpdate(DocumentEvent e) {
                        newFilter();
                    }
                });
        filterLabel.setLabelFor(filterText);
		this.add(filterText,c);
	}



	private void createPhenotypeTable(GridBagConstraints c) {
		//c.gridwidth = 3;
		//c.gridheight = 3;
		c.gridx=0;
		c.weighty=0.5;
		c.fill = GridBagConstraints.BOTH;
		phenotypeTableModel = new PhenotypeTableModel();
		sorter = new TableRowSorter<PhenotypeTableModel>(phenotypeTableModel);
		phenotypeTable = new JTable(phenotypeTableModel);
		phenotypeTable.getTableHeader().setReorderingAllowed(false);
		phenotypeTable.setRowSorter(sorter);
		phenotypePane = new JScrollPane(phenotypeTable);
		//phenotypePane.setViewportView(phenotypeTable);
  	    this.add(phenotypePane,c);
  	    c.weighty=0;
		
	}
	// Create methods
	private void createNetworkRow (GridBagConstraints c) {
		networkLabel = new JLabel("Network");
		//networkLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
		//GridBagConstraints networkConstraints = new GridBagConstraints();
		c.gridx = 0;
		c.gridwidth = 1;
		c.weightx=0.2;
		c.anchor = GridBagConstraints.EAST;
		add(networkLabel, c);
		networkCombo = new JComboBox();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx=0.8;
		add(networkCombo,c);
	}

	private void createSelectionRow(GridBagConstraints c) {
		
		
		c.gridx = 0;
		c.gridwidth = 6;
				
		GridBagLayout selectionLayout = new GridBagLayout();	
		GridBagConstraints selectionConstraints = new GridBagConstraints();
		selectionPanel = new JPanel(selectionLayout);
		
		
		geneNameLabel = new JLabel("Name");
		selectionConstraints.fill = GridBagConstraints.HORIZONTAL;
		selectionConstraints.gridx = 0;
		selectionConstraints.gridwidth = 1;
		selectionPanel.add(geneNameLabel, selectionConstraints);
		geneNameCombo = new JComboBox();
		selectionConstraints.gridx = 1;
		selectionConstraints.gridwidth = 1;
		selectionPanel.add(geneNameCombo, selectionConstraints);
		
		pvalueLabel = new JLabel("p-value");
		selectionConstraints.gridx = 2;
		selectionConstraints.gridwidth = 1;
		selectionPanel.add(pvalueLabel, selectionConstraints);
		pvalueCombo = new JComboBox();
		selectionConstraints.gridx = 3;
		selectionConstraints.gridwidth = 1;
		selectionPanel.add(pvalueCombo, selectionConstraints);
		
		foldchangeLabel = new JLabel("Fold Change");
		selectionConstraints.gridx = 4;
		selectionConstraints.gridwidth = 1;
		selectionPanel.add(foldchangeLabel, selectionConstraints);
		foldchangeCombo = new JComboBox();
		selectionConstraints.gridx = 5;
		selectionConstraints.gridwidth = 1;
		selectionPanel.add(foldchangeCombo, selectionConstraints);
		
		this.add(selectionPanel,c);
		
	}

	private void createPreprocessButtonRow (GridBagConstraints c) {
		c.gridx = 0;
		c.gridwidth = 2;
		preprocessButton = new JButton("Run PhenomeExpress");
		add(preprocessButton, c);
	}
	private void createAlgorithmConfigurations (GridBagConstraints c) {
		c.gridx = 0;
		c.gridwidth = 2;
		GridBagLayout algorithmLayout = new GridBagLayout();	
		GridBagConstraints algorithmConstraints = new GridBagConstraints();
		algorithmPanel = new JPanel(algorithmLayout);
		algorithmPanel.setBorder(new TitledBorder(new LineBorder(Color.black, 1), "Parameters"));
		
		JLabel speciesLabel = new JLabel("Species");
		speciesLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
		algorithmConstraints.gridx = 0;
		algorithmConstraints.gridwidth = 1;
		algorithmPanel.add(speciesLabel,algorithmConstraints);
		String[] species = new String[] {"Human","Mouse"};
		speciesCombo = new JComboBox<> (species);
		algorithmPanel.add(speciesCombo,algorithmConstraints);
	
		JLabel maxSizeLabel = new JLabel("Network Size");
		maxSizeLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		maxSizeTextField = new JTextField("10");
		algorithmConstraints.gridx = 1;
		algorithmConstraints.gridwidth = 1;
		algorithmPanel.add(maxSizeLabel,algorithmConstraints);
		algorithmPanel.add(maxSizeTextField,algorithmConstraints);
		
		
		JLabel permLabel = new JLabel("No. Random Networks");
		permLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		permTextField = new JTextField("1000");
		algorithmConstraints.gridx = 2;
		algorithmConstraints.gridwidth = 1;
		algorithmPanel.add(permLabel,algorithmConstraints);
		algorithmPanel.add(permTextField,algorithmConstraints);
		add(algorithmPanel, c);
		
		JLabel thresholdLabel = new JLabel("Min p-value");
		thresholdLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		thresholdTextField = new JTextField("0.05");
		algorithmConstraints.gridx = 3;
		algorithmConstraints.gridwidth = 1;
		algorithmPanel.add(thresholdLabel,algorithmConstraints);
		algorithmPanel.add(thresholdTextField,algorithmConstraints);
		add(algorithmPanel, c);
		
		JLabel minSizeLabel = new JLabel("Min Size");
		minSizeLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		minSizeTextField = new JTextField("4");
		algorithmConstraints.gridx = 4;
		algorithmConstraints.gridwidth = 1;
		algorithmPanel.add(minSizeLabel,algorithmConstraints);
		algorithmPanel.add(minSizeTextField,algorithmConstraints);
		add(algorithmPanel, c);
	}
	public String getThresholdValue() {
		String threshold = thresholdTextField.getText();
		return threshold;
	}

	public String getMinNetworkSize() {
		String threshold = minSizeTextField.getText();
		return threshold;
	}

	private void initialiseNetworkRow () {
		networkModel = new DefaultComboBoxModel();
		for (CyNetwork cyNetwork : cyServiceRegistrar.getService(CyNetworkManager.class).getNetworkSet()) {
			String cyNetworkName = cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);
			networkModel.addElement(cyNetworkName);
		}
		networkCombo.setModel(networkModel);
		NetworkComboBoxAddedNetwork addNetworkListener = new NetworkComboBoxAddedNetwork(networkCombo);
		cyServiceRegistrar.registerAllServices(addNetworkListener, new Properties());
		NetworkComboBoxRemovedNetwork removeNetworkListener = new NetworkComboBoxRemovedNetwork(networkCombo);
		cyServiceRegistrar.registerAllServices(removeNetworkListener, new Properties());
		networkCombo.addItemListener(new NetworkSelectedListener(this));
	}

	private void initialisePreprocessButtonRow () {
		preprocessButton.addActionListener(new AnalyseMouseListener(this, cyTableManager));
	}
	

	public String getNetworkValue () {
		String network = (String) networkCombo.getSelectedItem();
		return network;
	}
	
	
	public String getTableValue () {
		String table = (String) dataCombo.getSelectedItem();
		return table;
	}
	
	public String getPermuationValue () {
		String perms = permTextField.getText();
		return perms;
	}

	public String getMaxNetworkSize () {
		String maxSize = maxSizeTextField.getText();
		return maxSize;
	}
	public String getSpeciesValue () {
		String species =  (String) speciesCombo.getSelectedItem();
		return species;
	}
	
	public String getGeneNameValue () {
		String geneName =  (String) geneNameCombo.getSelectedItem();
		return geneName;
	}
	
	public String getFoldChangeValue () {
		String foldChange =  (String) foldchangeCombo.getSelectedItem();
		return foldChange;
	}
	
	public String getpvalueValue () {
		String pvalue =  (String) pvalueCombo.getSelectedItem();
		return pvalue ;
	}
	
	private void newFilter() {
	    RowFilter<PhenotypeTableModel, Object> rf = null;
	    try {
	       
	       rf =     RowFilter.orFilter(Arrays.asList(RowFilter.regexFilter("(?i)" + filterText.getText(),0),
	        	    RowFilter.regexFilter("(?i)" + filterText.getText(), 1)));        
	       
	    } catch (java.util.regex.PatternSyntaxException e) {
	        return;
	    }
	    sorter.setRowFilter(rf);
	}
	
	@Override
	public Component getComponent() {
		return this;
	}
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}
	@Override
	public Icon getIcon() {
		return null;
	}
	@Override
	public String getTitle() {
		return "PhenomeScape";
	}


	public void setGeneName(DefaultComboBoxModel jComboModel) {
		geneNameCombo.setModel(jComboModel);
		
	}
	public void setFoldChange(DefaultComboBoxModel jComboModel) {
		foldchangeCombo.setModel(jComboModel);
	}
	public void setPvalue (DefaultComboBoxModel jComboModel) {

		pvalueCombo.setModel(jComboModel);
	}

}
