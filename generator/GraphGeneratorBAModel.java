package graph.generator;

import graph.Graph;
import graph.Vertex;
import gui.EdgeType;
import gui.JPanelCanvas;
import gui.TabManager;

import java.awt.GridLayout;
import java.util.Random;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utils.GetInDegree;

/**
 *  This model is used to create random networks with 
 *  the scale-free property as suggested by Barabasi
 *  & Albert (1999). The algorithm uses the preferential 
 *  attachment property.  
 *  
 *  @author Kirsty Williams
 */
public class GraphGeneratorBAModel extends DefaultGraphGenerator implements GraphGenerator {	
	public GraphGeneratorBAModel() {
		setName("BAModel");
		setGraphLayout("GraphLayoutRandom");
	}//end constructor
	
	/**
	 * This method sets up the gui component for 
	 * the model.
	 */
	public JComponent getGUI() {
		guiControls.put("N", new JTextField("25", 5)); //number of vertices in final graph
		guiControls.put("M0", new JTextField("5", 5)); //number of vertices in initial graph
		guiControls.put("K", new JTextField("4", 5)); //number of edges connected to new vertex
		guiControls.put("Directed", new JCheckBox("Directed", true));
		
		JPanel panel = new JPanel(false);
		panel.setPreferredSize(PREFERED_SIZE);
		panel.setLayout(new GridLayout(0, 2));

		for (Entry<String, JComponent> e : guiControls.entrySet()) {
			panel.add(new JLabel(e.getKey()));
			panel.add(e.getValue());
		}//end for
		return panel;
	}//end getGUI()
	
	/**
	 * 	This method parses the input from the user.
	 *  @param G - the graph
	 */
	public void parseGUI(Graph G)
	{
		int n = 0; //number of vertices in final graph as input by user
		int m0 = 0; //number of vertices in initial graph as input by user
		int k = 0; //number of edges for new vertex as input by user
		boolean isDirected = false; //generate a directed graph or not
		
		for (Entry<String, JComponent> e : guiControls.entrySet()) {
			if (e.getKey() == "N")
				n = getInteger((JTextField) e.getValue()); //get num of vertices in final graph entered by user
			else if (e.getKey() == "M0")
				m0 = getInteger((JTextField) e.getValue()); //get num of vertices in initial graph entered by user
			else if (e.getKey() == "K")
				k = getInteger((JTextField) e.getValue()); //get num of edges entered by user
			else if (e.getKey() == "Directed")
				isDirected = getBoolean((JCheckBox) e.getValue());	 //get users selected graph type
		}//end for
		
		if(isDirected){
	  		JPanelCanvas canvas = TabManager.getActiveCanvas();
	  		canvas.setEdgeType(EdgeType.DIRECTED);
	  	}else{
	  		JPanelCanvas canvas = TabManager.getActiveCanvas();
	  		canvas.setEdgeType(EdgeType.UNDIRECTED);
	  	}
		
		generate(G, n, m0, k, isDirected); //generate graph with selected values
	}//end parseGUI()
	
