package utils;

import graph.Vertex;

import java.util.Set;

/**
* @author Kirsty Williams
*/
public class FloydWarshall {
	private int[][] matrix;
	private double avg = 0;
	public Vertex vLargest;
	
	public FloydWarshall(Set<Vertex> V) {
		matrix = new int[V.size()][V.size()];
		int largestCycle = 0;
		int currentCycle = 0;
		int vLargestCycle = 0;
		int i = 0;
		int j = 0;
		for (Vertex v : V) {
			j = 0;
			currentCycle = (int) v
					.getProperty("Cycle Length (E)");
			if (currentCycle > largestCycle) {
				largestCycle = currentCycle;
				vLargestCycle = i;
				vLargest = v;
			}//end if
			for (Vertex u : V) {
				if (v == u)
					matrix[i][j] = 0;
				else if (v.isConnected(u)) {
					matrix[i][j] = currentCycle;
				} else
					matrix[i][j] = Integer.MAX_VALUE;
				j++;
			}//end for
			i++;
		}//end for

		int vu;
		int vk;
		int ku;
		int newD;
		for (int h = 0; h < V.size(); h++) {
			for (i = 0; i < V.size(); i++) {
				for (j = 0; j < V.size(); j++) {
					vu = matrix[i][j];
					vk = matrix[i][h];
					ku = matrix[h][j];

					if (vk == Integer.MAX_VALUE || ku == Integer.MAX_VALUE)
						newD = vu;
					else
						newD = Math.min(vu, vk + ku);
					matrix[i][j] = newD;
				}//end for
			}//end for
		}//end for
		avg = 0;
		for (int[] tmp : matrix)
			avg += tmp[vLargestCycle];
		
		avg /= V.size() - 1;
	}

	public double getAvg() {
		return avg;
	}
}
