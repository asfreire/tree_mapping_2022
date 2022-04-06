package tmpsolvers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import datastructures.Edge;
import datastructures.Input;
import datastructures.Tree;
import solverinterface.Column;
import solverinterface.LinExpr;
import solverinterface.Range;
import solverinterface.Solver;
import solverinterface.SolverConnector;
import solverinterface.SolverException;
import solverinterface.Var;
import util.Config;
import util.Statistics;
import util.Timer;

public class ColGen extends TreeMappingSolver {
	Input input;
	Config config;
	Statistics statistics;
	Range[] constr1;
	Range[] constr2;
	HashMap<String, Range> constr3;
	HashMap<String, Range> constr4a;
	HashMap<String, Range> constr4b;
	double[] h_dualConstr1;
	double[] r_dualConstr2;
	HashMap<String, Double> t_dualConstr3;
	HashMap<String, Double> w_dualConstr4a;
	HashMap<String, Double> w_prime_dualConstr4b;
	HashMap<String, Var> y_ij_kl;
	LinExpr obj;
	Solver solver;
	public LinkedList<Set<Integer>> addedCols;
	Tree I;
	Tree M;
	HashMap<String, Edge> edgeMap_I;
	HashMap<String, Edge> edgeMap_M;
	double[] mem;
	Timer timer;

	// aux_retrieve[j] = i iff j is a child of i in the solution
	// i.e., i is the parent of j in the solution
	int[] aux_retrieve;

	void init() {
		constr1 = new Range[I.size];
		constr2 = new Range[M.size];
		h_dualConstr1 = new double[I.size];
		r_dualConstr2 = new double[M.size];
		mem = new double[I.size];
		aux_retrieve = new int[I.size];
		constr3 = new HashMap<>();
		constr4a = new HashMap<>();
		constr4b = new HashMap<>();
		t_dualConstr3 = new HashMap<>();
		w_dualConstr4a = new HashMap<>();
		w_prime_dualConstr4b = new HashMap<>();
		addedCols = new LinkedList<>();
		edgeMap_I = new HashMap<>();
		edgeMap_M = new HashMap<>();
		y_ij_kl = new HashMap<>();
	}

	@Override
	void solve(Input input, boolean LP, Config config, Statistics statistics) throws Exception {
		timer = new Timer();
		timer.start();
		this.input = input;
		this.config = config;
		this.statistics = statistics;
		this.I = input.I;
		this.M = input.M;
		I.setRoot(0);
		M.calcNonneibours();
		init();
		config.TurOffPresolve = true;
		config.turnOffLog = false;
		solver = SolverConnector.getSolverInstance(config);

		for (Edge e : I.edges) {
			edgeMap_I.put(e.getKey(), e);
		}

		for (Edge e : M.edges) {
			edgeMap_M.put(e.getKey(), e);
		}

		buildConstraintsAndObj();
		addInitialColumns();

		if(LP) {
			try {
				solveColumnGeneration();
			} catch(Exception e) {
				statistics.bestVal = -1;
				statistics.spentTime = -1;
				System.out.println(e.getMessage());
			}
		} else {
			throw new RuntimeException("Deu ruim");
		}
	}

	void addInitialColumns() throws SolverException {
		Set<Integer> S = new HashSet<>();

		for (int i = 0; i < I.size; i++) {
			S.add(i);
		}

		for (int k = 0; k < M.size; k++) {
			addColumnToMasterLP(S, k);
		}
	}

	Edge getEdge_I(int i, int j) {
		return edgeMap_I.get(new Edge(i, j).getKey());
	}

	Edge getEdge_M(int i, int j) {
		return edgeMap_M.get(new Edge(i, j).getKey());
	}

	String getKey(int i, int j, int k) {
		return i + "," + j + "," + k;
	}

	String getKey(Edge ij, Edge kl) {
		return ij.getKey() + "," + kl.getKey();
	}

	// LP is in standard form (min cx : Ax >= b)
	void buildConstraintsAndObj() throws SolverException {
		obj = solver.createLinExpr();
		solver.setMinObj(obj);

		// constraint 1 (4.16)
		for (int i = 0; i < I.size; i++) {
			constr1[i] = solver.createRange(1, 1);
		}

		// constraint 2 (4.17)
		for (int k = 0; k < M.size; k++) {
			constr2[k] = solver.createRange(-1, Double.POSITIVE_INFINITY);
		}

		// constraint 3 (4.18)
		for (int i = 0; i < I.size; i++) {
			for (int j : I.neighbours[i]) {
				for (int k = 0; k < M.size; k++) {
					constr3.put(getKey(i, j, k), solver.createRange(-1, Double.POSITIVE_INFINITY));
				}
			}
		}

		// constraints 4a and 4b (4.19a)-(4.19b)
		for (Edge ij : I.edges) {
			for (Edge kl : M.edges) {
				Column y_ij_kl_col = solver.column(input.d(ij.u, ij.v, kl.u, kl.v));
				constr4a.put(getKey(ij, kl), solver.createRange(-1, Double.POSITIVE_INFINITY));
				constr4b.put(getKey(ij, kl), solver.createRange(-1, Double.POSITIVE_INFINITY));
				y_ij_kl_col.and(solver.column(constr4a.get(getKey(ij, kl)), 1));
				y_ij_kl_col.and(solver.column(constr4b.get(getKey(ij, kl)), 1));
				Var var = solver.addContinuosVar(y_ij_kl_col, 0, 1);
				y_ij_kl.put(getKey(ij, kl), var);
			}
		}
	}

