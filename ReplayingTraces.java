package de.uni.freiburg.iig.telematik.swatiiplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.invation.code.toval.parser.ParserException;

import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsing;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsingFormat;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.mg.MarkingGraphException;
import de.uni.freiburg.iig.telematik.sepia.replay.Replay;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayCallableGenerator;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayException;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayResult;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.log.LogTraceUtils;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayCallable.TerminationCriteria;

public class ReplayingTraces {
	public static void main(String[] args) throws IOException, ParserException, MarkingGraphException {

		int[][] matrix = new int[80][12];
		PrintStream pt1 = new PrintStream(new FileOutputStream("C:/Users/telematik/MasterThesis/parikhvectors.txt"));
		boolean contains;
		List<String> list = Arrays.asList("at1", "ct1", "dt1", "at2", "ct2", "dt2", "bt3", "dt3", "bt4", "dt4", "at5", "dt5");
		int i, j, index1, row = 0, col = 0;
		String line, regex = "^[a-d][t][1-5]";
		for (i = 0; i < 80; i++) {
			for (j = 0; j < 12; j++)
				matrix[i][j] = 0;
			}
		BufferedReader br1 = new BufferedReader(new FileReader("C:/Users/telematik/MasterThesis/outputtraces.txt"));
		while ((line = br1.readLine()) != null) {
			if (!line.isEmpty() && line.contains("te")) {
				String[] parts = line.split("\\s+");
				for (String a : parts) {
					if (a.matches(regex)) {
						if (list.contains(a)) {
							index1 = list.indexOf(a);
							if (index1 != -1)
								matrix[row][index1] = 1;
						}
					}
				}
				row++;
			}
		}

		for (i = 0; i < row; i++) {
			for (j = 0; j < 12; j++)
				pt1.print(matrix[i][j] + "\t");
			pt1.println(" ");
		}
		for (i = 0; i < row; i++) {
			System.out.println("\n");
			for (j = 0; j < 12; j++)
				System.out.print(" " + matrix[i][j]);

		}

	}
}
