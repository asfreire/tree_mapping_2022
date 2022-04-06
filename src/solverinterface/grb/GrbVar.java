package solverinterface.grb;

import gurobi.GRBVar;
import solverinterface.Var;

public class GrbVar implements Var {

	private GRBVar grbVar;

	public GrbVar(GRBVar var) {
		this.grbVar = var;
	}

	@Override
	public Object getVar() {
		return grbVar;
	}
}
