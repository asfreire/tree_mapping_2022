package solverinterface.cplex;

import ilog.concert.IloColumn;
import solverinterface.Column;

public class CplexColumn extends Column{
	@Override
	public void and(Column column) {
		IloColumn col = (IloColumn) super.col;
		super.col = col.and((IloColumn) column.col);
	}
}
