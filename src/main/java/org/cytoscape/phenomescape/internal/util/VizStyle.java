package org.cytoscape.phenomescape.internal.util;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class VizStyle {
	
	public VisualStyle createVizStyle (CyServiceRegistrar cyServiceRegistrar, String geneName, String foldchangeName, double maxFC, double minFC) {
	
	  // To get references to services in CyActivator class
	  VisualMappingManager vmmServiceRef = cyServiceRegistrar.getService(VisualMappingManager.class);
	                
	  VisualStyleFactory visualStyleFactoryServiceRef = cyServiceRegistrar.getService(VisualStyleFactory.class);
	                
	  VisualMappingFunctionFactory vmfFactoryC = cyServiceRegistrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
	  VisualMappingFunctionFactory vmfFactoryD = cyServiceRegistrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
	  VisualMappingFunctionFactory vmfFactoryP = cyServiceRegistrar.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");


	  // To create a new VisualStyle object and set the mapping function
	  VisualStyle vs= visualStyleFactoryServiceRef.createVisualStyle("PhenomeScape");


	  //Use pass-through mapping to create the node label
	  PassthroughMapping nameMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction(geneName, String.class, BasicVisualLexicon.NODE_LABEL);
	  vs.addVisualMappingFunction(nameMapping); 
	  
	  vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE,15.0);
	  
	 
//	  RenderingEngineManager renderingEngineManager = cyServiceRegistrar.getService(RenderingEngineManager.class);
//	  CyNetworkViewManager viewManager = cyServiceRegistrar.getService(CyNetworkViewManager.class);
//	  CyNetworkViewFactory networkViewFactory = cyServiceRegistrar.getService(CyNetworkViewFactory.class);
//	  Set<CyNetworkView> views = viewManager.getNetworkViewSet();
//	  CyNetworkView networkView = views.iterator().next();
//      Collection<RenderingEngine<?>> engines = renderingEngineManager.getAllRenderingEngines();
//      VisualLexicon lex = engines.iterator().next().getVisualLexicon();
//      VisualProperty prop = lex.lookup(CyNode.class, "NODE_LABEL_POSITION");
//      Object value = prop.parseSerializableString("S,N,c,0.0,5.0"); // Put the north of the label on the southeast corner of the node
//	  vs.setDefaultValue(prop, value);
      
	  
	  //Set the node size to smaller than the default

	  
	// Set node color map to attribute "FoldChange"
	  ContinuousMapping mapping = (ContinuousMapping) vmfFactoryC.createVisualMappingFunction(foldchangeName,Double.class, BasicVisualLexicon.NODE_FILL_COLOR);

	  // Define the points
	  BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(Color.GREEN, Color.GREEN,Color.WHITE);
	  BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<Paint>(Color.GREEN, Color.WHITE, Color.RED);
	  BoundaryRangeValues<Paint> brv3 = new BoundaryRangeValues<Paint>(Color.RED, Color.RED, Color.RED);
	                
	  // Set the points
	  //TODO add the point size and text postition
	  mapping.addPoint(minFC, brv1);
	  mapping.addPoint(0.0, brv2); 
	  mapping.addPoint(maxFC, brv3);

	  

	  // add the mapping to visual style            
	  vs.addVisualMappingFunction(mapping);

	  // Add the new style to the VisualMappingManager
	  vmmServiceRef.addVisualStyle(vs);
	  
	  return (vs);
	}
	
	

}
