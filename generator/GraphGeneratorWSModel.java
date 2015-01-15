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

/**
 *	This model is used to create random networks with the 
 *	small world property.  A regular lattice is formed, 
 *  and then randomly rewired according to the variable beta.
 *  
 *  @author Kirsty Williams
 **/
public class GraphGeneratorWSModel extends DefaultGraphGenerator implements GraphGenerator {
	public GraphGeneratorWSModel() {
		setName("WSModel");
		setGraphLayout("GraphLayoutEllipse");
	}//end constructor
	
	public JComponent getGUI() {
		guiControls.put("N", new JTextField("25", 5)); //number of vertices
		guiControls.put("K", new JTextField("5", 5)); //number of edges for each vertex
		guiControls.put("P", new JTextField("0.5", 5)); //rewiring probability
		guiControls.put("Directed", new JCheckBox("Directed", true)); //directed graph or not
		
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
		int n = 0; //number of vertices as input by user
		int k = 0; //number of edges for each vertex as input by user
		double p = 0; //rewiring probability as entered by user
		boolean isDirected = false; //generate a directed graph or not
		
		for (Entry<String, JComponent> e : guiControls.entrySet()) {
			if (e.getKey() == "N")
				n = getInteger((JTextField) e.getValue()); //get num of vertices entered by user
			else if (e.getKey() == "K")
				k = getInteger((JTextField) e.getValue()); //get num of edges entered by user
			else if (e.getKey() == "P")
				p = getDouble((JTextField) e.getValue()); //get rewiring probability entered by user
			else if (e.getKey() == "Directed")
				isDirected = getBoolean((JCheckBox) e.getValue());	//get users selected graph type
		}//end for
		
	  	if(isDirected){
	  		JPanelCanvas canvas = TabManager.getActiveCanvas();
	  		canvas.setEdgeType(EdgeType.DIRECTED);
	  	}else{
	  		JPanelCanvas canvas = TabManager.getActiveCanvas();
	  		canvas.setEdgeType(EdgeType.UNDIRECTED);
	  	}
	  	
		generate(G, n, k, p, isDirected); //generate graph with selected values
	}//end parseGUI()

	/**
	 * This method generates the graph according to
	 * the definition of the model proposed by Watts
	 * & Strogatz (1998)
	 * @param G - the graph
	 * @param n - the number of vertices
	 * @param k - the number of edges connected to each 
	 * vertex in the initial graph 
	 * @param p - the probability that an edge will be 
	 * rewired
	 * @param isDirected - whether the generated graph 
	 * is a directed graph or not
	 */
	public void generate(Graph G, int n, int k, double p, boolean isDirected) {
		G.empty();
		G.ensureCapacity(n, n);
		Vertex[] vArray = new Vertex[n];
		double r = 0.5; //Likelihood that any two vertices are connected in both directions
		Random rand = new Random();
		
		//Make sure k is feasible
		if (k % 2 != 0) {
			System.out.println("K (degree) must be an even integer: changing from " + k + " to " + (k-1));
	        k = k-1;
	    }//end if
	    if (k < 0 || k > n-1)
	    	throw new IllegalArgumentException("K (degree) outside of range [0, " + (n-1) + "]");
	    //Make sure p is feasible
	    if (p < 0 || p > 1)
	    	throw new IllegalArgumentException("Invalid rewiring parameter = " + p + " (should be between 0 and 1)");
		
		//(1) Generate initial graph
	    //Wires a ring lattice. The added edges are defined as follows. 
	    //Edges to i-k/2, i-k/2+1, ..., i+k/2 are added (but not to i).
	    
	    //Create n vertices and store pointer in array
	  	for (int i = 0; i < n; i++){
	  		Vertex v = G.createVertex(0);
	  		vArray[i] = v;
	  	}
	  	
	  	//Add k edges to each vertex
	  	for(int i=0; i < n; i++){
	  		for(int j=1; j<= k/2; j++){
	  			if(j > n) j = j - n;
	  			if(!isDirected){
	  				G.createEdge(vArray[i], vArray[(i+j)%n]);
	  			}else{
	  				G.createDirectedEdge(vArray[i], vArray[(i+j)%n]);
	  				G.createDirectedEdge(vArray[(i+j)%n], vArray[i]);
	  			}//end if
	  		}//end for
	  	}//end for
	  	
	  	
	  	//(2)Rewiring the edges
	  	for(int i=0; i<n; i++){
	  		for(int j=1; j <= k/2; j++){
	  			if(j > n) j = j - n;
	  			double chance = rand.nextDouble();
	  			if(p > chance){
  					int h = rand.nextInt(n - 1);
  					while((h == i) || (vArray[i].getEdge(vArray[h]) !=null) || (vArray[h].getEdge(vArray[i]) != null))
  						h = rand.nextInt(n - 1);
  					
  					if(!isDirected){
  						G.deleteEdge(vArray[i], vArray[(i+j)%n]);
  						G.createEdge(vArray[i], vArray[h]);
  					}else{
  						//delete both edges connecting both nodes
  						//if the random number ('chance') is less 
  						//than 'r' (the chance of a two-way connection 
  						//existing between any two given nodes)
  						//reconnect node i with random node 'h' (in
  						//both directions). Else create outwards edge
  						//from i to random node 'h' and create inwards 
  						//edge to i from another random node 'l'.
  						G.deleteEdge(vArray[i], vArray[(i+j)%n]);
  						G.deleteEdge(vArray[(i+j)%n], vArray[i]);
  					
  						chance = rand.nextDouble();
  						if(r > chance){
  							G.createDirectedEdge(vArray[i], vArray[h]);
  							G.createDirectedEdge(vArray[h], vArray[i]);
  						}else{
  							G.createDirectedEdge(vArray[i], vArray[h]);
  					
  							int l = rand.nextInt(n - 1);
  		  					while((l == i) || (vArray[l].getEdge(vArray[i]) != null))
  		  						l = rand.nextInt(n - 1);
  		  					
  		  					G.createDirectedEdge(vArray[l], vArray[i]);
  		  				}//end if
  					}//end if
	  			}//end if
	  		}//end for
	  	}//end for	
	}//end generate()	
	
	@Override
	public void parseCMD(Graph G, int x, int y, String[] args) {
		// TODO Auto-generated method stub
	}
}