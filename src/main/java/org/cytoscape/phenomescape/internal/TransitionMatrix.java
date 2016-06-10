package org.cytoscape.phenomescape.internal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import mikera.matrixx.AMatrix;
import mikera.matrixx.impl.SparseRowMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector;



public class TransitionMatrix {
	
	private SparseRowMatrix transitionMatrix;
	private AVector prob;
	
	
	
	
	public TransitionMatrix(AMatrix phenome,AMatrix protein, AMatrix G2P, AMatrix P2G) throws IOException{
		
		int size = protein.rowCount()+phenome.rowCount();
		transitionMatrix = SparseRowMatrix.create(size,size);

			
		for (int i =0; i<protein.rowCount();i++){
			AVector temp = protein.getRow(i).join(G2P.getRow(i));
			transitionMatrix.replaceRow(i,temp);

		}
		
		int j=protein.rowCount();
		int index;
		for (int i =0; i<phenome.rowCount();i++){
			index = i+j;
			AVector temp = P2G.getRow(i).join(phenome.getRow(i));
			transitionMatrix.replaceRow(index,temp);
					
		}		
	}
	
	public void prepareVector(double [] piValues, double[] Phenotypes, double weighting) throws IOException{
		AVector pi =   Vector.of(piValues);

		double sum = pi.elementSum();
		pi.multiply(weighting);
		pi.divide(sum);
		
		AVector pheno =  Vector.of(Phenotypes);

		sum = pheno.elementSum();
		pheno.multiply(1-weighting);
		pheno.divide(sum);

		
		AVector prob =pi.join(pheno);
		this.prob=prob;
			
	}
	
	public double[] pageRank(double alpha) {
		AVector probAlpha = prob.copy();
		double eps = 0.000001;
		int iter=0;
		AVector pi0 = Vector.of(new double[prob.length()]);
		AVector pi1 = prob.copy();
		probAlpha.multiply(alpha);
		AMatrix transitionMatrixT = transitionMatrix.getTranspose();
		
				
		while ( (distance(pi0,pi1) > eps) && iter<100000){
			pi0=pi1.copy();
			pi1.multiply((1-alpha));
			pi1=pi1.innerProduct(transitionMatrixT);
			pi1.add(probAlpha);
			iter++;
		}
		return (pi1.asDoubleArray());
		
	}
	
	
	
	
	public double distance(AVector pi0, AVector pi1) {
		AVector difference = pi0.subCopy(pi1);
		double distance = Math.abs(difference.elementSum());
		return (distance);
					
	}

	public SparseRowMatrix getTransitionMatrix() {
		return transitionMatrix;
	}

}
