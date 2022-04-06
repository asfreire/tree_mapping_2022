package main;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import datastructures.Edge;
import datastructures.HyperEdge;
import datastructures.HyperGraph;
import datastructures.Tree;



public class TreeGenerator {
	Random rand = new Random();
	
	// https://proofwiki.org/wiki/Labeled_Tree_from_Prüfer_Sequence
	LinkedList<Edge> genLblTreeFromPruferSeq(int n) {
		LinkedList<Edge> edges = new LinkedList<Edge>();
		
		// Step 0:
		/*
			A Prüfer sequence of order n is a (finite) sequence of integers: (a_1, a_2, ..., a_{n-2}),
			such that 0 <= a_i <= n-1, for each i = 1, ..., n-2
			That is, it is a (finite) sequence of n−2 integers between 1 and n.
			Let P=(a_1, a_2, ..., a_n) be a Prüfer sequence. This will be called the sequence.
		 */
		LinkedList<Integer> sequence = new LinkedList<Integer>();
		
		for(int i = 0; i < n-2; i++) {
			sequence.add(rand.nextInt(n));
		}
		
		//Step 1: Draw the n nodes of the tree we are to generate, and label them from 0 to n-1. This will be called the tree.

		// no code needed...
		
		
		//Step 2: Make a list of all the integers (0,1,..., n-1). This will be called the list.
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		for(int i = 0; i < n; i++) {
			list.addLast(i);
		}
		
		while(true) {
			//Step 3: If there are two numbers left in the list, connect them with an edge and then stop. 
			//        Otherwise, continue on to step 4.
			if(list.size() == 2) {
				edges.add(new Edge(list.getFirst(), list.getLast()));
				break;
			}
			
			//Step 4: Find the smallest number in the list which is not in the sequence, 
			//        and also the first number in the sequence. 
			//        Add an edge to the tree connecting the nodes whose labels correspond to those numbers.
			int i_u = 0;
			for(int u : list) {
				if(!sequence.contains(u)) {
					edges.add(new Edge(u, sequence.getFirst()));
					break;
				}
				
				i_u++;
			}
			
			//Step 5: Delete the first of those numbers from the list and the second from the sequence. 
			//        This leaves a smaller list and a shorter sequence. Then return to step 3.
			sequence.removeFirst();
			list.remove(i_u);
		}
		
		return edges;
	}
	
	// density from 0 to 100
	HyperGraph genRandomHiperGraph(int nU, int nV, int nH, int maxEdgeCost, int density) {
		HyperGraph H = new HyperGraph(nU, nV, nH);
		int maxE = nU * nV * nH;
		int nE = (maxE * density) / 100; 
		
		for(int i = 0; i < nE; i++) {
			// vertices are labeled from 0 to nU + nV + nH - 1
			int u = rand.nextInt(nU);
			int v = nU + rand.nextInt(nV);
			int h = nU + nV + rand.nextInt(nH);
			H.addEdge(u, v, h, rand.nextInt(maxEdgeCost) + 1);
		}
		
		return H;
	}
	
	void writeImgMdlFrom3DMatching(HyperGraph H, String fileName) throws IOException {
		int r = 0, q = 0;
		Tree I = new Tree(H.edges.size() * 4 + 1);
		Tree M = new Tree(H.edges.size() * 4 + 1 + H.nU + H.nV + H.nH);
		int nextLbl_I = 0;
		int nextLbl_M = 0;
		int nE = 0;
		String[] lbl_I = new String[I.size]; 
		String[] lbl_M = new String[M.size];
		lbl_I[nextLbl_I++] = "r";
		lbl_M[nextLbl_M++] = "q";
		
		for(HyperEdge e : H.edges) {
			int u, v, h, a, d;

			// Image
			lbl_I[a = nextLbl_I++] = "a_" +  nE;
			lbl_I[u = nextLbl_I++] = "a^" +  nE + "_" + e.u;
			lbl_I[v = nextLbl_I++] = "a^" +  nE + "_" + e.v;
			lbl_I[h = nextLbl_I++] = "a^" +  nE + "_" + e.h;
			I.addEdge(r, a);		
			I.addEdge(u, a);
			I.addEdge(v, a);
			I.addEdge(h, a);
			
			// Model
			lbl_M[d = nextLbl_M++] = "d_" +  nE;
			lbl_M[u = nextLbl_M++] = "d^" +  nE + "_" + e.u;
			lbl_M[v = nextLbl_M++] = "d^" +  nE + "_" + e.v;
			lbl_M[h = nextLbl_M++] = "d^" +  nE + "_" + e.h;
			M.addEdge(q, d);		
			M.addEdge(u, d);
			M.addEdge(v, d);
			M.addEdge(h, d);

			nE++;
		}
		
		for(int i = 0; i < H.nU + H.nV + H.nH; i++) {
			int u;
			lbl_M[u = nextLbl_M++] = "" +  i;
			M.addEdge(q, u);
		}

		write3DM_instance(I, M, lbl_I, lbl_M, H, fileName);
	}
	
