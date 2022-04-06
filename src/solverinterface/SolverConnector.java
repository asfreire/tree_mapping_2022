package solverinterface;

import solverinterface.cplex.CplexSolver;
import solverinterface.grb.GrbSolver;
import util.Config;

public class SolverConnector {

	public enum SolverType {
		CPLEX, GUROBI;
	}

	public static Solver getSolverInstance(Config config) throws SolverException {
		if(config.solverType.equals(SolverType.GUROBI)) {
			return new GrbSolver(config);
		}
		
		if(config.solverType.equals(SolverType.CPLEX)) {
			return new CplexSolver(config);
		}

		throw new SolverException("Must define a solver type.");
	}
}
