package datastructures;
import java.util.LinkedList;


public class HyperGraph {
	public int nU, nV, nH;
	public LinkedList<HyperEdge> edges = new LinkedList<HyperEdge>();
	
	public HyperGraph(int nU, int nV, int nH) {
		this.nU = nU;
		this.nV = nV;
		this.nH = nH;
	}

	public void addEdge(HyperEdge e) {
		edges.add(e);
	}
	
	public void addEdge(int u, int v, int h, double cost) {
		addEdge(new HyperEdge(u, v, h, cost));
	}
}
