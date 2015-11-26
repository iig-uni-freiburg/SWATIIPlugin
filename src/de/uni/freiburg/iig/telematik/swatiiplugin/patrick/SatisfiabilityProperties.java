package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;;

public class SatisfiabilityProperties {
	public PropertyCheckingResult isSatifiable = PropertyCheckingResult.UNKNOWN;
	public Vector<Report> report = new Vector<Report>();
	public List<String> exception = new ArrayList<String>();
	
	public void printReport() {
		if (report.isEmpty())
			System.out.println("Report is empty");
		else
			System.out.println("Getestete Subjektkombinationen: " + report.get(0).getTestedUsers() + "\n");
			int sequenceCounter = 1;
			for (int i = 0; i < report.size(); i++) {
				if (i > 0) {
					if (!report.get(i).getSequence().equals(report.get(i-1).getSequence()))
						sequenceCounter++;
				}
				System.out.println("Sequenz " + sequenceCounter + ": " + report.get(i).getSequence());
				System.out.println("Transition: " + report.get(i).getTransition());
				System.out.println("Unverzichtbare Subjekte: " + report.get(i).getRelevantUsers());
				System.out.println("\n");
			}
				
	}
}