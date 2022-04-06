package datastructures;

public class Edge {
	public int u;
	public int v;
	public int cost;
	
	// dual vars aggregated per edge
	public double rho;
	public double rho_plus_u_leq_v;
	public double rho_plus_u_geq_v;
	
	public Edge(int u, int v, int cost) {
		this.u = Math.min(u, v);
		this.v = Math.max(u, v);
		this.cost = cost;
	}

	public Edge(int u, int v) {
		this.u = Math.min(u, v);
		this.v = Math.max(u, v);
	}

	@Override
	public String toString() {
		return getKey(u, v);
	}
	
	public String getKey() {
		return getKey(u, v);
	}
	
	public static String getKey(int u, int v) {
		return u < v ? u + "," + v : v + "," + u;
	}
}
