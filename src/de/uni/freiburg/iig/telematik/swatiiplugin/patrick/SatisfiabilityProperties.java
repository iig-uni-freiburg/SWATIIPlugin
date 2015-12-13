package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;;

public class SatisfiabilityProperties {
	public PropertyCheckingResult isSatifiable = PropertyCheckingResult.UNKNOWN;
	public Vector<Report> report = new Vector<Report>();
	public List<String> exception = new ArrayList<String>();
	
	/*
	 * prints out the report in the form:
	 * Sequenz 1: t1, t2
	 * Transition: t1
	 * Unverzichtbare Subjekte: Bob 
	 */
	public void printReport() {
		if (report.isEmpty())
			System.out.println("Report is empty");
		else {
			System.out.println("Getestete Subjektkombinationen: " + report.get(0).getTestedUsers() + "\n");
			int sequenceCounter = 1;
			for (int i = 0; i < report.size(); i++) {
				if (i > 0) {
					if (!report.get(i).getSequence().equals(report.get(i-1).getSequence()))
						sequenceCounter++;
				}
				System.out.println("Sequenz " + sequenceCounter + ": " + report.get(i).sequenceToString(report.get(i).getSequence()));
				System.out.println("Transition: " + report.get(i).getTransition().getTransition().getName());
				if (report.get(i).getBoDSoDInfo().isEmpty())
					System.out.println("Unverzichtbare Subjekte: " + report.get(i).getRelevantUsers());
				else
					System.out.println("BoDSoDInfo: "+ report.get(i).getBoDSoDInfo());
				System.out.println("\n");
			}
		}
	}
}