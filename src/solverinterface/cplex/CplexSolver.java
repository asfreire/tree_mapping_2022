package solverinterface.cplex;
import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.UserCutCallback;
import solverinterface.Column;
import solverinterface.LinExpr;
import solverinterface.Range;
import solverinterface.Solver;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;

public class CplexSolver implements Solver {

	private IloCplex cplex;

	public CplexSolver(Config config) throws SolverException {
		try {
			cplex = new IloCplex();
			
			if(config.singleThread)
				cplex.setParam(IntParam.Threads, 1);
			
			if(config.turnOffLog) {
				cplex.setOut(null);
				//cplex.setWarning(null);
			}
			
			if(config.TurOffPresolve)
				cplex.setParam(IloCplex.Param.Preprocessing.Presolve, false);
			
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void setTimeLimit(double timeLimitInSeconds) throws SolverException {
		try {
			cplex.setParam(IntParam.TimeLimit, timeLimitInSeconds);
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void setMinObj(LinExpr obj) throws SolverException {
		try {
			cplex.addMinimize((IloLinearNumExpr) obj.getLinExpr());
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public void solve() throws SolverException {
		try {
			cplex.solve();
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public double getObjVal() throws SolverException {
		try {
			return cplex.getObjValue();
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public int getNumNonZeros() throws SolverException {
		try {
			return cplex.getNNZs();
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public int getNumBBnodes() throws SolverException {
		try {
			return cplex.getNnodes();
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public boolean isOpt() throws SolverException {
		try {
			return cplex.getStatus() == IloCplex.Status.Optimal;
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public double getMIPRelativeGap() throws SolverException {
		try {
			if(isOpt()) {
				return 0;
			}
			
			return cplex.getMIPRelativeGap();
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public void dispose() throws SolverException {
		cplex.end();
	}
	
	@Override
	public LinExpr createLinExpr() throws SolverException {
    	try {
			return new CplexLinExpr(cplex.linearNumExpr());
		} catch (IloException e) {
			throw new SolverException(e);
		}
    }
    
	@Override
	public  void addLessEqual(LinExpr linExpr, double rhs) throws SolverException {
		try {
			cplex.addLe((IloNumExpr) linExpr.getLinExpr(), rhs);
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public  void addEqual(LinExpr linExpr, double rhs) throws SolverException {
		try {
			cplex.addEq((IloNumExpr) linExpr.getLinExpr(), rhs);
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}
	
	@Override
	public  void addGreaterEqual(LinExpr linExpr, double rhs) throws SolverException {
		try {
			cplex.addGe((IloNumExpr) linExpr.getLinExpr(), rhs);
		} catch (Exception e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Var addBinVar() throws SolverException {
		try {
			return new CplexVar(cplex.boolVar());
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Var addContinuosVar(double LB, double UB) throws SolverException {
		try {
			return new CplexVar(cplex.numVar(LB, UB));
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Var addIntVar(int LB, int UB) throws SolverException {
		try {
			return new CplexVar(cplex.intVar(LB, UB));
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public double getVarValue(Var var) throws SolverException {
		try {
			return cplex.getValue((IloNumVar) var.getVar());
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Object getModel() {
		return cplex;
	}

	@Override
	public void addUserCutCallback(Object dp_CutCallback) throws SolverException {
		try {
			cplex.use((UserCutCallback) dp_CutCallback);
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Var addContinuosVar(Column col, double LB, double UB) throws SolverException {
		try {
			return new CplexVar(cplex.numVar((IloColumn)col.col, LB, UB));
		} catch (IloException e) {
			throw new SolverException(e);
		}	
	}

	@Override
	public Range createRange(double lhs, double rhs) throws SolverException {
		try {
			return new CplexRange(cplex.addRange(lhs, rhs));
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public double getDual(Range range) throws SolverException {
		try {
			return cplex.getDual(((CplexRange) range).range);
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Column column(Range obj, double objCoef) throws SolverException {
		try {
			CplexColumn c = new CplexColumn();
			c.col = cplex.column(((CplexRange)obj).range, objCoef);
			return c;
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	public Column column(double coefObj) throws SolverException {
		try {
			CplexColumn c = new CplexColumn();
			c.col = cplex.column(cplex.getObjective(), coefObj);
			return c;
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}
}
