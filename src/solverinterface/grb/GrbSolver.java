package solverinterface.grb;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntAttr;
import gurobi.GRB.IntParam;
import gurobi.GRBCallback;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBExpr;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import solverinterface.Column;
import solverinterface.LinExpr;
import solverinterface.Range;
import solverinterface.Solver;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;

public class GrbSolver implements Solver {

	GRBModel grbModel;

	public GrbSolver(Config config) throws SolverException {
		try {
			GRBEnv env = new GRBEnv();
			grbModel = new GRBModel(env);
			
			if(config.singleThread)
				grbModel.set(IntParam.Threads, 1);
			
			if(config.turnOffLog)
				grbModel.set(IntParam.OutputFlag, 0);
			
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void setTimeLimit(double timeLimitInSeconds) throws SolverException {
		try {
			grbModel.set(DoubleParam.TimeLimit, timeLimitInSeconds);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void setMinObj(LinExpr obj) throws SolverException {
		try {
			grbModel.setObjective((GRBExpr) obj.getLinExpr(), GRB.MINIMIZE);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void solve() throws SolverException {
		try {
			grbModel.optimize();
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public double getObjVal() throws SolverException {
		try {
			return grbModel.get(DoubleAttr.ObjVal);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public int getNumNonZeros() throws SolverException {
		try {
			return grbModel.get(IntAttr.NumNZs);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public int getNumBBnodes() throws SolverException {
		try {
			return (int) grbModel.get(GRB.DoubleAttr.NodeCount);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public boolean isOpt() throws SolverException {
		try {
			return grbModel.get(IntAttr.Status) == GRB.Status.OPTIMAL;
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public double getMIPRelativeGap() throws SolverException {
		try {
			if(isOpt()) {
				return 0;
			}
			
			return grbModel.get(GRB.DoubleAttr.MIPGap);
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public void dispose() {
		grbModel.dispose();
	}
	
	@Override
	public Var addBinVar() throws SolverException {
		try {
			return new GrbVar(grbModel.addVar(0, 1, 0, GRB.BINARY, null));
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public Var addContinuosVar(double LB, double UB) throws SolverException {
		try {
			return new GrbVar(grbModel.addVar(LB, UB, 0, GRB.CONTINUOUS, null));
		} catch (GRBException e) {
			throw new SolverException(e);
		}	
	}
	
	@Override
	public Var addIntVar(int LB, int UB)
			throws SolverException {
		try {
			return new GrbVar(grbModel.addVar(LB, UB, 0, GRB.INTEGER, null));
		} catch (GRBException e) {
			throw new SolverException(e);
		}	
	}
	
	@Override
	public LinExpr createLinExpr() {
    	return new GrbLinExpr(new GRBLinExpr());
    }
    
	@Override
	public  void addLessEqual(LinExpr linExpr, double rhs) throws SolverException {
		try {
			grbModel.addConstr((GRBLinExpr) linExpr.getLinExpr(), GRB.LESS_EQUAL, rhs, null);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public  void addEqual(LinExpr linExpr, double rhs) throws SolverException {
		try {
			grbModel.addConstr((GRBLinExpr) linExpr.getLinExpr(), GRB.EQUAL, rhs, null);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public  void addGreaterEqual(LinExpr linExpr, double rhs) throws SolverException {
		try {
			grbModel.addConstr((GRBLinExpr) linExpr.getLinExpr(), GRB.GREATER_EQUAL, rhs, null);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public double getVarValue(Var var) throws SolverException {
		try {
			return ((GRBVar)var.getVar()).get(DoubleAttr.X);
		} catch (GRBException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Object getModel() {
		return grbModel;
	}

	@Override
	public void addUserCutCallback(Object dp_CutCallback)
			throws SolverException {
		grbModel.setCallback((GRBCallback) dp_CutCallback);
		
	}

	@Override
	public Var addContinuosVar(Column col, double LB, double UB) throws SolverException {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Range createRange(double lhs, double rhs) throws SolverException {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public double getDual(Range range) throws SolverException {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Column column(Range range, double i) throws SolverException {
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Column column(double objCoef) throws SolverException {
		throw new RuntimeException("Not implemented yet");
	}
}
