package tmpsolvers;

import java.util.HashMap;

import datastructures.Edge;
import datastructures.Input;
import datastructures.Tree;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.SolverConnector;
import solverinterface.Var;
import util.Config;
import util.Statistics;
import util.Timer;

public class Chopra extends TreeMappingSolver {

	void solve(Input input, boolean LP, Config config, Statistics statistics) throws Exception {
		Timer timer = new Timer();
		timer.start();
		Tree I = input.I;
		Tree M = input.M;
		Solver solver = SolverConnector.getSolverInstance(config);

		// ------------------ creating variables --------------------
		Var[][] x = new Var[I.size][M.size];
		HashMap<String, Var> y = new HashMap<String, Var>();
		HashMap<String, Var> z = new HashMap<String, Var>();

		for (int i = 0; i < I.size; i++) {
			for (int k = 0; k < M.size; k++) {
				if (LP)
					x[i][k] = solver.addContinuosVar(0, 1);
				else
					x[i][k] = solver.addBinVar();
			}
		}

		for (Edge ij : I.edges) {
			for (int k = 0; k < M.size; k++) {
				String ij_to_k = ij.u + "," + ij.v + "," + k;
				if (LP)
					y.put(ij_to_k, solver.addContinuosVar(0, 1));
				else
					y.put(ij_to_k, solver.addBinVar());
			}
		}

		for (Edge ij : I.edges) {
			for (Edge kl : M.edges) {
				String ij_to_kl = ij.u + "," + ij.v + "," + kl.u + "," + kl.v;
				if (LP)
					z.put(ij_to_kl, solver.addContinuosVar(0, 1));
				else
					z.put(ij_to_kl, solver.addBinVar());
			}
		}
		// -------------------------------------------------------

		// --- constraint (1) ---
		for (int i = 0; i < I.size; i++) {
			LinExpr expr = solver.createLinExpr();

			for (int k = 0; k < M.size; k++) {
				expr.addTerm(1, x[i][k]);
			}

			solver.addEqual(expr, 1);
		}
		// ----------------------

		// --- constraint (2) ---
		boolean[] notNeighboursOf_k = new boolean[M.size];

		for (int k = 0; k < M.size; k++) {
			getNotNeighbours(M, notNeighboursOf_k, k);

			for (Edge ij : I.edges) {
				LinExpr expr1 = solver.createLinExpr();
				LinExpr expr2 = solver.createLinExpr();
				expr1.addTerm(1, x[ij.u][k]);
				expr2.addTerm(1, x[ij.v][k]);

				for (int l = 0; l < M.size; l++) {
					if (notNeighboursOf_k[l]) {
						expr1.addTerm(1, x[ij.v][l]);
						expr2.addTerm(1, x[ij.u][l]);
					}
				}

				solver.addLessEqual(expr1, 1);
				solver.addLessEqual(expr2, 1);
			}
		}
		// ---------------------

		// --- constraint (3) ---
		for (int k = 0; k < M.size; k++) {
			LinExpr expr = solver.createLinExpr();

			for (int i = 0; i < I.size; i++) {
				expr.addTerm(1, x[i][k]);
			}

			for (Edge ij : I.edges) {
				String ij_to_k = ij.u + "," + ij.v + "," + k;
				expr.addTerm(-1, y.get(ij_to_k));
			}

			solver.addLessEqual(expr, 1);
		}
		// ---------------------

		// --- constraint (4) ---
		for (Edge ij : I.edges) {
			for (int k = 0; k < M.size; k++) {
				String ij_to_k = ij.u + "," + ij.v + "," + k;

				LinExpr expr = solver.createLinExpr();
				expr.addTerm(-1, x[ij.u][k]);
				expr.addTerm(1, y.get(ij_to_k));
				solver.addLessEqual(expr, 0);

				expr = solver.createLinExpr();
				expr.addTerm(-1, x[ij.v][k]);
				expr.addTerm(1, y.get(ij_to_k));
				solver.addLessEqual(expr, 0);
			}
		}
		// ---------------------

		// --- constraint (5) ---
		for (Edge ij : I.edges) {
			for (Edge kl : M.edges) {
				String ij_to_kl = ij.u + "," + ij.v + "," + kl.u + "," + kl.v;

				LinExpr expr = solver.createLinExpr();
				expr.addTerm(1, z.get(ij_to_kl));
				expr.addTerm(-1, x[ij.u][kl.u]);
				expr.addTerm(-1, x[ij.v][kl.v]);
				solver.addGreaterEqual(expr, -1);

				expr = solver.createLinExpr();
				expr.addTerm(1, z.get(ij_to_kl));
				expr.addTerm(-1, x[ij.v][kl.u]);
				expr.addTerm(-1, x[ij.u][kl.v]);
				solver.addGreaterEqual(expr, -1);
			}
		}
		// ---------------------

		// ------ objective -----------
		LinExpr obj = solver.createLinExpr();

		for (int k = 0; k < M.size; k++) {
			for (int i = 0; i < I.size; i++) {
				obj.addTerm(input.c(i, k), x[i][k]);
			}

			for (Edge ij : I.edges) {
				String ij_to_k = ij.u + "," + ij.v + "," + k;
				obj.addTerm(input.f(ij.u, ij.v, k), y.get(ij_to_k));
			}
		}

		for (Edge ij : I.edges) {
			for (Edge kl : M.edges) {
				String ij_to_kl = ij.u + "," + ij.v + "," + kl.u + "," + kl.v;
				obj.addTerm(input.d(ij.u, ij.v, kl.u, kl.v), z.get(ij_to_kl));
			}
		}

		solver.setMinObj(obj);
		// -----------------------

		solver.setTimeLimit(config.TimeLimit);
		solver.solve();
		timer.pause();
		statistics.bestVal = solver.isOpt() ? solver.getObjVal() : -1;
		statistics.spentTime = solver.isOpt() ? (int) timer.getSpentTimeInSeconds() : -1;
		statistics.numberOfBBN = solver.getNumBBnodes();
		//statistics.gapChopra = solver.getMIPRelativeGap();
	}

	private void getNotNeighbours(Tree M, boolean[] notNeighboursOf_k, int k) {
		for (int l = 0; l < M.size; l++) {
			notNeighboursOf_k[l] = true;
		}

		notNeighboursOf_k[k] = false;

		for (int l : M.neighbours[k]) {
			notNeighboursOf_k[l] = false;
		}
	}
}
