package de.uni.freiburg.iig.telematik.swatiiplugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReliefAlgorithm {

	public static void main(String[] args) throws IOException {

		String line;
		/*
		 * double out1[] = new double[9];
		 */
		double f[] = new double[10];
		HashMap<Double, Double> map = new HashMap();
		List<Double> list1 = new ArrayList<Double>();
		List<Double> list2 = new ArrayList<Double>();
		List<Double> escape = new ArrayList<Double>();

		double value, min, min_hol = 0, totaldistance, weightsfinal = 0;
		int obstructedtrace[] = { 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0 }, j = 0, i, weight, k = 0;
		Double v1 = null;
		BufferedReader br1 = new BufferedReader(
				new FileReader("C:/Users/telematik/MasterThesis/codes/succesfulltraces.txt"));
		BufferedReader br2 = new BufferedReader(
				new FileReader("C:/Users/telematik/MasterThesis/codes/obstructedtraces.txt"));
//		Finding distances between the given trace to set of successful and obstructed traces.
		while ((line = br1.readLine()) != null) {
			weight = 0;
			totaldistance = 0;
			String[] words = line.split("\\W+");
			for (i = 0; i < words.length; i++) {
				weight += Math.pow(Integer.parseInt(words[i]) - obstructedtrace[i], 2);
			}

			totaldistance = Math.sqrt(weight);
			list1.add((double) totaldistance);
		}
		while ((line = br2.readLine()) != null) {
			weight = 0;
			totaldistance = 0;
			String[] words = line.split("\\W+");
			for (i = 0; i < words.length; i++) {
				weight += Math.pow(Integer.parseInt(words[i]) - obstructedtrace[i], 2);
			}

			totaldistance = Math.sqrt(weight);
			list2.add((double) totaldistance);
		}
		/*System.out.println("list1");Prints the distances from the obstructed trace to set of successful traces.
		for (double v : list1)
			System.out.println(" " + v);

		System.out.println("\n list2");Prints the distances from the obstructed trace to set of obstructed traces.
		for (double v : list2)
			System.out.println(" " + v);*/
       // Establishing relation between the obtained distances from sets of successful and obstructed traces by considering the the pair with the least distance.
		min = list1.get(0) - list2.get(0);
		for (Double value1 : list1) {
			if (!escape.contains(value1)) {
				for (Double value2 : list2) {
					if (!escape.contains(value2)) {
						value = Math.abs(value1 - value2);
						if (map.get(value1) != null) {
							if (value < map.get(value1)) {
								map.put(value1, value);
								v1 = value2;
							}
						} else {
							map.put(value1, value);
							v1 = value2;
						}
					}
				}

				escape.add(value1);
				escape.add(v1);

			}
		}
		//Computing the weight.
		int count = 0;
		double weightFactor = 0;
		List<Double> finalList = new ArrayList<Double>(map.values());
		for (i = 1; i <= finalList.size(); i++) {
			weightFactor = weightFactor + finalList.get(i - 1);
			// System.out.println(finalList.get(i-1));
			f[count++] = weightFactor;
		}
		for (i = 0; i < count; i++)
			System.out.println("The weights are:"+f[i]);
	}
}
