package tmpsolvers;

import java.util.HashMap;
import java.util.LinkedList;

import datastructures.Input;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UserCutCallback;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;
import util.Statistics;

public class DP_CutCallback_Cplex extends UserCutCallback {
	HashMap<String, Var> x; 
	HashMap<String, Double> x_vals;
	Config config;
	DP_Separation sep;
	Solver solver;
	
	public DP_CutCallback_Cplex(int i, int k, HashMap<String, Var> x, Input input, Statistics statistics, Config config, Solver solver) {
		sep = new DP_Separation(i, k, input, config, statistics, solver, x);
		this.x = x;
		x_vals = new HashMap<String, Double>();
		this.solver = solver;
	}
	
	@Override
	protected void main() throws IloException {
		// skip the separation if not at the end of the cut loop
        if (!isAfterCutLoop())
           return;
        
        // get the current x solution
        x_vals = new HashMap<String, Double>();
        for(String p: x.keySet()) {
        	 x_vals.put(p, getValue((IloNumVar) x.get(p).getVar())) ;
        }

        LinkedList<LinExpr> cuts;
		try {
			cuts = sep.separate(x_vals);
			
			for(LinExpr cut : cuts) {
	        	IloRange cut_ = ((IloCplex) solver.getModel()).range(Double.MIN_VALUE, (IloNumExpr) cut.getLinExpr(), cut.getRhs());
	        	add(cut_);
			}
		} catch (SolverException e) {
			throw new RuntimeException(e);
		}
	}
}
