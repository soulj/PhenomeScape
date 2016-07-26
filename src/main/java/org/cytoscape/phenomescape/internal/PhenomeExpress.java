package org.cytoscape.phenomescape.internal;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.phenomescape.internal.Protein.DescendingScoreComparator;
import org.cytoscape.phenomescape.internal.util.CommandExecutor;
import org.cytoscape.phenomescape.internal.util.ConnectedComponentAnalyser;
import org.cytoscape.phenomescape.internal.util.CytoPanelUtils;
import org.cytoscape.phenomescape.internal.util.GOTermAnalyser;
import org.cytoscape.phenomescape.internal.util.GOTermAnalyser2;
import org.cytoscape.phenomescape.internal.util.NetworkUtils;
import org.cytoscape.phenomescape.internal.util.Phenotype;
import org.cytoscape.phenomescape.internal.util.VizStyle;

import mikera.indexz.Index;
import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;



public class PhenomeExpress extends AbstractTask implements ObservableTask {

	private List<Phenotype> phenotypeList;
	private int maxIterations;
	private int maxNetworkSize;
	private CyServiceRegistrar cyServiceRegistrar;
	private String networkName;
	private ArrayList<Phenotype> phenotypes;
	private String species;
	private String geneName;
	private String foldChange;
	private String pvalue;
	private PhenomeNetwork phenomeNetwork;
	private CyNetwork selectedNetwork;
	private ProteinNetwork proteinNetwork;
	private PhenoGeneNetwork phenoGeneNetwork;
	private ControlPanel controlPanel;
	private String phenotypeSelected;
	private double threshold;
	private int minNetSize;
	private ArrayList<Phenotype> selectedPhenotypes;
	private ArrayList<CyEdge> phenoEdges;
	private ArrayList<CyNode> phenotypesAdded;
	private VisualProperty property;
	private String phenotypeNamesSelected;




