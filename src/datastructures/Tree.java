package datastructures;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Tree {
	public int root = -1;
	public List<Edge> edges = new LinkedList<Edge>();
	public int[] parent;
	public LinkedList<Integer>[] neighbours;
	public LinkedList<Integer>[] nonneighbours;
	public int size;
	
	// dual vars aggregated per vertex
	public double[] xi; 
	
	public Tree(int n) {
		size = n;
		parent = new int[n];
		neighbours = new LinkedList[n];
		xi = new double[n];
		
		for (int i = 0; i < n; i++) {
			neighbours[i] = new LinkedList<Integer>();
		}
	}
	
	private boolean flagNonneighbours;
	
	public void calcNonneibours() {
		if(flagNonneighbours) {
			return;
		}
		
		flagNonneighbours = true;
		nonneighbours = new LinkedList[size];

		for (int i = 0; i < size; i++) {
			nonneighbours[i] = new LinkedList<Integer>();
		}
		
		boolean[] flag = new boolean[size];
		
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				flag[j] = true;
			}

			flag[i] = false;
			
			for(int j : neighbours[i]) {
				flag[j] = false;
			}
			
			for(int j = 0; j < size; j++) {
				if(flag[j])
					nonneighbours[i].add(j);
			}
		}
	}
	
	public void addEdge(Edge e) {
		edges.add(e);
		neighbours[e.u].add(e.v);
		neighbours[e.v].add(e.u);
	}

	public void addEdge(int u, int v, int cost) {
		edges.add(new Edge(u, v, cost));
		neighbours[u].add(v);
		neighbours[v].add(u);
	}

	public void addEdge(int u, int v) {
		edges.add(new Edge(u, v, 0));
		neighbours[u].add(v);
		neighbours[v].add(u);
	}
	
	public void setRoot(int r) {
		root = r;
		parent[r] = -1;
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(r);
		boolean[] visited = new boolean[size];
		visited[r] = true;

		while (!queue.isEmpty()) {
			int u = queue.removeFirst();

			for (int v : neighbours[u]) {
				if (!visited[v]) {
					visited[v] = true;
					parent[v] = u;
					queue.addLast(v);
				}
			}
		}
	}

	public void changeRoot(int new_root) {
		if(new_root == root)
			return;
		
		int u = new_root;
	    int p_u = parent[new_root];

	    while(p_u != -1) {
	        int aux = parent[p_u];
	        parent[p_u] = u;
	        u = p_u;
	        p_u = aux;
	    }

	    parent[new_root] = -1;
	    root = new_root;
	}

	public boolean isleaf(int u) {
		return neighbours[u].size() == 1 && u != root;
	}
	
	public List<Integer> D(Integer u) {
		List<Integer> D_u = V_star(u);
		D_u.remove(u);
		return D_u;
	}
	
	public List<Integer> V_star(int u) {
	    LinkedList<Integer> v_star = new LinkedList<Integer>();
	    LinkedList<Integer> queue = new LinkedList<Integer>();
	    queue.addLast(u);
	    
	    while (!queue.isEmpty()) {
	        int j = queue.removeLast();
	        add_children(queue, j, v_star);
	    }
	    
	   return v_star;
	}

	private void add_children(LinkedList<Integer> queue, int j, LinkedList<Integer> v_star) {
		for(Integer i : neighbours[j]) {
			if(parent[i] == j) {
				queue.addLast(i);
				v_star.add(i);
			}
		}
	}
}
