package solverinterface;

public abstract class Range {
	
	protected double rhs;
	protected double lhs;
	
	public abstract void addTerm(double value, Var var) throws SolverException;
	
	public abstract Object getLinExpr() throws SolverException;
	
	public double getRhs() {
		return rhs;
	}
	
	public void setRhs(double rhs) {
		this.rhs = rhs;
	}
	
	public double getLhs() {
		return lhs;
	}
	
	public void setLhs(double lhs) {
		this.lhs = lhs;
	}

	public abstract Object getRange();

}
