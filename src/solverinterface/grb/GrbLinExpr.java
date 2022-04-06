package solverinterface.grb;

import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import solverinterface.LinExpr;
import solverinterface.Var;

public class GrbLinExpr extends LinExpr {
	private GRBLinExpr grbLinExpr;

	public GrbLinExpr(GRBLinExpr linExpr) {
		this.grbLinExpr = linExpr;
	}
	
	@Override
	public void addTerm(double value, Var var) {
		grbLinExpr.addTerm(value, (GRBVar) var.getVar());
	}

	@Override
	public Object getLinExpr() {
		return grbLinExpr;
	}
}
