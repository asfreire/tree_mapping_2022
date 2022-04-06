package solverinterface;

public class SolverException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public SolverException(Exception e) {
		super(e);
	}
	
	public SolverException(String msg) {
		super(msg);
	}
}
