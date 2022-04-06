package datastructures;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Input {
	public Tree I; 
	public Tree M;
	public String imgFile; 
	public String mdlFile;
	private int[] costI;
	private int[] costM;
	private HashMap<String, Integer> cost_ij = new HashMap<String, Integer>();
	private HashMap<String, Integer> cost_kl = new HashMap<String, Integer>();
			
	public void read(String imgFile, String mdlFile) throws FileNotFoundException {
		this.imgFile = imgFile;
		this.mdlFile = mdlFile;
		
		// first, read image 
		Scanner inI = new Scanner(new File(imgFile));
		int n = inI.nextInt();
		costI = new int[n];
		I = new Tree(n);
		
		for(int i = 0; i < n; i++) {
			costI[i] = inI.nextInt();
		}

		for(int i = 0; i < n-1; i++) {
			Edge e = new Edge(inI.nextInt(), inI.nextInt(), inI.nextInt());
			I.addEdge(e);
			cost_ij.put(e.toString(), e.cost);
		}
		
		inI.close();
		
		
		// read the model
		Scanner inM = new Scanner(new File(mdlFile));
		int m = inM.nextInt();
		costM = new int[m];
		M = new Tree(m);

		for(int i = 0; i < m; i++) {
			costM[i] = inM.nextInt();
		}

		for(int i = 0; i < m-1; i++) {
			Edge e = new Edge(inM.nextInt(), inM.nextInt(), inM.nextInt());
			M.addEdge(e);  
			cost_kl.put(e.toString(), e.cost);
		}
		
		inM.close();
	}
	
	enum VertexType {
		ROOT, FIRST_LEVEL, SECOND_LEVEL, NUMERIC;
	}
	
	static class Vertex {
		VertexType type;
		int subScript;
		int superScript;
		int n;
		
		Vertex(VertexType type) {
			this.type = type;
		}
	}
	
	Vertex getVertex(String lbl) {
		if(lbl.equals("r") || lbl.equals("q")) {
			return new Vertex(VertexType.ROOT);
		}
		
		if(!lbl.contains("_") && !lbl.contains("^")) {
			Vertex v = new Vertex(VertexType.NUMERIC);
			v.n = Integer.parseInt(lbl);
			return v;
		}
		
		if(!lbl.contains("^")) {
			Vertex v = new Vertex(VertexType.FIRST_LEVEL);
			v.subScript = Integer.parseInt(lbl.substring(lbl.indexOf('_') + 1));
			return v;
		}

		Vertex v = new Vertex(VertexType.SECOND_LEVEL);
		v.subScript = Integer.parseInt(lbl.substring (lbl.indexOf('_') + 1));
		v.superScript = Integer.parseInt(lbl.substring(lbl.indexOf('^') + 1, lbl.indexOf('_')));
		return v;
		
	}
	
	public double c(int i, int k) {
		return Math.abs(costI[i] - costM[k]);
	}
	
	public double f(int i, int j, int k) {
		return Math.abs(cost_ij.get(Edge.getKey(i, j)) - costM[k]);
	}

	public double d(int i, int j, int k, int l) {
		return Math.abs(cost_ij.get(Edge.getKey(i, j)) - cost_kl.get(Edge.getKey(k, l)));
	}
}