	/**
	 * This method generates the graph according to
	 * the definition of the model proposed by Barabasi
	 * & Albert (1999).
	 * An intial complete network is composed of (m0) 
	 * many vertices. At each time step afterwards a single 
	 * vertex is added to the graph, until (n) many vertices 
	 * have been added. When a vertex is added, it is given 
	 * (k)-many initial edges, the endpoint of each edge is 
	 * chosen using preferential attachment. The probability 
	 * of an existing vertex u being selected is; 
	 * <ul><li>degree(u)/ sum(degrees of existing vertices)</li></ul>  
	 * In this way, vertices with more edges get more edges 
	 * and vertices with few edges, remain with low degree.
	 * In the case of directed edges, the variable 'r' is used
	 * to determine the likelihood that a mutual relationship
	 * exists between the new vertex and the old vertex it
	 * is connected to. If not, a new vertex is found to connect
	 * to the new vertex.
	 * @param G - the graph
	 * @param n - the number of vertices in final graph
	 * @param m0 - the number of vertices in initial graph
	 * @param k - the number of edges connecting the new 
	 * vertex to pre-existing vertices
	 * @param isDirected - whether the generated graph is
	 * to be directed or not
	 */
	public void generate(Graph G, int n, int m0, int k, boolean isDirected) {
		G.empty();
		G.ensureCapacity(n, n);
		Vertex[] vArray = new Vertex[n];
		Random rand = new Random();	
		GetInDegree inDegree;
		int[] degrees = new int[n];
		int[] indegrees = new int[n];
		
		//Make sure m0 is feasible
		if(m0 <= 1)
			throw new IllegalArgumentException("Number of initial unconnected vertices " + 
					m0 + " must be greater than 1");
		if(m0 > n){
			System.out.println("m0 (initial graph size) must be less than or equal to " +
					"final graph size: changing from " + m0 + " to " + n);
			m0 = n;
		}//end if
		if(k <= 0)
			throw new IllegalArgumentException("Number of edges to attach " + 
					k + " must be greater than 0");
		
		//(1) Create initial graph (fully connected)
		//Create m0 vertices and store pointer in array
	  	for (int i = 0; i < m0; i++){
	  		Vertex v = G.createVertex(0);
	  		vArray[i] = v;
	  		degrees[i] = 0;
	  		indegrees[i] = 0;
	  	}//end for
	  	  	
	  	for(int i=0; i<m0;i++){
	  		for(int j=i+1; j<m0;j++){
	  			if(!isDirected){
	  				G.createEdge(vArray[i], vArray[j]);
	  			}else{
	  				G.createDirectedEdge(vArray[i], vArray[j]);
	  				G.createDirectedEdge(vArray[j], vArray[i]);
	  			}//end if
	  			inDegree = new GetInDegree(G.getVertices());
  				G.getEdges().forEach(inDegree);
  				indegrees[i] = inDegree.getEdgeMap().get(vArray[i]).size();				
  				indegrees[j] = inDegree.getEdgeMap().get(vArray[j]).size();
	  		}//end for
	  	}//end for
	  	
	  	//(2) Add remaining edges
	  	int temp = 0;
	  	if(!isDirected){
	  		for(int i=m0;i<n;i++){
	  			//create the new vertex
	  			Vertex v = G.createVertex(0);
	  			//add pointer to new vertex to array
	  			vArray[i] = v;
	  			
	  			//impossible to add k edges if k is bigger than
	  			//number of vertices - 1 in the graph so temporarily
	  			//set number of edges to be added to the number of
	  			//vertices in the graph - 1.
	  			if(k > G.getNumVertices() - 1) temp = G.getNumVertices() - 1;
	  			else temp = k;
	  			
	  			//connect to k many edges
	  			int added = 0;
	  			while (added < temp){
	  				boolean createdEdge = false;
	  				
	  				while(!createdEdge){
	  					int j = rand.nextInt(G.getNumVertices() - 1);
	  		  			while (j == i || vArray[i].getEdge(vArray[j]) != null){
	  		  				j = rand.nextInt(G.getNumVertices()- 1);;
	  		  			}//end while
	  		  			
	  		  			double prob = (double)indegrees[j] / (getSum(i, indegrees));

	  		  			if(prob > rand.nextDouble()){
	  		  				G.createEdge(vArray[i], vArray[j]);
		  		  			inDegree = new GetInDegree(G.getVertices());
			  				G.getEdges().forEach(inDegree);
			  				indegrees[i] = inDegree.getEdgeMap().get(vArray[i]).size();				
			  				indegrees[j] = inDegree.getEdgeMap().get(vArray[j]).size();
	  		  				createdEdge = true;
	  		  			}//end if
	  				}//end while
	  				added++;
	  			}//end while
	  		}//end for
	  	}else{
	  		for(int i=m0;i<n;i++){
	  			//create new vertex
	  			Vertex v = G.createVertex(0);
	  			//add pointer to new index to array
	  			vArray[i] = v;
	  			
	  		    //impossible to add k edges if k is bigger than
	  			//number of vertices - 1 in the graph so temporarily
	  			//set number of edges to be added to the number of
	  			//vertices in the graph - 1.
	  			if(k > G.getNumVertices()-1) temp = G.getNumVertices() - 1;
	  			else temp = k;
	  			
	  			//connect k many edges
	  			int added = 0;
	  			while(added<temp){
	  				boolean createdEdge = false;
	  				
	  				while(!createdEdge){	  		  			
	  					int j = rand.nextInt(G.getNumVertices() - 1);
	  		  			while ((vArray[j] == vArray[i]) || (vArray[i].getEdge(vArray[j]) != null)){
	  		  				j = rand.nextInt(G.getNumVertices() - 1);
	  		  			}//end while  		  			
	  		  			
	  		  			double prob = (double)indegrees[j] / (getSum(i, indegrees));
	  		  			if(prob > rand.nextDouble()){
	  		  				
  		  					G.createDirectedEdge(vArray[i], vArray[j]);
  		  				
	  		  				inDegree = new GetInDegree(G.getVertices());
	  		  				G.getEdges().forEach(inDegree);			
	  		  				indegrees[j] = inDegree.getEdgeMap().get(vArray[j]).size();
			  				
	  		  				boolean noEdge = true;
		  		  			while(noEdge){			  		  				
			  		  			int h = rand.nextInt(G.getNumVertices() - 1);
			  		  			boolean hOk = false;
			  		  			while (!hOk){
			  		  				if(h != i){
			  		  					if(vArray[h].getEdge(vArray[i]) != null){
			  		  						if(vArray[h].getEdge(vArray[i]).getVertexTo() == vArray[i]){
			  		  							h = rand.nextInt(G.getNumVertices()- 1);
			  		  						}else{
					  		  					hOk = true;
					  		  				}//end if
			  		  					}else{
				  		  					hOk = true;
				  		  				}//end if
			  		  				}else{
			  		  					h = rand.nextInt(G.getNumVertices()- 1);
			  		  				}//end if
			  		  			}//end while
	  		  					double prob2 = (double)indegrees[h] / (getSum(i, indegrees));
		  			  			
	  		  					if(prob2 > rand.nextDouble()){
		  			  				G.createDirectedEdge(vArray[h], vArray[i]);
		  			  					  			  				
			  			  			inDegree = new GetInDegree(G.getVertices());
			  		  				G.getEdges().forEach(inDegree);
			  		  				indegrees[i] = inDegree.getEdgeMap().get(vArray[i]).size();	
			 
		  			  				noEdge = false;
		  			  			
		  			  			}else{
		  			  				noEdge = true;
		  			  			}//end if
	  		  				}//end while
		  		  			
	  		  			createdEdge = true;
	  		  				
	  		  			}//end if
	  				}//end while
	  				
	  				added++;
	  				
	  			}//end while
	  		}//end for
	  	}//end if
	}//end generate()		
	
	/**
	 * This method computes the sum of all degrees
	 * of pre-existing vertices such that
	 * sum_degs = degrees(0) +...+ degrees(newVertex - 1)
	 * @param vertex - the vertex being added
	 * @param degrees - the array of vertex degrees
	 * @return the sum of degrees of pre-existing vertices 
	 */
	private double getSum(int vertex, int[] deg){
		double sum = 0.0;
		for(int i=0; i<vertex; i++){
			sum+= deg[i];
		}
		return sum;
	}//end getSum()	
	
	@Override
	public void parseCMD(Graph G, int x, int y, String[] args) {
		// TODO Auto-generated method stub
		
	}
}