package tmpsolvers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import datastructures.Edge;
import datastructures.Input;
import datastructures.Pair;
import datastructures.Tree;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.SolverConnector;
import solverinterface.Var;
import util.Config;
import util.Statistics;
import util.Timer;

public class Mestrado extends TreeMappingSolver {

	LinkedList<Integer> getS_I(Tree I, int u, int v) {
		LinkedList<Integer> S_u = new LinkedList<Integer>(I.neighbours[u]);
		S_u.remove((Integer)v);
		return S_u;
	}

	LinkedList<Integer> getS_M(Tree M, int u, int v) {
		LinkedList<Integer> S_u = new LinkedList<Integer>(M.neighbours[u]);

		if (u != v) {
			S_u.remove((Integer)v);
		}

		S_u.add(u);
		return S_u;
	}

	@Override
	void solve(Input input, boolean LP, Config config, Statistics statistics) throws Exception {
		Timer timer = new Timer();
		timer.start();
		Tree I = input.I;
		Tree M = input.M;
		Solver solver = SolverConnector.getSolverInstance(config);
		List<Pair> P_M = new LinkedList<Pair>();

		for (Edge e : M.edges) {
			P_M.add(new Pair(e.u, e.v));
			P_M.add(new Pair(e.v, e.u));
		}

		for (int k = 0; k < M.size; k++) {
			P_M.add(new Pair(k, k));
		}

		// ------------------ creating variables --------------------

		HashMap<String, Var> y = new HashMap<String, Var>();

		for (Edge ij : I.edges) {
			for (Pair kl : P_M) {
				String ij_to_kl = ij.u + "," + ij.v + "," + kl.i + "," + kl.k;

				if (LP) {
					y.put(ij_to_kl, solver.addContinuosVar(0, 1));
				} else {
					y.put(ij_to_kl, solver.addBinVar());
				}
			}
		}
		// -------------------------------------------------------

		// --- constraint (1) ---
		for (Edge ij : I.edges) {
			LinExpr expr = solver.createLinExpr();

			for (Pair kl : P_M) {
				String ij_to_kl = ij.u + "," + ij.v + "," + kl.i + "," + kl.k;
				expr.addTerm(1, y.get(ij_to_kl));
			}

			solver.addEqual(expr, 1);
		}
		// ----------------------

		// --- constraint (2) ---
		for (Edge ij : I.edges) {
			int i = ij.u, j = ij.v;
			List<Integer> S_i = getS_I(I, i, j);
			List<Integer> S_j = getS_I(I, j, i);

			for (Pair kl : P_M) {
				int k = kl.i, l = kl.k;

				List<Integer> S_k = getS_M(M, k, l);
				LinExpr expr1 = solver.createLinExpr();
				String ij_to_kl = i + "," + j + "," + k + "," + l;

				for (int i_ : S_i) {
					for (int k_ : S_k) {
						String i_i_to_k_k = i_ + "," + i + "," + k_ + "," + k;

						if (y.get(i_i_to_k_k) == null) {
							i_i_to_k_k = i + "," + i_ + "," + k + "," + k_;
						}

						expr1.addTerm(1, y.get(i_i_to_k_k));
					}
				}

				expr1.addTerm(-I.neighbours[i].size() + 1, y.get(ij_to_kl));
				solver.addGreaterEqual(expr1, 0);

				LinExpr expr2 = solver.createLinExpr();
				List<Integer> S_l = getS_M(M, l, k);

				for (int j_ : S_j) {
					for (int l_ : S_l) {
						String j_j_to_l_l = j_ + "," + j + "," + l_ + "," + l;

						if (y.get(j_j_to_l_l) == null) {
							j_j_to_l_l = j + "," + j_ + "," + l + "," + l_;
						}

						expr2.addTerm(1, y.get(j_j_to_l_l));
					}
				}

				expr2.addTerm(-I.neighbours[j].size() + 1, y.get(ij_to_kl));
				solver.addGreaterEqual(expr2, 0);
			}
		}
		// ----------------------

		// --- constraint (3) ---
		for (Edge kl : M.edges) {
			LinExpr expr = solver.createLinExpr();

			for (Edge ij : I.edges) {
				String ij_to_kl = ij.u + "," + ij.v + "," + kl.u + "," + kl.v;
				String ij_to_lk = ij.u + "," + ij.v + "," + kl.v + "," + kl.u;
				expr.addTerm(1, y.get(ij_to_kl));
				expr.addTerm(1, y.get(ij_to_lk));
			}

			solver.addLessEqual(expr, 1);
		}
		// ----------------------

		// ------ objective -----------
		LinExpr obj = solver.createLinExpr();

		for (Edge ij : I.edges) {
			for (Pair kl : P_M) {
				int i = ij.u, j = ij.v, k = kl.i, l = kl.k;
				String ij_to_kl = i + "," + j + "," + k + "," + l;
				double H_ij_to_kl;

				if (k == l) {
					H_ij_to_kl = input.c(i, k) / I.neighbours[i].size() + input.c(j, l) / I.neighbours[j].size()
							+ input.f(i, j, k);
				} else {
					H_ij_to_kl = input.c(i, k) / I.neighbours[i].size() + input.c(j, l) / I.neighbours[j].size()
							+ input.d(i, j, k, l);
				}

				obj.addTerm(H_ij_to_kl, y.get(ij_to_kl));
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
		// statistics.gapFreire = solver.getMIPRelativeGap();
	}
}
