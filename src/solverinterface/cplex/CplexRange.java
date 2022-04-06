package solverinterface.cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import solverinterface.Range;
import solverinterface.SolverException;
import solverinterface.Var;

public class CplexRange extends Range {
	public IloRange range;

	public CplexRange(IloRange range) {
		super();
		this.range = range;
	}

	@Override
	public void addTerm(double value, Var var) throws SolverException {
		try {
			((IloLinearNumExpr) range.getExpr()).addTerm(value, (IloNumVar) var.getVar());
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Object getLinExpr()  throws SolverException {
		try {
			return range.getExpr();
		} catch (IloException e) {
			throw new SolverException(e);
		}
	}

	@Override
	public Object getRange() {
		return range;
	}
}
