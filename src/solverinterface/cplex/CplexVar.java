package solverinterface.cplex;

import ilog.concert.IloNumVar;
import solverinterface.Var;

public class CplexVar implements Var {

	private IloNumVar cplexVar;

	public CplexVar(IloNumVar var) {
		this.cplexVar = var;
	}

	@Override
	public Object getVar() {
		return cplexVar;
	}
}
