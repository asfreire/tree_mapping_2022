package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import datastructures.Input;
import tmpsolvers.Chopra;
import tmpsolvers.ColGen;
import tmpsolvers.DP;
import tmpsolvers.Mestrado;
import tmpsolvers.TreeMappingSolver;
import util.Config;
import util.Statistics;

public class Main {

	static void printAll(int[] v) {
		System.out.print("[");
		for (int i = 0; i < v.length; i++) {
			System.out.print(v[i] + ",");
		}
		System.out.println("]");
	}

	// --- COMANDOS PARA COPIAR CODIGOS NO SERVIDOR, COMPILAR E RODAR ---
	// no meu notebook (na pasta do projeto):
	// ./manda_codigos_pro_ime.sh
	//
	// no ime.usp.br (na pasta TEMP):
	// ./manda_codigos_pra_hulk.sh
	//
	// no hulk: (na pasta 2022_treemapping/src):
	// --> unzip codigos.zip
	// --> cd..
	// --> ./exec.sh

	// ARGS:
	// dataset={random,bio_id}
	// method={chopra,mestrado,colgen,dynprog, dynprogcut}
	// timelimit={<int>}
	// whattosolve={lp,ip}

	// --- PARA VER OS RESULTADOS
	// no ime.usp.br (na pasta TEMP):
	// --> scp chegado@hulk:TEMP/bin/*.txt .
	//
	// no meu notebook (na basta que me der na telha)
	// --> scp afreire@ime.usp.br:TEMP/*.txt .

	enum DataSet {
		Random, BIO_ID;
	}

	enum Method {
		Chopra, Mestrado, ColGen, DynProg, DynProgCut;
	}

	enum WhatToSolve {
		LP(1), IP(2);

		int val;

		WhatToSolve(int val) {
			this.val = val;
		}
	}

	static class ARGS {
		Method method;
		DataSet dataset;
		boolean cut;
		WhatToSolve whatToSolve;
		int timeLimit;
	}

	static List<String[]> getInstances(DataSet dataset) {
		String rootDir = "../instances";

		if (dataset.equals(DataSet.Random))
			return getLblTreeByPrufferInstances(rootDir);

		if (dataset.equals(DataSet.BIO_ID))
			return getBioIDInstances(rootDir);

		throw new RuntimeException("Deu ruim!");
	}

	static Statistics solve(Method method, Config config, String I, String M) throws Exception {
		Input input = new Input();
		Statistics statistics = new Statistics(input, config);
		input.read(I, M);
		TreeMappingSolver tmpSolver = null;

		switch (method) {
		case Chopra:
			tmpSolver = new Chopra();
			break;

		case Mestrado:
			tmpSolver = new Mestrado();
			break;

		case ColGen:
			tmpSolver = new ColGen();
			break;

		case DynProg:
			tmpSolver = new DP();
			break;
			
		case DynProgCut:	
			tmpSolver = new DP();
			config.addCuts = true;
			break;

		default:
			throw new RuntimeException("Deu ruim!");
		}

		if (config.solve_LP) {
			tmpSolver.solveLP(input, config, statistics);
		} else if (config.solve_IP) {
			tmpSolver.solveIP(input, config, statistics);
		} else {
			throw new RuntimeException("Deu ruim");
		}

		return statistics;
	}

	static ARGS readArgs(String[] args) {
		ARGS a = new ARGS();

		for (int i = 0; i < args.length; i++) {
			String[] s = args[i].split("=");
			String cmd = s[0];

			if (cmd.equals("dataset")) {
				a.dataset = readDatasetArg(s[1]);
			} else if (cmd.equals("method")) {
				a.method = readMethodArg(s[1]);
			} else if (cmd.equals("whattosolve")) {
				a.whatToSolve = readWhatToSolveArg(s[1]);
			} else if (cmd.equals("timelimit")) {
				a.timeLimit = Integer.parseInt(s[1]);
			} else {
				throw new RuntimeException("Badly formatted arg: " + cmd);
			}
		}

		return a;
	}

