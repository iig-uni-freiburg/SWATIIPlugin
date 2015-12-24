package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;;

/**
 * This class manages the resilience information about a Petri net and prints out a report generated in the class {@link Report}.<br>
 * 
 * @author Patrick Notz
 *
 */
public class ResilienceProperties {
	public PropertyCheckingResult isSatifiable = PropertyCheckingResult.UNKNOWN;
	public Vector<Report> report = new Vector<Report>();
	public List<String> exception = new ArrayList<String>();
	
	/**
	 * prints out the report in the form:<br>
	 * (WSP Problem)<br>
	 * Tested Combinations: [Alice], [Bob], [Alice, Bob]<br>
	 * Sequence x: tx, ty<br>
	 * Transition: tx<br>
	 * (Vital subjects): Bob<br>
	 * (SODInfo)<br>
	 * (BoDInfo)<br>
	 */
	public void printReport() {
		if (report.isEmpty())
			System.out.println("Report is empty");
		else {
			if (!report.get(0).getTestedUsers().isEmpty())
				System.out.println("Tested Combinations: " + report.get(0).getTestedUsers() + "\n");
			int sequenceCounter = 1;
			for (int i = 0; i < report.size(); i++) {
				if (report.get(i).getWSPInfo().isEmpty()) {
					if (i > 0) {
						if (!report.get(i).getSequence().equals(report.get(i-1).getSequence())) {
							sequenceCounter++;
							System.out.print('\n');
							System.out.println("Sequence " + sequenceCounter + ": " + report.get(i).sequenceToString(report.get(i).getSequence()));
						}
					}
					if (i == 0)
						System.out.println("Sequence 1: " + report.get(i).sequenceToString(report.get(i).getSequence()));
					System.out.println("	Transition: " + report.get(i).getTransition().getName());
					if (report.get(i).getBoDInfo().isEmpty() && report.get(i).getSoDInfo().isEmpty())
							System.out.print("		Vital subjects: " + report.get(i).getRelevantUsers());
					else {
						if (!report.get(i).getSoDInfo().isEmpty())
							System.out.print("		SoDInfo: "+ report.get(i).getSoDInfo());
						if (!report.get(i).getBoDInfo().isEmpty())
							System.out.print("		BoDInfo: "+ report.get(i).getBoDInfo());
					}
					System.out.println('\n');
				}
				else
					System.out.println(report.get(i).getWSPInfo());
			}
		}
	}
}