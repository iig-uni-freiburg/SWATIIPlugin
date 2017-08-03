package de.uni.freiburg.iig.telematik.swatiiplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.parser.ParserException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.MGTraversalResult;
import de.uni.freiburg.iig.telematik.sepia.mg.pt.PTMarkingGraph;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsing;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsingFormat;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.mg.MGCalculator;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.mg.MGConstruction;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.mg.MarkingGraphException;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.mg.ThreadedMGCalculator;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGeneration;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationCallableGenerator;
import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.sequences.SequenceGenerationException;
import de.uni.freiburg.iig.telematik.sepia.replay.Replay;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayCallable.TerminationCriteria;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayCallableGenerator;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayException;
import de.uni.freiburg.iig.telematik.sepia.replay.ReplayResult;

public class MarkingGraph {

	public static void main(String[] args) throws IOException, ParserException, MarkingGraphException {
		File inputFile = new File("C:/Users/telematik/MasterThesis/net1withut_without_loop.pnml");
		PrintStream pt1 = new PrintStream(new FileOutputStream("C:/Users/telematik/MasterThesis/outputtraces.txt"));
		AbstractGraphicalPN gNet = PNParsing.parse(inputFile, PNParsingFormat.PNML);
//	To generate marking graph.
		//PrintStream pt2 = new PrintStream(new FileOutputStream("C:/Users/telematik/MasterThesis/outputobstructed.txt"));
		// MGCalculator ptnet = new ThreadedMGCalculator<>(gNet.getPetriNet());
		// PTMarkingGraph mg = (PTMarkingGraph)
		// MGConstruction.buildMarkingGraph(ptnet);
		// System.out.println(mg.toString());
		SequenceGenerationCallableGenerator generator = new SequenceGenerationCallableGenerator(gNet.getPetriNet());

		MGTraversalResult res = null;
		try {
			res = SequenceGeneration.getFiringSequences(generator);
		} catch (SequenceGenerationException e) {
			// TODO Auto-generated catch block
			System.out.println("\n" + e.getLocalizedMessage());
		}
		Set<List<String>> output = new HashSet<List<String>>();
		output= res.getCompleteSequences();
		System.out.println(output.size());
		for (List<String> out1 : output) {
			pt1.println("\n");
			for (String out2 : out1) {
				System.out.print(out2 + "\t");
				pt1.print(out2 + "\t");
			}

		}
		/*outss = res.getSequences();
		pt1.println("\n\n");
		System.out.println(outss.size());
		for (List<String> out1 : outss) {
			pt1.println("\n");
			for (String out2 : out1) {
				System.out.print(out2 + "\t");
				pt1.print(out2 + "\t");
			}*/

		}

	}

