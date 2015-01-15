package algorithms;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import graph.Graph;
import graph.Vertex;


/**
 * The clustering coefficient (Watts & Strogatz, 1998)
 * of a vertex v is the likelihood that two neighbours 
 * are connected. It is determined as the ratio between
 * the number of direct links that exist between the 
 * neighbours of vertex v, divided by the max number of 
 * possible links that could exist. That is,
 * 
 * 		LOCAL-CC_v = #Existing Links in G_v
 *                   ------------------------
 *                Max # of possible links in G_v
 *                
 * Where G_v is the neighbourhood of vertex v. The 
 * clustering coefficient of the graph as a whole is the
 * average clustering coefficient of all the vertices.
 * Therefore, the GLOBAL-CC ranges between 0 and 1, with
 * 0 containing no triangles of connected vertices, and 
 * 1 being a perfect clique.
 * 
 * @author Kirsty Williams
 *
 */
public class ClusteringCoefficient {
	
	private Graph G;
	private TObjectFloatHashMap<Vertex> clusteringMap;
	
	public ClusteringCoefficient(Graph G){
		this.G = G;
		
		clusteringMap = new TObjectFloatHashMap<Vertex>(G.getNumVertices());
		for(Vertex v : G.getVertices()) clusteringMap.put(v, 0f);
		System.out.println("Global CC: " + computeGlobalClusteringCoefficient());
	}//end constructor
	
	/**
	 * This method returns the local clustering coefficient
	 * for a vertex.
	 * @param i - Vertex to compute clustering coefficient for
	 * */
	private float getLocalClusteringCoefficient(Vertex i){
		float cc_i = 0;
		int edgesInNeighbourhood = 0;
		int numNeighbours = i.getNeighbours().size();
		int possibleEdgesInNeighbourhood;
		if (G.isDirected()) possibleEdgesInNeighbourhood = (numNeighbours * (numNeighbours - 1)) * 2;
		else possibleEdgesInNeighbourhood = (numNeighbours * (numNeighbours - 1));
		
		for(Vertex j : i.getNeighbours()){
			for(Vertex k : i.getNeighbours()){
				if(j != k){ 
					if (j.getEdge(k) != null) edgesInNeighbourhood++;
					if(G.isDirected()) 
						if (k.getEdge(j) != null) edgesInNeighbourhood++;
				}//end if
			}//end for
		}//end for
		
		if(numNeighbours <= 1) cc_i = 1;
		else cc_i = (float)edgesInNeighbourhood / (float) possibleEdgesInNeighbourhood;

		clusteringMap.adjustValue(i, cc_i);
		return cc_i;
	}//end getLocalClusteringCoefficient
	
	/**
	 * This method returns the global clustering 
	 * coefficient of the graph as a whole.
	 * */
	private float computeGlobalClusteringCoefficient(){
		float cc_g = 0;
		float sum = 0;

		for(Vertex v : G.getVertices()){
			sum += getLocalClusteringCoefficient(v);
		}//end for
		
		cc_g = sum / G.getNumVertices();
		
		return cc_g;
	}//end computeGlobalClusteringCoefficient
	
	public final TObjectFloatMap<Vertex> getClusteringMap() {
		return clusteringMap;
	}//end getClusteringMap
}