package de.uni.freiburg.iig.telematik.swatiiplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KnnWithSuccessfulAndObstructedTraces {
	public static void main(String[] args) {
		int[][] sources = { { 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1 }, { 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0 },
				{ 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0 }, { 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 1, 0 },
				{ 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1 }, { 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0 },
				{ 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1 }, { 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0 },
				{ 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1 } };
		Map<String, Double> newMap = new HashMap<String, Double>();

		int[] target = { 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0 };
		int j = 0;
		int k = 0;
		ArrayList<int[]> list1 = new ArrayList<int[]>();
		for (int i = 0; i < 9; i++)
			list1.add(sources[i]);
		System.out.println("The distances between source and target are as follows\n");
		for (int[] element : list1) {
			StringBuilder sb = new StringBuilder("");
			for (int m = 0; m < element.length; m++) {
				sb.append(element[m]);
			}
			double output = calculateDistance(element, sources[j], target);
			newMap.put(sb.toString(), output);
			j++;
		}

		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(newMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		for(Map.Entry<String,Double>entry:sortedMap.entrySet()) {
			if(k<5)
		  System.out.println(entry.getKey()+" "+entry.getValue());
		  k++;
		  }
	}

	private static double calculateDistance(int[] element, int[] sr, int[] target) {
		int squareddistance = 0;
		double distance = 0.0;
		if (element.length == target.length) {
			for (int i = 0; i < element.length; i++) {
				squareddistance += (int) Math.pow(Math.abs(element[i] - target[i]), 2);
			}
			distance = Math.sqrt(squareddistance);

		}
		return distance;
	}
}