	static WhatToSolve readWhatToSolveArg(String s) {
		if (s.equals("lp"))
			return WhatToSolve.LP;
		else if (s.equals("ip"))
			return WhatToSolve.IP;

		throw new RuntimeException("Badly formatted arg: whattosolve");
	}

	static Method readMethodArg(String s) {
		if (s.equals("chopra"))
			return Method.Chopra;
		else if (s.equals("mestrado"))
			return Method.Mestrado;
		else if (s.equals("colgen"))
			return Method.ColGen;
		else if (s.equals("dynprog"))
			return Method.DynProg;
		else if (s.equals("dynprogcut"))
			return Method.DynProgCut;

		throw new RuntimeException("Badly formatted arg: method");
	}

	static DataSet readDatasetArg(String s) {
		if (s.equals("random"))
			return DataSet.Random;
		else if (s.equals("bio_id"))
			return DataSet.BIO_ID;
		else
			throw new RuntimeException("Badly formatted arg: dataset");
	}

	public static void main(String[] argx) throws Exception {
		if(argx.length == 0) {
			Config config = new Config();
			config.solve_IP = false;
			config.solve_LP = true;
			Input input = new Input();
			Statistics stats = new Statistics(input, config);
			input.read("teste_I.txt", "teste_M.txt");
			new ColGen().solveLP(input, config, stats);
			BufferedWriter br = new BufferedWriter(new FileWriter("ColGen" + "_" + (config.solve_LP ? "LP_" : "IP_")  + "teste" + "_output.txt"));
			br.append(stats.toString());
			br.flush();
			br.close();
			System.exit(0);
		}
		
		ARGS args = readArgs(argx);
		DataSet dataSet = args.dataset;
		Method method = args.method;
		// Cut cut = args.cut;
		Config config = new Config();
		config.solve_IP = args.whatToSolve.equals(WhatToSolve.IP);
		config.solve_LP = args.whatToSolve.equals(WhatToSolve.LP);

		if (args.timeLimit != 0)
			config.TimeLimit = args.timeLimit;

		BufferedWriter br = new BufferedWriter(new FileWriter(method + "_" + (config.solve_LP ? "LP_" : "IP_")  + dataSet + "_output.txt"));

		for (String[] in : getInstances(dataSet)) {
			Statistics stats = solve(method, config, in[0], in[1]);
			br.append(stats.toString());
			br.flush();
		}

		br.close();
	}

	static List<String[]> getBioIDInstances(String rootDir) {
		LinkedList<String[]> instances = new LinkedList<String[]>();
		String model = "0117";
		//String[] img = { "0001", "0077", "0057", "0149", "1387", "1027", "1520", "0182", "0663" };
		String[] img = {"0077"};

		
		for (int i = 0; i < img.length; i++) {
			instances.addLast(new String[] { rootDir + "/BioID/BioID_" + img[i] + ".txt",
					rootDir + "/BioID/BioID_" + model + ".txt" });
		}

		return instances;
	}

	static List<String[]> getLblTreeByPrufferInstances(String rootDir) {
		LinkedList<String[]> instances = new LinkedList<String[]>();

		for (int mdlSize = 20; mdlSize <= 80; mdlSize += 20) {
			for (int imgSize = 20; imgSize <= 80; imgSize += 20) {
				for (int id = 1; id < 20; id += 2) {
					String img = rootDir + "/lbl_tree_by_prufer/lbl_tree_by_prufer_" + imgSize + "_50_50_" + id
							+ ".txt";
					String mdl = rootDir + "/lbl_tree_by_prufer/lbl_tree_by_prufer_" + mdlSize + "_50_50_" + (id + 1)
							+ ".txt";
					instances.addLast(new String[] { img, mdl });
				}
			}
		}

		return instances;
	}
}
