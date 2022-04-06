package util;

import datastructures.Input;

public class Statistics {
	Input input;
	public Config config;
	public double bestVal;
	public int spentTime;
	public int numberOfBBN;
	public int nSubProblemsDynProg;
	public int nSubProblemsDynProgInt;
	public int nCuts;
	public int Isize;
	public int Msize;

	public Statistics(Input input, Config config) {
		this.input = input;
		this.config = config;
	}

	@Override
	public String toString() {
		return input.imgFile + "\t" + input.mdlFile + "\t" + input.I.size + "\t" + input.M.size + "\t"
				+ (config.solve_LP ? "LP" : "IP") + "\t" + bestVal + "\t" + spentTime + "\t" + nCuts + "\t"
				+ nSubProblemsDynProg + "\t" + nSubProblemsDynProgInt + "\n";
	}
}
