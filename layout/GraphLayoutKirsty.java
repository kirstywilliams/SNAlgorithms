package graph.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import utils.QuadNode;
import utils.QuadTree;
import utils.Box;
import gnu.trove.map.hash.THashMap;
import graph.Graph;
import graph.Vertex;
import graph.vertex.DefaultVertex;

public class GraphLayoutKirsty extends DefaultGraphLayout implements
		GraphLayout {
	
	private final int SPACER = 5;
	private final double THETA = 1.1; //keep relatively close to one. Anything larger than 1.2 skews the layout
	private final int ITERATIONS = 1000;
	THashMap<Vertex, Point2D> old = new THashMap<Vertex, Point2D>();
	List<Point2D> forces = new ArrayList<Point2D>();
	
	/**
	 * Constructor - only use this for aspects related to the class, not the
	 * Graph The Graph will not exist when this is initialised
	 */
	public GraphLayoutKirsty() {
		setName("Kirstys Spring");
	}

	/** Implementation based on the Quigley, FADE algorithm.
	 * Quadtree is used for space decomposition followed by
	 * recursive force computation on vertices. A threshold
	 * is defined by s/d < theta. Where, s is the width of
	 * a quadrant in the tree, d is the distance between the
	 * vertex being considered and the centre of mass of the
	 * quadrant being looked at. */
	public final void updateLayout() {	
		
		for(int i=0; i<ITERATIONS; i++){
	        Rectangle2D bounds;
	        bounds = Graph.getBounds(V);
	        
	    	QuadTree<Vertex> tree = new QuadTree<Vertex>(bounds.getMinX() - SPACER, bounds.getMinY() - SPACER, bounds.getMaxX() + SPACER, bounds.getMaxY() + SPACER);
	    	//Add vertices to the quadtree
	    	for(Vertex v : V){    		
	    		tree.put(v.getX(), v.getY(), v);
	    	}  	
	    	
	    	//Map each vertex with it's original location
	    	for(Vertex v : V){
	    		old.put(v, v.getLocation());
	    	}
	
	    	//Calculate the centre of mass of each quadnode in the tree
	    	calculateCOM(tree.root);
	    	
	    	//Compute force on each vertex
			for(Vertex v : V){
				double posX = 0;
				double posY = 0;
				forces = new ArrayList<Point2D>();
				computeForce(v, tree.root, forces);
				for(Point2D p : forces){
					posX += p.getX();
					posY += p.getY();
				}
	
				//update the location of the vertex
				v.setLocation(old.get(v).getX() + posX, old.get(v).getY() + posY);
	    	}
			//Clear the tree for next iteration
			tree.clear();
		}
	}
	
	
	/* Calculates the centre of mass of a quadrant */
	private void calculateCOM(QuadNode<Vertex> parent){
		double comX = 0; //centre of mass, x coord
		double comY = 0; //centre of mass, y coord
		
		//values in the given quadrant
		ArrayList<Vertex> values = new ArrayList<Vertex>();
		values = parent.get(parent.getBounds(), values);
		
		if(values.size() == 1){
			parent.setCOM(values.get(0).getX(), values.get(0).getY());
		} else{
			if(parent.NE != null)
				calculateCOM(parent.NE);
			if(parent.NW != null)
				calculateCOM(parent.NW);
			if(parent.SE != null)
				calculateCOM(parent.SE);
			if(parent.SW != null)
				calculateCOM(parent.SW);
		
		    for(Vertex vert : values){
		    	comX += vert.getX();
		    	comY += vert.getY();
		    }
		    
		    comX = comX / parent.getMass();
		    comY = comY / parent.getMass();
			parent.setCOM(comX, comY);;
		}
	}
     
	private void computeForce(Vertex v, QuadNode<Vertex> node, List<Point2D> f){
		Box bounds = node.getBounds();
    	ArrayList<Vertex> values = new ArrayList<Vertex>();
    	values = node.get(bounds, values);

		double vx = v.getX();
		double vy = v.getY();
		double dx = node.comX - vx;
		double dy = node.comY - vy;
		double d = Math.sqrt(dx*dx + dy*dy);
		double s = bounds.maxX - bounds.minX;
		
		Graph sub = new Graph();
		Vertex theVertex = new DefaultVertex(vx, vy, 1);
		
		if(!node.hasChildren && (values.size()>0)){
			Vertex leafVertex = new DefaultVertex(values.get(0).getX(), values.get(0).getY(), 1);
			//We're at a leaf node (containing a single vertex)
			if(values.get(0) != v){
				sub.addVertex(theVertex);
				sub.addVertex(leafVertex);
				if(v.isConnected(values.get(0))){
					sub.createEdge(theVertex, leafVertex);
				}
				
				GraphLayoutReingold GLR = new GraphLayoutReingold();
				GLR.setVertices(sub.getVertices());
				GLR.setEdges(sub.getEdges());
				GLR.updateLayout();
				double diffX = theVertex.getX() - old.get(v).getX();
				double diffY = theVertex.getY() - old.get(v).getY();
				
				Point2D transV = new Point2D.Double(diffX, diffY);
				f.add(transV);
			}
		} else if(s/d < THETA){
			//threshold reached, compare with cluster of 
			//nodes (i.e. the centre of mass)
			sub.addVertex(theVertex);
			Vertex nodeVertex = sub.createVertex(node.comX, node.comY, 1);
			if(isConnection(v, values)){
				sub.createEdge(theVertex, nodeVertex);
			}
			
			GraphLayoutReingold GLR = new GraphLayoutReingold();
			GLR.setVertices(sub.getVertices());
			GLR.setEdges(sub.getEdges());
			GLR.updateLayout();
			double diffX = theVertex.getX() - old.get(v).getX();
			double diffY = theVertex.getY() - old.get(v).getY();
			
			Point2D trans = new Point2D.Double(diffX, diffY);
			f.add(trans);
			
		} else if(node.hasChildren && !(s/d < THETA )){
			//recurse the children nodes
			if(node.NE != null)
				computeForce(v, node.NE, f);
			
			if(node.SE != null)
				computeForce(v, node.SE, f);
			
			if(node.SW != null)
				computeForce(v, node.SW, f);
			
			if(node.NW != null)
				computeForce(v, node.NW, f);
			
		}		
    }
	
	//check whether an edge exists between a vertex and a 
	//collection of vertices
    private boolean isConnection(Vertex v, ArrayList<Vertex> u){
    	boolean connected = false;
    	
    	for(int i=0; i< u.size(); i++){
    		Vertex dest = u.get(i);
    		
    		if(v.isConnected(dest)){
    			connected = true;
    			return connected;
    		}
    	}
    	return connected;
    }
}