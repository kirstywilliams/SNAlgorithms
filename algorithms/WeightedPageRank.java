package algorithms;

import utils.GetInDegree;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

/**
 * @author Kirsty Williams
 */
public final class WeightedPageRank {
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
		private THashSet<Vertex> allVertices;

		public TempRankMapProcedure(
				final THashMap<Vertex, THashSet<Vertex>> edgeMap, THashSet<Vertex> allVertices, final float K) {

			this.edgeMap = edgeMap;
			this.allVertices = allVertices;
			this.K = K;
		}

		/**
		 * Gets the first part of the new algorithm. (1-d)*(Vi/Sum(Vj) over all j.
		 * @param K (1-d)
		 * @param v Vertex
		 * @return (1-d)*(Vi/Sum(Vj) over all j.
		 * @author Thomas
		 */
		private float getWeightedNodeInclusion(float K, THashSet<Vertex> allVertices,Vertex v) {
			
			weightedVertexProcedure.weightSum = 0;
			allVertices.forEach(weightedVertexProcedure);
			float weightSum = weightedVertexProcedure.weightSum;
			
			return (K * (1.0f / weightSum)*v.getWeight());
		}

		public final boolean execute(final Vertex v) {
			tempRankMap.put(v, (DAMPING * rankSum(edgeMap.get(v), v) + getWeightedNodeInclusion(K, allVertices, v)));

			return true;
		}
	}

	/**
	 * Now includes a weight sum of vertices in the first part of the rankSum and an edge weight in the 2nd part.
	 * @author Thomas
	 *
	 */
	private final class CalcRankSumProcedure implements
			TObjectProcedure<Vertex> {
		public float rankSum = 0;
		public Vertex src;
		public float weightSum;

		//Altered from original PR to account for weighted edges between j and i.
		public final boolean execute(final Vertex v) {

			Edge e = v.getEdge(src);
			rankSum += (1.0f / weightSum) * (e.getWeight() * rankMap.get(v));

			return true;
		}
	}
	
	/**
	 * Calculates the total weight of all edges out of vertex v.
	 * @author Thomas
	 *
	 */
	private final class CalcWeightedOutSumProcedure implements
			TObjectProcedure<Vertex> {
		public float weightSum = 0;
		public Vertex src;
	
		//Gets weight sum for all edges from j to i.
		public final boolean execute(final Vertex v) {
		
			Edge e = v.getEdge(src);
			weightSum += e.getWeight();
		
			return true;
		}
	}
	
	/**
	 * Calculates the sum of all vertex weights of a vertices neighbours.
	 * @author Thomas
	 *
	 */
	private final class CalcVertexWeighSumProcedure implements
			TObjectProcedure<Vertex> {
		public float weightSum = 0;
		
		//Gets weight sum for all edges from j to i.
		public final boolean execute(final Vertex v) {
		
			weightSum += v.getWeight();
		
			return true;
		}
	}
	
	private final class NegatizeProcedure implements
			TObjectProcedure<Vertex> {
		public final boolean execute(final Vertex v) {
		
			float rank = rankMap.get(v);
			rankMap.put(v, rank*-1);
		
			return true;
		}
	}

	private static final float DAMPING = 0.85f;
	private static final int MAX_ITERATIONS = 50;

	private TObjectFloatMap<Vertex> rankMap;
	private final TObjectFloatMap<Vertex> tempRankMap;
	private final CalcRankSumProcedure rankSumProcedure = new CalcRankSumProcedure();
	private final CalcWeightedOutSumProcedure weightedOutProcedure = new CalcWeightedOutSumProcedure();
	private final CalcVertexWeighSumProcedure weightedVertexProcedure = new CalcVertexWeighSumProcedure();

	public WeightedPageRank(Graph G) {
		rankMap = new TObjectFloatHashMap<Vertex>(G.getNumVertices());
		tempRankMap = new TObjectFloatHashMap<Vertex>(G.getNumVertices());

		// Set vertex starting ranks to 1/n.
		G.getVertices().forEach(new InitRankMap(1.0f / G.getVertices().size()));

		// Get set of all edges in graph G
		final GetInDegree inDegree = new GetInDegree(G.getVertices());
		G.getEdges().forEach(inDegree);

		TempRankMapProcedure tempRankMapProcedure = new TempRankMapProcedure(
				inDegree.getEdgeMap(),G.getVertices(), (1.0f - DAMPING));

		for (int i = MAX_ITERATIONS; i != 0; i--) {
			G.getVertices().forEach(tempRankMapProcedure);
			rankMap = new TObjectFloatHashMap<Vertex>(tempRankMap);
		}//end for
		
		//NegatizeProcedure negatize = new NegatizeProcedure();
		//G.getVertices().forEach(negatize);
	}

	public static final void Peform(Graph G) {
		new WeightedPageRank(G);
	}

	/**
	 * This method calculates the PageRank for each vertex in the graph by
	 * iterating over all of the vertex's incoming edges. Algorithm uses edge and
	 * vertex weights.
	 * @param vNeighbours Set of v's Neighbours
	 * @param v Vertex
	 * @return rankSum
	 * @author Thomas
	 */
	private final float rankSum(final THashSet<Vertex> vNeighbours, Vertex v) {
		
		weightedOutProcedure.weightSum = 0;
		weightedOutProcedure.src = v;
		// Iterate over all incoming edges for v.
		vNeighbours.forEach(weightedOutProcedure);
		
		rankSumProcedure.rankSum = 0;
		rankSumProcedure.weightSum = weightedOutProcedure.weightSum;
		rankSumProcedure.src = v;
		// Iterate over all incoming edges for v.
		vNeighbours.forEach(rankSumProcedure);

		return rankSumProcedure.rankSum;
	}
	
	public final TObjectFloatMap<Vertex> getRankMap() {
		return rankMap;
	}
}