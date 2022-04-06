package tmpsolvers;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import datastructures.Input;
import datastructures.Tree;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import solverinterface.LinExpr;
import solverinterface.Solver;
import solverinterface.SolverConnector;
import solverinterface.SolverConnector.SolverType;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;
import util.Statistics;
import util.Timer;


public class DP extends TreeMappingSolver {
	
	HashMap<String, Double> η; 	// memoization dictionary
	Input input;
	Tree I; 
	Tree M;
	Config config;
	Statistics statistics;
	boolean isLP;
	
	@Override
	void solve(Input input, boolean LP, Config config, Statistics statistics) throws Exception {
		isLP = LP;
		Timer timer = new Timer();
		timer.start();
    	this.I = input.I;
    	this.M = input.M;
    	this.config = config;
    	this.statistics = statistics;
    	I.setRoot(0);
    	M.setRoot(0);
    	this.input = input;
        η = new HashMap<String, Double>(); 
        int r = I.root;
        double β = Double.MAX_VALUE;
        
        for (int q = 0; q < M.size; q++) {
            M.changeRoot(q);
            double b = minCostRootedMapping(r, q);
            β = Math.min(b, β);
        }

        timer.pause();
        statistics.bestVal = β;
        statistics.spentTime = (int) timer.getSpentTimeInSeconds();
    }

    double minCostRootedMapping(int i, int k) throws Exception {
        String i_to_k = i + "," + k + "," + M.parent[k];
        
        if(!η.containsKey(i_to_k)) {
            if (I.isleaf(i) || M.isleaf(k)) {
                double cost_I_i_to_k = input.c(i, k);

                for (Integer j : I.V_star(i)) {
                    cost_I_i_to_k += input.c(j, k) + input.f(j, I.parent[j], k);
                }

                η.put(i_to_k, cost_I_i_to_k);
            } else {
                for (Integer j : I.V_star(i)) {
                    for (int l : M.neighbours[k]) {
                    	if(M.parent[l] == k) // is it child? 
                    		minCostRootedMapping(j, l);
                    }
                }

                η.put(i_to_k, P_rtm(i, k));
            }
        }

        return η.get(i_to_k);
    }

