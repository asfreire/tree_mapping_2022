package main;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import util.Statistics;

public class ParseResults {

	
	static void boxplot() {
		Scanner in = new Scanner(System.in);
		int[] chopra = new int[3601];
		int[] colgen = new int[3601];
		int[] mestrado = new int[3601];
		int[] dynprog = new int[3601];
		int[] dynprogcut = new int[3601];
		
		while(in.hasNextLine()) {
			String[] line = in.nextLine().trim().split("\\s+");
			int a = Integer.parseInt(line[0]);
			if(a == -1) break;
			int b = Integer.parseInt(line[1]);
			int c = Integer.parseInt(line[2]);
			int d = Integer.parseInt(line[3]);
			int e = Integer.parseInt(line[4]);
			chopra[a]++;
			colgen[b]++;
			mestrado[c]++;
			dynprog[d]++;
			dynprogcut[e]++;
		}
		
		for(int i = 1; i < chopra.length; i++) {
			chopra[i] += chopra[i-1];
			colgen[i] += colgen[i-1];
			mestrado[i] += mestrado[i-1];
			dynprog[i] += dynprog[i-1];
			dynprogcut[i] += dynprogcut[i-1];
			System.out.println(i + "\t" + chopra[i] + "\t" + colgen[i] + "\t" + mestrado[i] + "\t" + dynprog[i] + "\t" + dynprogcut[i]);
		}
		
		in.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		boxplot();
		System.exit(0);
		String root = "/home/chegado/Documentos/root/trabalho/implementacoes/2022 - Tree Mapping/workspace/Tree Mapping/resultados/";
		HashMap<String, Statistics[]> map = new HashMap<>();
		String[] method = { "Chopra", "Mestrado", "DynProg", "DynProgCut", "ColGen" };
		String[] dataset = { "Random"};
		String[] lpip = { "LP", "IP" };
		int nDataSets = dataset.length;
		int nMethods = method.length * 2;
		int i = 0;

		for (String d : dataset) {
			for (String m : method) {
				for (String l : lpip) {
					File file = new File(root + m + "_" + l + "_" + d + "_output.txt");

					if (!file.exists()) {
						i++;
						continue;
					}

					Scanner in = new Scanner(file);

					while (in.hasNextLine()) {
						String[] line = in.nextLine().split("\\s+");
						String key = line[0] + "---" + line[1];

						if (!map.containsKey(key)) {
							map.put(key, new Statistics[nDataSets * nMethods]);
						}

						Statistics[] stat = map.get(key);
						Statistics s = new Statistics(null, null);
						s.Isize = Integer.parseInt(line[2]);
						s.Msize = Integer.parseInt(line[3]);
						s.bestVal = Double.parseDouble(line[5]);
						s.spentTime = Integer.parseInt(line[6]);
						s.nCuts = Integer.parseInt(line[7]);
						s.nSubProblemsDynProg = Integer.parseInt(line[8]);
						s.nSubProblemsDynProgInt = Integer.parseInt(line[9]);
						stat[i] = s;
					}

					i++;
				}
			}
		}

		LinkedList<Statistics[]> S = new LinkedList<>(map.values());
		S.sort(new Comparator<Statistics[]>() {

			@Override
			public int compare(Statistics[] o1, Statistics[] o2) {
				return o1[0].Isize == o2[0].Isize ? o1[0].Msize - o2[0].Msize : o1[0].Isize - o2[0].Isize;
			}
		});
		
		for (Statistics[] s : S) {
			int Isize = 0;
			int Msize = 0;
			for (int flag = 0; flag <= 3; flag++) {
				i = 0;
				for (String m : method) {
					for (String d : dataset) {
						for (String l : lpip) {
							if (s[i] == null) {
								if(flag == 1 || flag == 2)
									System.out.print("---\t");
							} else {
								Isize = s[i].Isize;
								Msize = s[i].Msize;
								if (flag == 1) {
									System.out.print((s[i].bestVal + "\t").replace('.', ','));
								}

								if (flag == 2) {
									System.out.print((s[i].spentTime + "\t").replace('.', ','));
								}
								if (flag == 3) {
									// 4 DynProg LP
									// 5 DynProg IP
									// 6 DynProgCut LP
									// 7 DynProgCut IP
									if(i == 4)
										System.out.print(s[i].nSubProblemsDynProg + "\t");
									
									if (4 == i || i == 6) {
//										System.out.print(s[i].nCuts + "\t");
										System.out.print(s[i].nSubProblemsDynProgInt + "\t");
									}
								}
							}
							i++;
						}
					}
				}

				if (flag == 0) {
					System.out.print(Isize + "\t" + Msize + "\t");
				}
			}
			
			System.out.println();
		}
	}
}
