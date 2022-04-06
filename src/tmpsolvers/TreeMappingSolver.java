package tmpsolvers;
import datastructures.Input;
import util.Config;
import util.Statistics;



public abstract class TreeMappingSolver {

	public void solveLP(Input input, Config config, Statistics statistics) throws Exception {
		solve(input, true, config, statistics);
	}

	public void solveIP(Input input, Config config, Statistics statistics) throws Exception {
		solve(input, false, config, statistics);
	}

	abstract void solve(Input input, boolean LP, Config config, Statistics statistics) throws Exception;
}
