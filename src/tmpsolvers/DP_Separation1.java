package tmpsolvers;

import java.util.HashMap;
import java.util.LinkedList;

import datastructures.Input;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;
import util.Statistics;

public class DP_Separation1 extends DP_AbstractSeparation {

	double max_lhs;
	int max_j;
	int max_l;
	HashMap<String, Double> mem = new HashMap<>();

	public DP_Separation1(int i, int k, Input input, Config config, Statistics statistics, Solver solver,
			HashMap<String, Var> x) {
		super(i, k, input, config, statistics, solver, x);
	}

	public LinkedList<LinExpr> separate(HashMap<String, Double> x_vals) throws SolverException {
		this.x_vals = x_vals;
		mem.clear();
		max_lhs = max_j = max_l = -1;
		LinkedList<LinExpr> cuts = new LinkedList<LinExpr>();

		for (int j : I.D(i)) {
			double x_jk = x_vals.get(DP.getKey(j, k));
			
			for (int l : M.neighbours[k]) {
				if (l == M.parent[k])
					continue;
			
				double val = calcSum(j, l) - x_jk - x_vals.get(DP.getKey(j, l));

				if (val > max_lhs) {
					max_lhs = val;
					max_j = j;
					max_l = l;
				}
			}
		}

		if (max_lhs > config.violationThreashold) {
			LinExpr cut = solver.createLinExpr();
			statistics.nCuts++;
			addCoefs(cut);
			cuts.add(cut);
		}

		return cuts;
	}

	double calcSum(int u, int l) throws SolverException {
		if (!mem.containsKey(DP.getKey(u, l))) {
			double val = x_vals.get(DP.getKey(u, l));

			for (int v : I.neighbours[u]) {
				if (v != I.parent[u]) {
					val += calcSum(v, l);
				}
			}

			mem.put(DP.getKey(u, l), val);
		}

		return mem.get(DP.getKey(u, l));
	}

	void addCoefs(LinExpr cut) throws SolverException {
		int j = max_j;
		int l = max_l;
		cut.setRhs(0.0);
		cut.addTerm(-1, x.get(DP.getKey(j, k)));
		//cut.addTerm(1, x.get(DP.getKey(j, l)));
		addCoefInDescendants(cut, j, l);
	}
}
