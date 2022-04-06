package solverinterface;



public interface Solver {

	public void setTimeLimit(double timeLimitInSeconds) throws SolverException;

	public void setMinObj(LinExpr obj) throws SolverException;

	public void solve() throws SolverException;

	public double getObjVal() throws SolverException;
	
	public double getVarValue(Var var) throws SolverException;

	public int getNumNonZeros() throws SolverException;

	public int getNumBBnodes() throws SolverException;
	
	public boolean isOpt() throws SolverException;

	public double getMIPRelativeGap() throws SolverException;
	
	public void dispose() throws SolverException;
		
	public Var addBinVar() throws SolverException;
	
	public Var addContinuosVar(double LB, double UB) throws SolverException;
	
	public Var addContinuosVar(Column col, double LB, double UB) throws SolverException;
	
	public Var addIntVar(int LB, int UB) throws SolverException;
	
	public LinExpr createLinExpr() throws SolverException;
	
	public Range createRange(double lhs, double rhs) throws SolverException;
    
	public  void addLessEqual(LinExpr linExpr, double rhs) throws SolverException;
	
	public  void addEqual(LinExpr linExpr, double rhs) throws SolverException;
	
	public  void addGreaterEqual(LinExpr linExpr, double rhs) throws SolverException;
	
	public Object getModel() throws SolverException;

	public void addUserCutCallback(Object dp_CutCallback) throws SolverException;

	public double getDual(Range range) throws SolverException;

	public Column column(Range range, double i) throws SolverException;
	
	public Column column(double objCoef) throws SolverException;
}
