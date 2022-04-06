package solverinterface;


public abstract class LinExpr {
	
	protected double rhs;

	public abstract void addTerm(double value, Var var) throws SolverException;
	
	public abstract Object getLinExpr();
	
	public double getRhs() {
		return rhs;
	}
	
	public void setRhs(double rhs) {
		this.rhs = rhs;
	}
}