	void solveColumnGeneration() throws Exception {
		int nItWithNoViolatedColFound = 0;
		int k = 0;
		callLPSolver();

		while (nItWithNoViolatedColFound < M.size) {
			
			if(isTimeLimitExceeded()) {
				statistics.bestVal =  -1;
				statistics.spentTime = -1;
				return;
			}
			
			Set<Integer> S = solvePricing(k);

			if (S != null) {
				addColumnToMasterLP(S, k);
				callLPSolver();
				nItWithNoViolatedColFound = 0;
			} else {
				nItWithNoViolatedColFound++;
			}

			k = (k + 1) % M.size;
		}

		timer.pause();
		statistics.bestVal = solver.isOpt() ? solver.getObjVal() : -1;
		statistics.spentTime = (int) timer.getSpentTimeInSeconds();
	}

	Set<Integer> solvePricing(int k) {
		calcDualVarsAggregatedPerEdgeAndVertex(k);
		resetMem();
		double opt = Double.POSITIVE_INFINITY;
		int best_i = -1;

		for (int i = 0; i < I.size; i++) {
			double opt_i = MRP_SUB(i, k);

			if (I.root != i) {
				if(i < I.parent[i]) {
					opt_i += getEdge_I(i, I.parent[i]).rho_plus_u_leq_v;
				} else {
					opt_i += getEdge_I(i, I.parent[i]).rho_plus_u_geq_v;
				}
			}

			if (opt_i < opt) {
				best_i = i;
				opt = opt_i;
			}
		}

		opt += r_dualConstr2[k];

		if (opt < -0.000001) {
			Set<Integer> S = retriveOpt(best_i, new HashSet<Integer>());
			return S;
		} else {
			return null;
		}
	}

	private Set<Integer> retriveOpt(int i, Set<Integer> set) {
		set.add(i);

		for (int j : I.neighbours[i]) {
			if (j == I.parent[i])
				continue;

			if (aux_retrieve[j] == i) {
				retriveOpt(j, set);
			}
		}

		return set;
	}

	void resetMem() {
		for (int i = 0; i < mem.length; i++) {
			mem[i] = Double.MAX_VALUE;
			aux_retrieve[i] = -1;
		}
	}

	double MRP_SUB(int i, int k) {
		if (I.isleaf(i)) {
			return I.xi[i];
		} else {
			if (mem[i] == Double.MAX_VALUE) {
				mem[i] = I.xi[i];

				for (int j : I.neighbours[i]) {
					if (j == I.parent[i])
						continue;

					double rho_plus = i < j ? getEdge_I(i, j).rho_plus_u_leq_v : getEdge_I(i, j).rho_plus_u_geq_v;
						
					if (rho_plus < MRP_SUB(j, k) + getEdge_I(i, j).rho) {
						mem[i] += rho_plus;
					} else {
						mem[i] += MRP_SUB(j, k) + getEdge_I(i, j).rho;
						aux_retrieve[j] = i;
					}
				}
			}

			return mem[i];
		}
	}

