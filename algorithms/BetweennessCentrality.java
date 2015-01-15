package algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import utils.Reset;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectProcedure;
import graph.Graph;
import graph.Vertex;

/**
 * @author Kirsty Williams
 */
public class BetweennessCentrality {
	/**
	 * Using the algorithm described by Ulrik Brandes from the University of
	 * Konstanz in Germany the method calculates the Betweenness Centrality
	 * value of each vertex in the passed Graph object.
	 * <p>
	 * Essentially the Betweenness Centrality values are computed in the
	 * following steps:
	 * <ol>
	 * <li>Compute the length and number of shortest paths between all pairs.</li>
	 * <li>Sum all pair-dependencies</li>
	 * </ol>
	 * <p>
	 * 
	 * @param Graph
	 *            - Graph object containing vertices to be analysed.
	 */
	private class BCAlgo implements TObjectProcedure<Vertex> {
		private final Reset resetZero = new Reset(0f, 0);
		private final Reset resetOne = new Reset(-1f, -1);
		private final Stack<Vertex> stackS = new Stack<Vertex>();
		
		private final EachNeighbour eachNeighbour;
		
		private final TObjectFloatMap<Vertex> pairDependencies;
		private final Queue<Vertex> queue;
		private final TObjectIntMap<Vertex> numShortestPaths;
		private final THashMap<Vertex, List<Vertex>> predecessors;
		private final TObjectFloatMap<Vertex> distance;

		public BCAlgo(final Graph G) {
			distance = new TObjectFloatHashMap<Vertex>(G.getNumVertices());
			predecessors = new THashMap<Vertex, List<Vertex>>(G.getNumVertices());
			numShortestPaths = new TObjectIntHashMap<Vertex>(G.getNumVertices());
			pairDependencies = new TObjectFloatHashMap<Vertex>(G.getNumVertices());

			if (G.isWeighted())
				queue = new PriorityQueue<Vertex>(G.getNumVertices(),
						new CompareDistance(distance));
			else
				queue = new ArrayDeque<Vertex>(G.getNumVertices());

			eachNeighbour = new EachNeighbour(distance, predecessors,
					numShortestPaths, queue);

			G.forEachVertex(new TObjectProcedure<Vertex>() {
				@Override
				public boolean execute(final Vertex v) {
					numShortestPaths.put(v, 0);
					distance.put(v, -1f);
					pairDependencies.put(v, 0f);
					betweennessMap.put(v, 0f);
					predecessors.put(v, new ArrayList<Vertex>());

					return true;
				}
			});
		}

		/**
		 * Calculates the pairwise dependencies of each vertex in the
		 * predecessors list for the passed Vertex object.
		 * <p>
		 * 
		 * @param vertex
		 *            - Vertex object to be analysed
		 */
		private final void calculateDependencies(final Vertex v) {
			for (final Vertex u : predecessors.get(v)) {
				pairDependencies.adjustValue(
						u,
						(numShortestPaths.get(u) / numShortestPaths.get(v))
								* (u.getEdge(v).getWeight() + pairDependencies
										.get(v)));
			}//end for
		}

		public final boolean execute(final Vertex v) {
			// Resets maps for new s
			predecessors.forEachValue(resetZero);
			numShortestPaths.transformValues(resetZero);
			distance.transformValues(resetOne);
			pairDependencies.transformValues(resetZero);

			// Init
			numShortestPaths.put(v, 1);
			distance.put(v, 0f);

			/*
			 * Finds shortest paths and list of predecessors for all vertices in
			 * the graph relative to vertex s.
			 */
			processQueue(v);

			// S returns vertices in order of non-increasing distance from s
			Vertex u;
			while (!stackS.empty()) {
				u = stackS.pop();

				calculateDependencies(u);

				// Update rankMap with new value for w.
				if (v != u)
					betweennessMap.adjustValue(u, pairDependencies.get(u));
			}

			return true;
		}

		/**
		 * Resets the queue for analysis of the new Vertex object passed then
		 * finds shortest paths and predecessors.
		 * <p>
		 * The method runs while to queue still contains Vertex objects. Each
		 * vertex gets removed from the queue and pushed to the stack. Then for
		 * each neighbour of the vertex if the neighbour is found for the first
		 * time then its distance is set. If it has been found before then the
		 * number of shortest paths is checked and updated.
		 * <p>
		 * 
		 * @param vertex
		 *            - Vertex object in the graph to be analysed
		 */
		private final void processQueue(final Vertex vSource) {
			queue.add(vSource);

			Vertex v;
			while (!queue.isEmpty()) {
				// deque v from queueQ
				v = queue.poll();

				// push v onto stackS
				stackS.push(v);

				// for each neighbour vertex u of v
				eachNeighbour.v = v;
				v.eachNeighbour(eachNeighbour);
			}
		}
	}
	private class CompareDistance implements Comparator<Vertex> {
		private final TObjectFloatMap<Vertex> distance;

		public CompareDistance(final TObjectFloatMap<Vertex> distance) {
			this.distance = distance;
		}

		/**
		 * Compares two vertices to determine witch has the lowest distance
		 * value.
		 * <p>
		 * 
		 * @return Int object.
		 */
		public final int compare(final Vertex v, final Vertex u) {
			final float p = distance.get(v);
			final float q = distance.get(u);

			if (p < q)
				return 1;
			else if (p > q)
				return -1;
			return 0;
		}
	}

	private final class EachNeighbour implements TObjectProcedure<Vertex> {
		public Vertex v;
		private final THashMap<Vertex, List<Vertex>> predecessors;
		private final TObjectIntMap<Vertex> numShortestPaths;
		private final TObjectFloatMap<Vertex> distance;

		private final Queue<Vertex> queue;

		public EachNeighbour(TObjectFloatMap<Vertex> distance,
				THashMap<Vertex, List<Vertex>> predecessors,
				TObjectIntMap<Vertex> numShortestPaths, Queue<Vertex> queue) {
			this.distance = distance;
			this.predecessors = predecessors;
			this.numShortestPaths = numShortestPaths;
			this.queue = queue;
		}

		@Override
		public final boolean execute(final Vertex u) {
			// check to see if undirected edge exists;
			final float uvDist = distance.get(v) + v.getEdge(u).getWeight();
			float uDist = distance.get(u);

			// is u found for the first time?
			if (uDist < 0) {
				queue.add(u);
				distance.put(u, uvDist);
				uDist = uvDist;
			}//end if

			// is the shortest path to u via v?
			if (uDist == uvDist) {
				numShortestPaths.adjustValue(u, numShortestPaths.get(v));
				predecessors.get(u).add(v);
			}//end if

			return true;
		}
	}

	private final TObjectFloatMap<Vertex> betweennessMap;

	public BetweennessCentrality(final Graph G) {
		betweennessMap = new TObjectFloatHashMap<Vertex>(G.getNumVertices());

		G.forEachVertex(new BCAlgo(G));

		if (!G.isDirected()) {
			for (final Vertex v : G.getVertices())
				betweennessMap.put(v, betweennessMap.get(v) / 2);
		}//end if
	}

	/**
	 * Get Betweenness Centrality scores for all vertices in the graph.
	 * 
	 * @return betweennessMap - TObjectFloatMap<Vertex>
	 */
	public final TObjectFloatMap<Vertex> getBetweennessMap() {
		return betweennessMap;
	}
}