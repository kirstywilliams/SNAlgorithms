package algorithms;

import utils.GetInDegree;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;

import graph.Graph;
import graph.Vertex;

/**
 * @author Kirsty Williams
 */
public final class PageRank {
	private final class InitRankMap implements TObjectProcedure<Vertex> {
		public final float INIT_VALUE;

		public InitRankMap(final float initialiser) {
			INIT_VALUE = initialiser;
		}

		public final boolean execute(final Vertex v) {
			rankMap.put(v, INIT_VALUE);
			return true;
		}
	}

	private final class TempRankMapProcedure implements
			TObjectProcedure<Vertex> {
		public final THashMap<Vertex, THashSet<Vertex>> edgeMap;
		public final float K;

		public TempRankMapProcedure(
				final THashMap<Vertex, THashSet<Vertex>> edgeMap, final float K) {

			this.edgeMap = edgeMap;
			this.K = K;
		}

		public final boolean execute(final Vertex v) {
			tempRankMap.put(v, (DAMPING * rankSum(edgeMap.get(v)) + K));

			return true;
		}
	}

	private final class CalcRankSumProcedure implements
			TObjectProcedure<Vertex> {
		public float rankSum = 0;

		public final boolean execute(final Vertex v) {
			rankSum += (1.0f / v.getDegree()) * rankMap.get(v);

			return true;
		}
	}

	private static final float DAMPING = 0.85f;
	private static final int MAX_ITERATIONS = 50;

	private TObjectFloatMap<Vertex> rankMap;
	private final TObjectFloatMap<Vertex> tempRankMap;
	private final CalcRankSumProcedure rankSumProcedure = new CalcRankSumProcedure();

	public PageRank(Graph G) {
		rankMap = new TObjectFloatHashMap<Vertex>(G.getNumVertices());
		tempRankMap = new TObjectFloatHashMap<Vertex>(G.getNumVertices());

		// Set vertex starting ranks to 1/n.
		G.getVertices().forEach(new InitRankMap(1.0f / G.getVertices().size()));

		// Get set of all edges in graph G
		final GetInDegree inDegree = new GetInDegree(G.getVertices());
		G.getEdges().forEach(inDegree);

		TempRankMapProcedure tempRankMapProcedure = new TempRankMapProcedure(
				inDegree.getEdgeMap(), (1.0f - DAMPING)
						/ G.getVertices().size());

		for (int i = MAX_ITERATIONS; i != 0; i--) {
			G.getVertices().forEach(tempRankMapProcedure);
			rankMap = new TObjectFloatHashMap<Vertex>(tempRankMap);
		}//end for
	}

	public static final void Peform(Graph G) {
		new PageRank(G);
	}

	// This method calculates the PageRank for each vertex in the graph by
	// iterating over all of the vertex's incoming edges.
	private final float rankSum(final THashSet<Vertex> vNeighbours) {
		// Iterate over all incoming edges for v.
		rankSumProcedure.rankSum = 0;
		vNeighbours.forEach(rankSumProcedure);

		return rankSumProcedure.rankSum;
	}
	
	public final TObjectFloatMap<Vertex> getRankMap() {
		return rankMap;
	}
}