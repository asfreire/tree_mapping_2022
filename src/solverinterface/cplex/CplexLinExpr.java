package solverinterface.cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import solverinterface.LinExpr;
import solverinterface.SolverException;
import solverinterface.Var;

public class CplexLinExpr extends LinExpr {
	private IloLinearNumExpr cplexLinExpr;

	public CplexLinExpr(IloLinearNumExpr linExpr) {
		this.cplexLinExpr = linExpr;
	}
	
	@Override
	public void addTerm(double value, Var var) throws SolverException {
		try {
			cplexLinExpr.addTerm(value, (IloNumVar) var.getVar());
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Object getLinExpr() {
		return cplexLinExpr;
	}
}
