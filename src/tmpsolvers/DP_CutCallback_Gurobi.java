package tmpsolvers;

import java.util.HashMap;
import java.util.LinkedList;

import datastructures.Input;
import gurobi.GRB;
import gurobi.GRBCallback;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.Var;
import util.Config;
import util.Statistics;

public class DP_CutCallback_Gurobi extends GRBCallback {
	HashMap<String, Var> x; 
	HashMap<String, Double> x_vals;
	Config config;
	DP_Separation sep;
	
	public DP_CutCallback_Gurobi(int i, int k, HashMap<String, Var> x, Input input, Statistics statistics, Config config, Solver solver) {
		sep = new DP_Separation(i, k, input, config, statistics, solver, x);
		this.x = x;
		x_vals = new HashMap<String, Double>();
	}
	
	@Override
	protected void callback() {
		try {
			if(where != GRB.CB_MIPNODE || getIntInfo(GRB.CB_MIPNODE_STATUS) != GRB.OPTIMAL) 
				return;
			
			  // get the current x solution
	        for(String p: x.keySet()) {
	        	 x_vals.put(p, getNodeRel((GRBVar) x.get(p).getVar())) ;
	        }

	        LinkedList<LinExpr> cuts = sep.separate(x_vals);
				
			for(LinExpr cut : cuts) {
	        	addCut((GRBLinExpr)cut.getLinExpr(), GRB.LESS_EQUAL, cut.getRhs());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
