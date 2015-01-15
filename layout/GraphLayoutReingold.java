package graph.layout;

import java.awt.geom.Point2D;

import utils.StopWatch;

import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import graph.Edge;
import graph.Vertex;

/**
* @author Kirsty Williams
*/
public class GraphLayoutReingold extends DefaultGraphLayout implements
		GraphLayout {

	private static final int ITERATIONS = 1;
	private static final int ENERGY = 100;
	public static final int LENGTH_MAXIMUM = (ENERGY*5) * (ENERGY*5);
	public GraphLayoutReingold()
	{
		setName("Reingold");
	}
	
	private final class Push implements TObjectProcedure<Vertex> {

		private final class NestedIterator implements TObjectProcedure<Vertex> {
			private final THashMap<Vertex, Point2D> disp;
			private double vDiffX;
			private double vDiffY;
			
			private final double kSq = ENERGY * ENERGY;
			private Point2D vDisp;
			private Vertex v;

			private double len, mul;

			private double C;

			public NestedIterator(final THashMap<Vertex, Point2D> disp) {
				this.disp = disp;
			}

			public final boolean execute(final Vertex u) {
				if (v == u)
					return true;

				// Euclidean Distance
				vDiffX = v.getX() - u.getX();
				vDiffY = v.getY() - u.getY();
				
				len = (vDiffX * vDiffX)
						+ (vDiffY * vDiffY); // distance
				
				if (len > LENGTH_MAXIMUM) // Too far away, don't bother
					return true;
				
				mul = kSq / (len * C);
				if (Double.isInfinite(mul)){
					//System.out.println(kSq + ":" + len + ":" + C);
					mul = Math.random();
					//return true;
				}
				
				vDisp = disp.get(v);

				vDisp.setLocation(vDisp.getX() + (vDiffX * mul),
						vDisp.getY() + (vDiffY * mul));

				return true;
			}
		}

		private final THashSet<Vertex> V;
		private final NestedIterator nestedIterator;

		public Push(final THashMap<Vertex, Point2D> disp, final THashSet<Vertex> V) {
			this.V = V;
			this.nestedIterator = new NestedIterator(disp);
		}

		public final boolean execute(final Vertex v) {
			nestedIterator.v = v;
			V.forEach(nestedIterator);

			return true;
		}
	}

	private final class Pull implements TObjectProcedure<Edge> {
		private final THashMap<Vertex, Point2D> disp;

		private double vDiffX;
		private double vDiffY;
		private Point2D vDisp;

		private Vertex p, q;

		private double mul, C, len;
		private final double k = ENERGY;

		public Pull(final THashMap<Vertex, Point2D> disp) {
			this.disp = disp;
		}

		public final boolean execute(final Edge e) {
			p = e.getVertexFrom();
			q = e.getVertexTo();
			
			len = p.getEuclideanDistance(q);		
			
			mul = len / k / C;
			vDiffX = (p.getX() - q.getX()) * mul;
			vDiffY = (p.getY() - q.getY()) * mul;
			
			vDisp = disp.get(p);
			vDisp.setLocation(vDisp.getX() - vDiffX, vDisp.getY() - vDiffY);

			vDisp = disp.get(q);
			vDisp.setLocation(vDisp.getX() + vDiffX, vDisp.getY() + vDiffY);
			return true;
		}

	}

	private final class SetPositions implements TObjectProcedure<Vertex> {
		private final THashMap<Vertex, Point2D> disp;

		private Point2D vDisp;
		private double max = 10;
		private double len;
		private double div = 0;

		public SetPositions(final THashMap<Vertex, Point2D> disp) {
			this.disp = disp;
		}

		@Override
		public final boolean execute(final Vertex v) {
			vDisp = disp.get(v);
			len = vDisp.getX() * vDisp.getX() + vDisp.getY() * vDisp.getY();

			if (Math.sqrt(len) < 100)
				return true;
			if (len > max * max) {
				len = Math.sqrt(len);
				div = max / len;
				vDisp.setLocation(vDisp.getX() * div, vDisp.getY() * div);
			}
			
			v.setAddition(vDisp);
			return true;
		}
	}

	private final class SetInitial implements TObjectProcedure<Vertex> {
		private final THashMap<Vertex, Point2D> disp;

		public SetInitial(final THashMap<Vertex, Point2D> disp) {
			this.disp = disp;
		}

		public final boolean execute(final Vertex v) {
			disp.put(v, new Point2D.Double());
			
			return true;
		}
	}

	private final class EdgeReduce implements TObjectProcedure<Edge> {
		final THashSet<Edge> E;

		public EdgeReduce(final THashSet<Edge> E) {
			this.E = E;
		}

		public final boolean execute(final Edge e) {
			if (E.contains(e.getReverseEdge()))
				return true;
			
			E.add(e);
			
			return true;
		}

	}

	public final void updateLayout() {
		StopWatch SW = new StopWatch();
		SW.start();
		final THashSet<Edge> E = new THashSet<Edge>(this.E.size() / 2);
		
		final EdgeReduce edgeReduce = new EdgeReduce(E);
		this.E.forEach(edgeReduce);
		E.compact();
		V.compact();

		final THashMap<Vertex, Point2D> disp = new THashMap<Vertex, Point2D>(
				V.size());
		//final TIntFloatHashMap dispX = new TIntFloatHashMap(V.size());
		//final TIntFloatHashMap dispY = new TIntFloatHashMap(V.size());

		final SetInitial setInitial = new SetInitial(disp);
		
		final Push push = new Push(disp, V);
		final Pull pull = new Pull(disp);
		final SetPositions setPositions = new SetPositions(disp);

		final double C = 0.6931471805599453;
		push.nestedIterator.C = C;
		pull.C = C;
		V.forEach(setInitial);
		
		
		for (int i = ITERATIONS; i != 0; i--){
			V.forEach(push);
			E.forEach(pull);

			V.forEach(setPositions);
			System.out.println(i);
		}//end for
		System.out.println(SW.getElapsedTime());
	}
}