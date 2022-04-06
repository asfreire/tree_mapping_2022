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

public class DP_Separation {

	DP_Separation1 sep1;
	Config config;
	
	public DP_Separation(int i, int k, Input input, Config config,
			Statistics statistics, Solver solver, HashMap<String, Var> x) {
		this.config = config;
		
		if(config.addCuts) {
			sep1 = new DP_Separation1(i, k, input, config, statistics, solver, x);
		}
	}
	
	public LinkedList<LinExpr> separate(HashMap<String, Double> x_vals) throws SolverException {
		LinkedList<LinExpr> cuts = new LinkedList<LinExpr>();
		
		if(config.addCuts) {
			cuts.addAll(sep1.separate(x_vals));
		}

		return cuts;
	}
}