    // finds the cost of the i-rooted sub-tree and k-rooted sub-tree mapping
    double P_rtm(int i, int k) throws Exception { 
       	List<Integer> V_i_star = I.V_star(i);
               
		Solver solver = SolverConnector.getSolverInstance(config);

        // ----------- creates x variables -----------
		HashMap<String, Var> x = new HashMap<String, Var>();
		
		if(isLP || config.addCuts) {
	        for (Integer j : V_i_star) {
	        	
	        	if(i == j)
	        		continue;
	        	
	        	x.put(DP.getKey(j, k), solver.addContinuosVar(0, 1));
	        	
	            for (int l : M.neighbours[k]) {
	            	if(M.parent[l] == k) // is it child?
	            		x.put(DP.getKey(j, l), solver.addContinuosVar(0, 1));
	            }
	        }
	
	        x.put(DP.getKey(i, k), solver.addContinuosVar(1, 1)); // rooted-mapping constraint
		} else {
	        for (Integer j : V_i_star) {

	        	if(i == j)
	        		continue;

	        	x.put(DP.getKey(j, k), solver.addBinVar());
	        	
	            for (int l : M.neighbours[k]) {
	            	if(M.parent[l] == k) // is it child?
	            		x.put(DP.getKey(j, l), solver.addBinVar());
	            }
	        }
	
	        x.put(DP.getKey(i, k), solver.addIntVar(1, 1)); // rooted-mapping constraint
		}
        // -------------------------------------------

        // --------- objective ------------
        LinExpr obj = solver.createLinExpr();
        
        for (int j : V_i_star) {
            double γ_j = input.c(j, k) + input.f(j, I.parent[j], k);
            obj.addTerm(γ_j, x.get(DP.getKey(j, k)));
            
            for (int l : M.neighbours[k]) {
            	if(M.parent[l] == k) { // is it child?
	            	String j_to_l = j + "," + l + "," + M.parent[l];
	            	double κ_j_l = η.get(j_to_l) + input.d(j, I.parent[j], l, M.parent[l]);
	            	obj.addTerm(κ_j_l, x.get(DP.getKey(j, l)));
            	}
            }
        }

        solver.setMinObj(obj);
        // ---------------------------------

        // --- first constraint ---
        for (int j : V_i_star) {
            LinExpr expr = solver.createLinExpr();
            expr.addTerm(-1, x.get(DP.getKey(j, k)));

            for (int l : M.neighbours[k]) {
            	if(M.parent[l] == k) // is it child?
            		expr.addTerm(-1, x.get(DP.getKey(j, l)));
            }

            expr.addTerm(1, x.get(DP.getKey(I.parent[j], k)));
            solver.addEqual(expr, 0);
        }
        // ------------------------

        // --- second constraint ---
        for (int l : M.neighbours[k]) {
        	if(M.parent[l] == k) {// is it child?
	        	LinExpr expr = solver.createLinExpr();
	
	            for (int j : V_i_star) {
	            	expr.addTerm(1, x.get(DP.getKey(j, l)));
	            }
	
	            solver.addLessEqual(expr, 1);
        	}
        }
        // ------------------------

        // add callback if needed
        if(!isLP && config.addCuts) {
        	if(config.solverType.equals(SolverType.GUROBI))
        		solver.addUserCutCallback(new DP_CutCallback_Gurobi(i, k, x, input, statistics, config, solver));
        	else
        		solver.addUserCutCallback(new DP_CutCallback_Cplex(i, k, x, input, statistics, config, solver));
        }
        
        solver.solve();
        
        // if it is IP, we apply cutting-plane method before converting the variables
        if(isLP || config.addCuts) {
        	cuttingPlaneMethod(i, k, x, solver);
        }
        
        if(areAllIntegral(x, solver)) {
        	statistics.nSubProblemsDynProgInt++;
        } else if(!isLP) {
        	 for(String key: x.keySet()) {
        		IloCplex cplex = (IloCplex)solver.getModel();
        		cplex.add(cplex.conversion((IloNumVar)x.get(key).getVar(), IloNumVarType.Bool));
        		solver.solve();
        	 }
        }
        
        statistics.numberOfBBN += solver.getNumBBnodes();
        double objVal = solver.getObjVal();
        statistics.nSubProblemsDynProg++;
        solver.dispose();
        return input.c(i, k) + objVal;
    }
    
    void cuttingPlaneMethod(int i, int k, HashMap<String, Var> x, Solver solver) throws SolverException, IloException {
    	if(areAllIntegral(x, solver)) {
    		return;
    	}
    	
    	DP_Separation sep = new DP_Separation(i, k, input, config, statistics, solver, x);
    	HashMap<String, Double> x_vals = new HashMap<String, Double>();
        
        while (true) {
        	x_vals.clear();
        	
            for(String p: x.keySet()) {
           	 	x_vals.put(p, solver.getVarValue(x.get(p)));
            }

            LinkedList<LinExpr> cuts = sep.separate(x_vals);

            if(cuts.size() == 0) {
            	break;
            }
            
            for(LinExpr cut : cuts) {
            	solver.addLessEqual(cut, cut.getRhs());	
            }
        	
        	solver.solve();
        	
        	if(!solver.isOpt()) {
        		throw new RuntimeException("Deu ruim");
        	}
        }         
    }
    
    boolean areAllIntegral(HashMap<String, Var> x, Solver solver) throws SolverException {
    	for(Var v : x.values()) {
    		double val = solver.getVarValue(v);
    		
    		if(val > 0.000001 && val < 0.999999) {
    			return false;
    		}
    	}

    	return true;    	
    }
    
    static String getKey(int j, int l) {
		return j + "," + l;
	}
}
