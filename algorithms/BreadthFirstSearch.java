package utils;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import graph.Vertex;

import java.util.ArrayDeque;
import java.util.Queue;

/**
* @author Kirsty Williams
*/
public class BreadthFirstSearch {
	private TObjectIntMap<Vertex> depth;
	private int maxDepth = Integer.MIN_VALUE; // -inf
	private EachVertex eachVertex = new EachVertex();
	
	private class EachVertex implements TObjectProcedure<Vertex>
	{
		THashSet<Vertex> visited;
		int successorDepth;
		public Vertex v;
		public Queue<Vertex> queue;
		public int depthLimit;

		public boolean execute(Vertex u) {
			if (visited.contains(u))
				return true;

			if (!depth.containsKey(u)) {
				successorDepth = depth.get(v) + 1;
				
				if (successorDepth > getMaxDepth())
					setMaxDepth(successorDepth);
				
				if (successorDepth > depthLimit)
					return true;
				
				depth.put(u, successorDepth);
				queue.add(u);
			}
			return true;
		}
	}

	public final THashSet<Vertex> findAll(final Vertex s) {
		return findAll(s, Integer.MAX_VALUE, 255);
	}

	public final THashSet<Vertex> findAll(final Vertex s, final int totalVertices) {
		return findAll(s, Integer.MAX_VALUE, totalVertices);
	}

	public final THashSet<Vertex> findAll(final Vertex vSource, final int depthLimit, final int totalVertices) {
		if (vSource.getDegree() == 0)
			return emptyTree(vSource);
		
		final Queue<Vertex> queue = new ArrayDeque<Vertex>(totalVertices);
		final THashSet<Vertex> visited = new THashSet<Vertex>(totalVertices);
		
		depth = new TObjectIntHashMap<Vertex>(totalVertices, 0.5f, Integer.MIN_VALUE);
		setMaxDepth(Integer.MIN_VALUE); // reset
		
		queue.add(vSource);
		depth.put(vSource, 0);
		Vertex v;
		
		eachVertex.depthLimit = depthLimit;
		eachVertex.queue = queue;
		eachVertex.visited = visited;
		while (!queue.isEmpty()) {
			v = queue.poll();
			visited.add(v);

			// Enqueue successors
			eachVertex.v = v;
			v.eachNeighbour(eachVertex);
		}//end while
		return visited;	
	}

	private final THashSet<Vertex> emptyTree(Vertex vSource) {
		final THashSet<Vertex> visited = new THashSet<Vertex>(1);
		visited.add(vSource);
		
		return visited;
	}

	public final TObjectIntMap<Vertex> getDepth() {
		return depth;
	}

	public final int getDepth(final Vertex v) {
		return depth.get(v);
	}

	public final int getMaxDepth() {
		return maxDepth;
	}

	private final void setMaxDepth(final int maxDepth) {
		this.maxDepth = maxDepth;
	}
}
