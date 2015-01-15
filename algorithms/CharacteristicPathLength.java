package algorithms;

import java.util.Iterator;
import java.util.Set;
import gnu.trove.map.hash.THashMap;
import graph.Edge;
import graph.Graph;
import graph.Vertex;

/**
 * The path length of a graph is the number of distinct
 * directed edges that are connected between a source
 * vertex v and target vertex u. The shortest path length
 * between two vertices averaged over all pairs of vertices
 * is the graph's characteristic path length. More formally,
 * The characteristic path length of a graph:
 * 		CPL = 1/n(n-1) * sum_distance[i, j]
 * 
 * The following implementation returns the characteristic
 * path length.
 * 
 * @author Kirsty Williams
 */
public class CharacteristicPathLength {
	
	private THashMap<Integer, Vertex> iMap; //Map an integer to every vertex in the graph
	private THashMap<Vertex, Integer> vMap; //Map a vertex to its integer representation
	private int n; //The number of vertices
	
	/**
	 * @param G - the graph
	 */
	public CharacteristicPathLength(Graph G){
		//initialise variables
		n = G.getVertices().size();
		iMap = new THashMap<Integer, Vertex>(n);
		vMap = new THashMap<Vertex, Integer>(n);
	
		//store vertex pointer in map
		int i = 0;
	  	for (Vertex v : G.getVertices()){
	  		vMap.put(v, i);
	  		iMap.put(i, v);
	  		i++;
	  	}//end for
	  	
	  	//System.out.println("cpl:" + calculateCPL());
	  	calculateCPL();
	}//end constructor
	
	/**
	 * This method calculates the average
	 * shortest path length between any two vertices
	 * (i.e. the characteristic path length).
	 */
	private void calculateCPL(){
		double cpl = 0; //The sum of all distances
		int infinitePaths = 0; //any infinte paths
		
		for(int i = 0; i < n; i++){
			int[] L = new int[n]; //distances from i to every other vertex
			boolean[] checked = new boolean[n]; //visited vertices
			
			for(int j = 0; j < n; j++){
				L[j] = Integer.MAX_VALUE; //initialise to very large number
			}//end for
			
			//Retrieve neighbours of the vertex i
			//Set<Vertex> neighbours = iMap.get(i).getNeighbours();
			Set<Edge> neighbours = iMap.get(i).getEdges();
			Iterator<Edge> it = neighbours.iterator();
			
			//iterate over all neighbours
			while(it.hasNext()){
				Edge edge = it.next();
				Vertex k = edge.getVertexTo();
				int neighbourIndex = vMap.get(k);
				
				//Make sure we have retrieved the correct side of 
				//the edge
				if(neighbourIndex == i)
					neighbourIndex = vMap.get(edge.getVertexFrom());
				
				//set distance to the neighbour as 1
				L[neighbourIndex] = 1;
			}//end while
			
			//Check vertices on path from i to j
			//if distance from i to k is less than
			//a valid distance
			for(int valid = 1; valid < n; valid++){
				int min = Integer.MAX_VALUE;
				int index = 0;
				
				for(int j=0;j<n;j++){
					if((min > L[j]) && (!checked[j])){
						min = L[j];
						index = j;
					}//end if
				}//end for
				
				//mark closest vertex as visited
				checked[index] = true;
				
				//get neighbours of neighbour
				//Set<Vertex> adjacentNeighbours = iMap.get(index).getNeighbours();
				Set<Edge> adjacentNeighbours = iMap.get(index).getEdges();
				Iterator<Edge> it2 = adjacentNeighbours.iterator();
				
				//iterate over adjacent neighbours
				while(it2.hasNext()){
					Edge edge = it2.next();
					Vertex k = edge.getVertexTo();
					int adjacentNeighbourIndex = vMap.get(k);
					
					//Make sure we have retrieved the correct side of 
					//the edge
					if(adjacentNeighbourIndex == index)
						adjacentNeighbourIndex = vMap.get(edge.getVertexFrom());
					
					if(!checked[adjacentNeighbourIndex]){
						//increment distance
						int sum = L[index] + 1;
						if(sum < L[adjacentNeighbourIndex]){
							L[adjacentNeighbourIndex] = sum;
						}//end if
					}//end if
				}//end while		
			}//end for
			
			//add all shortest distances
			for(int j=0; j<n;j++){
				if(i != j){
					if((L[j] < Integer.MAX_VALUE) && (L[j] > 0)) cpl += L[j];
					else infinitePaths++;
				}//end if
			}//end for
		}//end for
		
		//return the characteristic path length
		System.out.println("CPL: " +  cpl/((double)(n * (n - 1.0d) - infinitePaths)));

	}//end calculateCPL()
}