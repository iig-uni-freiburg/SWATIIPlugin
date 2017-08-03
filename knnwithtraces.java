package de.uni.freiburg.iig.telematik.swatiiplugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class knnwithtraces {
	public static void main(String[] args) throws FileNotFoundException {
		long startTime = System.currentTimeMillis();
		BufferedReader br1 = new BufferedReader(new FileReader("C:/Users/telematik/MasterThesis/parikhvectors.txt"));
		Map<String, Double> newMap = new HashMap<String, Double>();
		int[][] sources = new int[50][12];
		int[] target = { 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0 };
		int j = 0, i;
		int k = 0;
		ArrayList<int[]> list1 = new ArrayList<int[]>();
		int row = 0;
		String line;
		try {
			while ((line = br1.readLine()) != null) {
				if (!line.equals(" ")) {
					String[] words = line.split("\\W+");
					for (i = 0; i < words.length; i++) {
						sources[row][i] = Integer.parseInt(words[i]);
					}
					row++;
				}
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (i = 0; i < row; i++)
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

		for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
			if (k < 10)
				System.out.println(entry.getKey() + " " + entry.getValue());
			k++;
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken to execute the program is :" + totalTime / 1000.0);
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