	void calcDualVarsAggregatedPerEdgeAndVertex(int k) {
		// xi
		for (int i = 0; i < I.size; i++) {
			I.xi[i] = input.c(i, k) - h_dualConstr1[i];
		}

		// rho
		for (Edge ij : I.edges) {
			ij.rho = input.f(ij.u, ij.v, k);

			for (int l : M.neighbours[k]) {
				Edge kl = getEdge_M(k, l);
				ij.rho += w_dualConstr4a.get(getKey(ij, kl));
				ij.rho += w_prime_dualConstr4b.get(getKey(ij, kl));
			}

			for (int l : M.nonneighbours[k]) {
				ij.rho += t_dualConstr3.get(getKey(ij.u, ij.v, l));
				ij.rho += t_dualConstr3.get(getKey(ij.v, ij.u, l));
			}

			ij.rho += t_dualConstr3.get(getKey(ij.u, ij.v, k));
			ij.rho += t_dualConstr3.get(getKey(ij.v, ij.u, k));
		}

		// rho+
		for (Edge ij : I.edges) {
			ij.rho_plus_u_leq_v = t_dualConstr3.get(getKey(ij.u, ij.v, k));
			ij.rho_plus_u_geq_v = t_dualConstr3.get(getKey(ij.v, ij.u, k));
			
			for (int l : M.nonneighbours[k]) {
				ij.rho_plus_u_leq_v += t_dualConstr3.get(getKey(ij.v, ij.u, l));
				ij.rho_plus_u_geq_v += t_dualConstr3.get(getKey(ij.u, ij.v, l));
			}

			for (int l : M.neighbours[k]) {
				Edge kl = getEdge_M(k, l);

				if(l > k) {
					ij.rho_plus_u_leq_v += w_dualConstr4a.get(getKey(ij, kl));
					ij.rho_plus_u_geq_v += w_prime_dualConstr4b.get(getKey(ij, kl));
				} else {
					ij.rho_plus_u_leq_v += w_prime_dualConstr4b.get(getKey(ij, kl));
					ij.rho_plus_u_geq_v += w_dualConstr4a.get(getKey(ij, kl));
				}
			}
		}
	}

	void addColumnToMasterLP(Set<Integer> S, int k) throws SolverException {
		double g_S_k = 0;
		
		for (int i : S) {
			g_S_k += input.c(i, k);
		}

		for (Edge ij : I.edges) {
			if (S.contains(ij.u) && S.contains(ij.v)) {
				g_S_k += input.f(ij.u, ij.v, k);
			}
		}
		
		// obj
		Column col = solver.column(g_S_k);

		// constraint 1 (4.16)
		for (int i : S) {
			col.and(solver.column(constr1[i], 1));
		}

		// constraint 2 (4.17)
		col.and(solver.column(constr2[k], -1));

		// constraint 3 (4.18)
		for (int j : S) {
			for (int i : I.neighbours[j]) {
				if (!S.contains(i)) {
					for (int l : M.nonneighbours[k]) {
						col.and(solver.column(constr3.get(getKey(i, j, l)), -1));
					}

					col.and(solver.column(constr3.get(getKey(j, i, k)), -1));
				} else {
					for (int l : M.nonneighbours[k]) {
						col.and(solver.column(constr3.get(getKey(j, i, l)), -1));
					}

					col.and(solver.column(constr3.get(getKey(j, i, k)), -1));
				}
			}
		}

		// constraint 4 (4.19a)-(4.19b)
		for (Edge ij : I.edges) {
			if (!S.contains(ij.u) && !S.contains(ij.v))
				continue;

			for (Edge kl : M.edges) {
				if (kl.u != k && kl.v != k)
					continue;

				if (S.contains(ij.u)) {
					if (kl.u == k)
						col.and(solver.column(constr4a.get(getKey(ij, kl)), -1));

					if (kl.v == k)
						col.and(solver.column(constr4b.get(getKey(ij, kl)), -1));
				}

				if (S.contains(ij.v)) {
					if (kl.v == k)
						col.and(solver.column(constr4a.get(getKey(ij, kl)), -1));

					if (kl.u == k)
						col.and(solver.column(constr4b.get(getKey(ij, kl)), -1));
				}
			}
		}

		solver.addContinuosVar(col, 0, 1);
		addedCols.add(S);
	}

	double getObjVal() throws Exception {
		return solver.getObjVal();
	}

	int getRoundedObjVal() throws Exception {
		return (int) (getObjVal() + 0.000001);
	}

	void callLPSolver() throws Exception {
		solver.solve();

		if (!solver.isOpt()) {
			throw new Exception("Deu ruim");
		}

		for (int i = 0; i < I.size; i++) {
			h_dualConstr1[i] = solver.getDual(constr1[i]);
		}

		for (int k = 0; k < M.size; k++) {
			r_dualConstr2[k] = solver.getDual(constr2[k]);
		}

		for (int i = 0; i < I.size; i++) {
			for (int j : I.neighbours[i]) {
				for (int k = 0; k < M.size; k++) {
					t_dualConstr3.put(getKey(i, j, k), solver.getDual(constr3.get(getKey(i, j, k))));
				}
			}
		}

		for (Edge ij : I.edges) {
			for (Edge kl : M.edges) {
				w_dualConstr4a.put(getKey(ij, kl), solver.getDual(constr4a.get(getKey(ij, kl))));
				w_prime_dualConstr4b.put(getKey(ij, kl), solver.getDual(constr4b.get(getKey(ij, kl))));
			}
		}		
	}
	
	boolean isTimeLimitExceeded() throws Exception {
		timer.pause();
		long spentTime = timer.getSpentTimeInSeconds();
		timer.restart();
		
		if (spentTime > config.TimeLimit) {
			return true;
		}
		
		return false;
	}
}