	public PhenomeExpress(ControlPanel controlPanel) throws FileNotFoundException {

	
		this.networkName = controlPanel.getNetworkValue();
		this.phenotypes=controlPanel.phenotypeTableModel.phenotypes;
		this.species = controlPanel.getSpeciesValue();
		this.geneName = controlPanel.getGeneNameValue();
		this.foldChange = controlPanel.getFoldChangeValue();
		this.species = controlPanel.getSpeciesValue();
		this.pvalue = controlPanel.getpvalueValue();
		this.cyServiceRegistrar=controlPanel.cyServiceRegistrar;
		this.controlPanel = controlPanel;
		
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Running PhenomeExpress");
		taskMonitor.setStatusMessage("Preparing Networks");

		long time = System.currentTimeMillis();

		inputCheck();
		setUpNetworks();


		double[] relativeWeighting= {0.2,0.3,0.4,0.5,0.6,0.7,0.8};
		double[] alphas = {0.2,0.3,0.4,0.5,0.6,0.7,0.8};
		double [] piValues = proteinNetwork.getPivalues();



		Map<String, Integer> phenotypeMap = phenomeNetwork.getName2IndexMap();
		double [] phenoArray = new  double [phenomeNetwork.getName2IndexMap().size()] ;
		selectedPhenotypes = new ArrayList<Phenotype>();
		boolean first = true;
		for (Phenotype phenotype: phenotypes){
			if (phenotype.getSelected()){
				if(first){
					phenotypeSelected= phenotype.getID();
					phenotypeNamesSelected=phenotype.getName();
					selectedPhenotypes.add(phenotype);
					phenoArray[phenotypeMap.get(phenotype.getID())]=1.0;
					first=false;
				}
				else{
				
					phenotypeSelected = phenotypeSelected + " ," + phenotype.getID();
					phenotypeNamesSelected = phenotypeNamesSelected + " ," + phenotype.getName();
					phenoArray[phenotypeMap.get(phenotype.getID())]=1.0;
					selectedPhenotypes.add(phenotype);
				}

				
			}			 
		}


		Map<CyNode, Integer> map = proteinNetwork.getNode2IndexMap();
		int size = proteinNetwork.getNetwork().getNodeCount();

		int[] occuranceArray = new int[size];
		HashMap<CyNode,Protein> node2Protein = new HashMap<CyNode,Protein>();

		
	

		if (cancelled) { return; }
		taskMonitor.setStatusMessage("Calculating Activity Scores");
		taskMonitor.setProgress(0.0);
		double progress = 0.0;
		SparseRowMatrix adjMatrix = SparseRowMatrix.create(size, size);
		double[] transitionProbs = {0.5,0.6,0.7,0.8};
		for (double transitionProb:transitionProbs){

			TransitionMatrix transitionMatrix = new TransitionMatrix(phenomeNetwork.normalise(transitionProb, phenoGeneNetwork),proteinNetwork.normalise(transitionProb, phenoGeneNetwork),phenoGeneNetwork.normaliseT(transitionProb),phenoGeneNetwork.normalise(transitionProb));


			for (double weighting : relativeWeighting){
				if (cancelled) { return; }
				transitionMatrix.prepareVector(piValues, phenoArray,weighting);
				for (double alpha : alphas){
					progress+=0.0051;
					taskMonitor.setProgress(progress);

					double[] pageRankResults = transitionMatrix.pageRank(alpha);
					HashMap<Integer,Protein> index2ProteinMap = proteinNetwork.getIndex2ProteinMap();
					
					for (int i =0;i<index2ProteinMap.size();i++ ){
						Protein protein = index2ProteinMap.get(i);
						protein.setValue(pageRankResults[i]);
					}
					List<Protein> proteinList = new ArrayList<Protein>(index2ProteinMap.values());

					Collections.sort(proteinList, new DescendingScoreComparator());
					
					int rank=0;
					for (Protein protein: proteinList){
						rank++;
						protein.setRank(rank);
						node2Protein.put(protein.getNode(),protein);				
					}

					Collections.sort(proteinList, new DescendingScoreComparator());

					GIGA giga = new GIGA(proteinList,node2Protein,proteinNetwork,maxNetworkSize);
					giga.compute();

					for (GIGACluster cluster: giga.getClusters()){
						for (CyNode node :cluster.getCluster()){
							Integer index = map.get(node);
							AVector row = adjMatrix.getRow(index);
							if(!row.isMutable()){
								row=row.mutable();
							}
							for (CyNode node2 :cluster.getCluster()){
								Integer index2 = map.get(node2);
								row.set(index2,row.get(index2)+1.0);
								occuranceArray[index2]++;

							}							

							adjMatrix.replaceRow(index,row);	
						}



					} 
				}
			}
		}

		if (cancelled) { return; }
		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Creating Consensus Subnetworks");
		AMatrix cooccuranceMat = adjMatrix.transposeInnerProduct(adjMatrix);
		for (int i=0;i < size;i++ ){
			AVector row = cooccuranceMat.getRow(i);
			if (row.elementSum()>0){
				row.divide(occuranceArray[i]);
				Index nonSparse=row.nonSparseIndex();
				for (int j=0;j < nonSparse.length();j++){
					if (row.get(nonSparse.get(j))< 1.0){
						row.set(nonSparse.get(j),0.0);
					}
				}
				cooccuranceMat.replaceRow(i, row.sparse());
			}
		}


		ConnectedComponentAnalyser CCA = new ConnectedComponentAnalyser(node2Protein);
		ArrayList<PhenomeExpressSubnetwork> subnetworks = CCA.ccFromAdjMatrix(adjMatrix,proteinNetwork);
		
		Iterator<PhenomeExpressSubnetwork> it = subnetworks.iterator();
		while(it.hasNext()){
			PhenomeExpressSubnetwork subnetwork = it.next();
			if(subnetwork.getNodeList().size()<this.minNetSize){
				it.remove();
			}
		}


		if (cancelled) { return; }
		taskMonitor.setStatusMessage("Calculating Subnetwork Significance");
		subnetworksSignificance(subnetworks);

		
		taskMonitor.setStatusMessage("Visualising Subnetworks");
		List<Double> foldChangeValues = proteinNetwork.getNodeTable().getColumn(foldChange).getValues(Double.class);
		double fcMax = Collections.max(foldChangeValues);
		double fcMin = Collections.min(foldChangeValues);

		// Apply the visual style to a NetworkView
		VizStyle vizStyle = new VizStyle();
		VisualStyle vs = vizStyle.createVizStyle(cyServiceRegistrar, geneName, foldChange, fcMax, fcMin);
		this.property = getNodeLabelPositionProperty();
		vs.setDefaultValue(property, property.parseSerializableString("N,S,c,0.0,0.0"));
		VisualMappingManager visualMappingManager = cyServiceRegistrar.getService(VisualMappingManager.class);
		visualMappingManager.setCurrentVisualStyle(vs);
		ArrayList<CyNetworkView>  networkViewList = new ArrayList<CyNetworkView>();
		
		Iterator<PhenomeExpressSubnetwork> it2 = subnetworks.iterator();

		CyNetworkManager networkManager = cyServiceRegistrar.getService(CyNetworkManager.class);
		CyNetworkViewFactory networkViewFactory = cyServiceRegistrar.getService(CyNetworkViewFactory.class);
		CyLayoutAlgorithmManager layoutManager= cyServiceRegistrar.getService(CyLayoutAlgorithmManager.class);
		CyApplicationManager cyApplicationManager = cyServiceRegistrar.getService(CyApplicationManager.class);
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("defaultSpringLength", 5.0);
		settings.put("defaultSpringNodeMass", 2.0);
		TunableSetter setter = cyServiceRegistrar.getService(TunableSetter.class);
		Object context = layout.createLayoutContext();
		setter.applyTunables(context, settings);
		
		GOTermAnalyser2 goTermAnalyser = new GOTermAnalyser2(proteinNetwork,species);
		
		while(it2.hasNext()){

			PhenomeExpressSubnetwork subnet = it2.next();
			if(subnet.getPvalue()>this.threshold){
				it2.remove();
			}
			else{
						
				goTermAnalyser.calculateGOTermPValues(subnet, proteinNetwork);
				String subnetworkName = NetworkUtils.getUniqueNetworkName(cyServiceRegistrar,networkName + "_" + subnet.getBestGOTerm());
				subnet.setName(subnetworkName);
				CySubNetwork subnetwork= NetworkUtils.createSubNetwork(((CySubNetwork)proteinNetwork.getNetwork()).getRootNetwork(),subnet.getNodeList());
				subnetwork.getRow(subnetwork).set(CyNetwork.NAME, subnetworkName);
				addSeedPhenotypes(subnetwork);
				
				networkManager.addNetwork(subnetwork);
				CyNetworkViewManager viewManager = cyServiceRegistrar.getService(CyNetworkViewManager.class);
				CyNetworkView nv = networkViewFactory.createNetworkView(subnetwork);
				viewManager.addNetworkView(nv);
				cyApplicationManager.setCurrentNetworkView(nv);
						
				for (CyNode phenoNode: phenotypesAdded){
					View<CyNode> nodeView=nv.getNodeView(phenoNode);
					nodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE,NodeShapeVisualProperty.RECTANGLE);
					nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR,Color.BLUE);
					
				}
				for (CyEdge phenoEdge:phenoEdges){
					View<CyEdge> edgeView=nv.getEdgeView(phenoEdge);
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_LINE_TYPE,LineTypeVisualProperty.DOT);
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT,Color.BLUE);
										
				}
				
				networkViewList.add(nv);
				Set<View<CyNode>> nodeSet = Collections.emptySet();
				cyServiceRegistrar.getService(TaskManager.class).execute(layout.createTaskIterator(nv,context,nodeSet,null));
						
			}
		}
		
		CyEventHelper cyEventHelper = cyServiceRegistrar.getService(CyEventHelper.class);
		
		for (CyNetworkView nv : networkViewList){
			visualMappingManager.setVisualStyle(vs,nv);
			vs.apply(nv);
			cyEventHelper.flushPayloadEvents();
			cyApplicationManager.setCurrentNetworkView(nv);
			nv.updateView();			
		}
		
		

		phenomeNetwork=null;
		proteinNetwork=null;
		

		ResultsPanel resultsPanel = (ResultsPanel) CytoPanelUtils.getCytoPanel(cyServiceRegistrar, ResultsPanel.class, CytoPanelName.EAST);
		resultsPanel.newTableData(getResultsSummary(subnetworks), getParameterList());
		CytoPanelUtils.showCytoPanel(cyServiceRegistrar,CytoPanelName.EAST);
	}


	private VisualProperty getNodeLabelPositionProperty() {
		VisualLexicon lex = cyServiceRegistrar.getService(RenderingEngineFactory.class).getVisualLexicon();
		VisualProperty prop = lex.lookup(CyNode.class, "NODE_LABEL_POSITION");
		return prop;
	}

	private void addSeedPhenotypes(CySubNetwork subnetwork) {
		phenotypesAdded = new ArrayList<CyNode>();
		phenoEdges = new ArrayList<CyEdge>();
		if( proteinNetwork.getNodeTable().getColumn("PhenotypeID") == null) {
			proteinNetwork.getNodeTable().createColumn("PhenotypeID", String.class, false);
		}
		for (Phenotype phenotype : selectedPhenotypes){
			HashMap<String, Integer> nameMap = phenoGeneNetwork.getPhenotypeName2IndexMap();
			Integer index = nameMap.get(phenotype.getID());
			
			AVector row = phenoGeneNetwork.getBipartiteAdjMatrix().getRow(Integer.valueOf(index));
			for (CyNode node:subnetwork.getNodeList()){
				//get the node index
				
				Integer proteinIndex = proteinNetwork.getNode2IndexMap().get(node);
				if (proteinIndex ==null) {continue;}
					
				//check if there is an association or not
				if(row.get(proteinIndex)!=0.0){
				
					CyNode phenoNode = null;
					if(proteinNetwork.getNodeTable().getMatchingRows("name", phenotype.getID()).isEmpty()){
						phenoNode  =proteinNetwork.getNetwork().addNode();
						proteinNetwork.getNodeTable().getRow(phenoNode.getSUID()).set(CyNetwork.NAME, phenotype.getID());
						proteinNetwork.getNodeTable().getRow(phenoNode.getSUID()).set("name", phenotype.getID());
						proteinNetwork.getNodeTable().getRow(phenoNode.getSUID()).set("PhenotypeID", phenotype.getName());
						
						
					}
					else{
						 phenoNode = NetworkUtils.getNodeWithName(proteinNetwork.getNetwork(),proteinNetwork.getNodeTable(), "name",phenotype.getID());
										
					}
						subnetwork.addNode(phenoNode);
						subnetwork.getDefaultNodeTable().getRow(phenoNode.getSUID()).set("name", phenotype.getName());
						subnetwork.getDefaultNodeTable().getRow(phenoNode.getSUID()).set("PhenotypeID", phenotype.getID());
						phenotypesAdded.add(phenoNode);
						proteinNetwork.getNetwork().addEdge(phenoNode,node,true);
						CyEdge addedEdge = subnetwork.addEdge(phenoNode,node,true);
						String proteinName = proteinNetwork.getIndex2ProteinMap().get(proteinIndex).getName();
						proteinNetwork.getEdgeTable().getRow(addedEdge.getSUID()).set("interaction","pgi");
						proteinNetwork.getEdgeTable().getRow(addedEdge.getSUID()).set("name",phenotype.getID()+" (pgi) "+proteinName);
						phenoEdges.add(addedEdge);
						
					}
										
				}
			}
			
			
		}

	private void inputCheck() throws IOException {
		int selected =0;
		for (Phenotype phenotype:phenotypes){
			if(phenotype.getSelected()){
				selected++;
			}
		}
		if (selected==0){
			throw new IOException("Please select at least one Phenotype");
		}
		try {
		this.threshold=Double.parseDouble(controlPanel.getThresholdValue());
		}
		catch (Exception e) {
			throw new IOException("Error in min p-value - must be a number");
		}
		if (this.threshold > 1.0 || this.threshold < 0.0){
			throw new IOException("Error in min p-value - must be between 0 and 1");
		}
		try {
		this.minNetSize=Integer.parseInt(controlPanel.getMinNetworkSize());
		}
		catch (Exception e) {
			throw new IOException("Min network size must be an integer");
		}
		if (this.minNetSize<2){
			throw new IOException("Error in min network size - must be greater than 1");
		}
		try {
			this.maxIterations= Integer.parseInt(controlPanel.getPermuationValue());
		}
		catch (Exception e){
			throw new IOException("The number of random networks must be an integer");
		}
		if (this.maxIterations<1){
			throw new IOException("The number of random networks must be at least 1!");
		}
				
		try {
			this.maxNetworkSize = Integer.parseInt(controlPanel.getMaxNetworkSize());
		}
		catch (Exception e){
			throw new IOException("The max network size must be an integer");
		}
		
		
		



	}

	private void setUpNetworks() throws Exception {
		//get the network
		CommandExecutor.execute("network set current network=" + networkName, cyServiceRegistrar);
		selectedNetwork = cyServiceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

		//create the protein network		
		proteinNetwork = new ProteinNetwork(selectedNetwork);

		//create the phenome network		
		phenomeNetwork = new PhenomeNetwork();

		
		//filter by the expressionData
		try {
			proteinNetwork.filterByExpression(foldChange,pvalue,geneName);
		} catch (IOException e) {				
			throw e;
		}

	

		proteinNetwork.createSparseAdjMatrix();
		
		phenoGeneNetwork = new PhenoGeneNetwork(controlPanel,species);
		
	
		phenomeNetwork = phenomeNetwork.load();

		
		
		System.gc();
		proteinNetwork.getName2IndexMap().keySet();

		
		phenoGeneNetwork.createSparseBiPartAdjMatrix(proteinNetwork, phenomeNetwork);

	}

	private void subnetworksSignificance(ArrayList<PhenomeExpressSubnetwork> subnetworks){
		ExecutorService executor = Executors.newFixedThreadPool(Math.max((Runtime.getRuntime().availableProcessors()-1),1));
		Collection<Callable<Double>> tasks = new ArrayList<>();
		for(PhenomeExpressSubnetwork subnetwork:subnetworks){
			Task task = new Task(proteinNetwork,subnetwork);
			tasks.add(task);
		}

		try {
			executor.invokeAll(tasks);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		executor.shutdown();

	}

	private  final class Task implements Callable{

		private PhenomeExpressSubnetwork subnet;
		private ProteinNetwork proteinNetwork;


		Task(ProteinNetwork proteinNetwork, PhenomeExpressSubnetwork subnetwork){
			this.subnet = subnetwork;
			this.proteinNetwork = proteinNetwork;
		}

		@Override
		public Object call() throws Exception {

			return subnet.sampleSubnetworks(proteinNetwork.getNetwork(), maxIterations);
		}


	}

	
	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}


	public CyNetwork getNetwork(){
		return(this.selectedNetwork);
	}

	@Override
	public <R> R getResults(Class<? extends R> arg0) {
		return null;
	}




	public ArrayList<String[]> getParameterList (){
		ArrayList<String[]>  parameterData = new ArrayList<String[]>();
		String[] parameters = {"Phenotype IDs", "Phenotype Names","Species","Network Size","Min Subnetwork Size", "No. Random Subnetwork","Min p-value"};
		String[] values = {this.phenotypeSelected,this.phenotypeNamesSelected,this.species,Integer.toString(this.maxNetworkSize),Integer.toString(this.minNetSize),Integer.toString(this.maxIterations),Double.toString(this.threshold)};

		parameterData.add(parameters);
		parameterData.add(values);


		return(parameterData);
	}

	public ArrayList<String[]> getResultsSummary (ArrayList<PhenomeExpressSubnetwork> subnetworks){
		ArrayList<String[]>  summary = new ArrayList<String[]>();
		for (int i =0;i<subnetworks.size();i++){
			String [] row = {subnetworks.get(i).getSubnetworkName(), Double.toString(subnetworks.get(i).getPvalue()),subnetworks.get(i).getBestGOTerm()};
			summary.add(row);
		}


		return(summary);
	}

}
