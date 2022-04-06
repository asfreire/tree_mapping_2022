package tmpsolvers;

import java.util.HashMap;
import java.util.LinkedList;

import datastructures.Input;
import datastructures.Tree;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;
import util.Statistics;

public abstract class DP_AbstractSeparation {
	Tree I;
	Tree M;
	int k;
	int i;
	int[] children_k;
	HashMap<String, Double> x_vals;
	HashMap<String, Var> x;
	Solver solver;
	Config config;
	Statistics statistics;
	Input input;

	public DP_AbstractSeparation(int i, int k, Input input, Config config, Statistics statistics, Solver solver,
			HashMap<String, Var> x) {
		this.solver = solver;
		this.input = input;
		this.I = input.I;
		this.M = input.M;
		this.i = i;
		this.k = k;
		this.statistics = statistics;
		this.config = config;
		this.x = x;
	}

	void addCoefInDescendants(LinExpr cut, int j, int l) throws SolverException {
		for (int u : I.neighbours[j]) {
			if (u == I.parent[j])
				continue;

			cut.addTerm(1, x.get(DP.getKey(u, l)));
			addCoefInDescendants(cut, u, l);
		}
	}

	public abstract LinkedList<LinExpr> separate(HashMap<String, Double> x_vals) throws SolverException;
}
