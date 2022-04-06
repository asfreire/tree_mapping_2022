package util;

import solverinterface.SolverConnector.SolverType;


public class Config {
	public boolean TurOffPresolve = false;
	public boolean solve_LP = true;
	public boolean solve_IP = true;
	public double TimeLimit = 60 * 60; // 1 hour
	public boolean turnOffLog = true;
	public boolean singleThread = false;
	public SolverType solverType = SolverType.CPLEX;
	//public SolverType solverType = SolverType.GUROBI;

	// For DynProg
	public boolean addCuts;
	public int nAddedCuts;
	public double violationThreashold = 0.001;

}
