package algorithms;

import utils.GetInDegree;

import graph.Graph;
import graph.Vertex;

/**
 * The degree distribution is the probability that
 * a randomly chosen vertex has exactly k edges. 
 * The measurement is computed as follows:
 * 			P(k) = N(k) / N
 * where, N(k) is the average number of vertices of 
 * degree k.
 * 
 * @author Kirsty Williams
 */
public class DegreeDistribution {
	private int[] dd;
	private Graph G;
	
	public DegreeDistribution(Graph G){
		this.G = G;
		int max = 0;
		
		if(G.isDirected()){
			GetInDegree inDegree = new GetInDegree(G.getVertices());
			G.getEdges().forEach(inDegree);
			
			for(Vertex v : G.getVertices()){
				int vInDegree = inDegree.getEdgeMap().get(v).size();				
				if (vInDegree > max) max = vInDegree;
			}//end for
			
			dd = new int[max+1];
			for(Vertex v : G.getVertices()){
				int vInDegree = inDegree.getEdgeMap().get(v).size();
				dd[vInDegree]++;
			}//end for
		}else{
			for(Vertex v : G.getVertices()){
				int deg = v.getOutDegree();
				if(deg > max) max = deg;
			}//end for
			
			dd = new int[max+1];
			for(Vertex v : G.getVertices()){
				int deg = v.getOutDegree();
				dd[deg]++;
			}//end for
		}//end if
		printDegreeDistribution();
		
	}//end constructor
	
	/**
	 * This method prints the degree distributions
	 * to console. In the case of directed graphs the
	 * inDegree is used for calculations.
	 */
	private void printDegreeDistribution(){
		if(G.isDirected()){
			System.out.println("----------------------------");
			System.out.println("DEGREE|QUANTITY|FREQ");
			System.out.println("----------------------------");
			for(int i = 0; i < dd.length; i++){
				double freq = (dd[i]/(double)G.getNumVertices());
				System.out.println("{" + (i) + ", "+dd[i]+", "+freq + "}");
			}//end for
		}else{
			System.out.println("----------------------------");
			System.out.println("DEGREE|QUANTITY|FREQ");
			System.out.println("----------------------------");
			for(int i=0;i<dd.length;i++){
				double freq = (dd[i]/(double)G.getNumVertices());
				System.out.println((i) + " "+dd[i]+" "+freq); 
			}//end for
		}//end if
	}//end printDegreeDistribution
}//end class

