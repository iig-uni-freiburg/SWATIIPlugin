package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.uni.freiburg.iig.telematik.sepia.petrinet.properties.PropertyCheckingResult;;

/**
 * This class manages the robustness information about a Petri net and prints out a report generated in the class {@link Report}.<br>
 * 
 * @author Patrick Notz
 *
 */
public class RobustnessProperties {
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
		if (!report.isEmpty() && exception.isEmpty()) {
			if (!report.get(0).getTestedSubjects().isEmpty())
				System.out.println("Examine the relevance regarding robustness in a WF-Net for the following combinations of subjects: " + "\n"+ report.get(0).getTestedSubjects() + "\n");
			int sequenceCounter = 1;
			if (isSatifiable == PropertyCheckingResult.TRUE)
				System.out.println("The workflow is satisfiable");
			if (isSatifiable == PropertyCheckingResult.FALSE)
				System.out.println("The workflow is not satisfiable");
			
			if (isSatifiable == PropertyCheckingResult.TRUE) {
				if (getNumberOfSequences() == 1)
					System.out.println("The WF-Net contains one sequences" + "\n");
				else
					System.out.println("The WF-Net contains " + getNumberOfSequences() + " different sequences" + "\n");
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
						System.out.print("		All authorized subjects: " + report.get(i).getAllSubjects() + "\n");
						if (report.get(i).getBoDInfo().isEmpty() && report.get(i).getSoDInfo().isEmpty())
								System.out.print("		Subjects relevant for robustness: " + report.get(i).getRelevantSubjects());
						else {
							if (!report.get(i).getSoDInfo().isEmpty())
								if (!report.get(i).getBoDInfo().isEmpty())
									System.out.print("		SoDInfo: "+ report.get(i).getSoDInfo() + "\n");
								else
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
		else
			for (int i=0; i<exception.size(); i++)
				System.out.println(exception.get(i));
	}
	
	public int getNumberOfSequences() {
		int sequenceCounter = 0;
		for (int i = 0; i < report.size(); i++) {
			if (i == 0)
				sequenceCounter++;
			else
				if (report.get(i).getSequence() != report.get(i-1).getSequence())
					sequenceCounter++;
		}
		return sequenceCounter;
	}
}