	void write3DM_instance(Tree I, Tree M, String[] lbl_I, String[] lbl_M, HyperGraph H, String fileName) throws IOException {
		BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
		br.write(I.size + " " + M.size + " " + H.edges.size() + "\n");

		// --- write image ---
		for(int i = 0; i < I.size; i++) {
			br.write(lbl_I[i] + "\n");
		}
		
		for(Edge e : I.edges) {
			br.write(e.u + " " + e.v + "\n");
		}
		// -------------------

		// --- write model ---
		for(int i = 0; i < M.size; i++) {
			br.write(lbl_M[i] + "\n");
		}
		
		for(Edge e : M.edges) {
			br.write(e.u + " " + e.v + "\n");
		}
		// -------------------

		// write hyper-edges' costs 
		for(HyperEdge e : H.edges) {
			br.write((int) e.cost + "\n");
		}
		
		br.close();
	}

	
	void genAndWriteLblTreeFromPruferSeq(int n, int maxCostVert, int maxCostEdge, int id) throws IOException {
		LinkedList<Edge> edges = genLblTreeFromPruferSeq(n);
		String fileName = "lbl_tree_by_prufer_" + n + "_" + maxCostVert + "_" + maxCostEdge + "_" + id + ".txt";
		BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
		br.write(n + "\n");

		for(int i = 0; i < n; i++) {
			br.write((rand.nextInt(maxCostVert)  + 1) + "\n");
		}
		
		for(Edge e : edges) {
			br.write(e.u + " " + e.v + " " + (rand.nextInt(maxCostVert) + 1) + "\n");
		}
		
		br.close();
	}
	
	public static void main(String[] args) throws IOException {
		TreeGenerator tg = new TreeGenerator();
		
		for(int size = 20; size <= 80; size += 20) {
			for(int id = 1; id <= 20; id++) {
				tg.genAndWriteLblTreeFromPruferSeq(size, 50, 50, id);		
			}
		}
		
		int[] s ={13,14,14,11,11,11,9,10,10,8,9,9,8,8,8,7,8,8,7,7,8,7,7,7,6,7,7,15,16,16,13,12,12,10,11,11,9,10,10,9,9,9,8,9,9,8,8,8,7,8,8,7,7,8,17,17,17,13,14,14,12,12,12,10,11,11,10,10,10,9,9,10,9,9,9,8,9,9,8,8,9,19,18,18,14,15,15,12,13,13,12,12,11,10,11,11,10,10,11,10,10,9,9,9,10,9,9,9,19,19,20,16,16,15,14,14,13,12,12,13,11,11,12,10,11,11,10,11,10,9,10,10,9,10,9};

		int dens = 10;
		for(int i = 0; i < s.length; i+=3) {
			int nU = s[i], nV = s[i+1], nH = s[i+2];

			for(int id = 1; id <= 10; id++) {
				String fileName = "GMP_from_3DM_" + nU + "_" + nV + "_" + nH + "_" + dens + "_" + id + ".txt";
				tg.writeImgMdlFrom3DMatching(tg.genRandomHiperGraph(nU, nV, nH, 50, dens), fileName);
			}
			
			dens = dens == 90 ? 10 : dens + 10;
		}
	}
}
