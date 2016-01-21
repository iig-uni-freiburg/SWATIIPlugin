package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class stores every single information about the robustness of a Petri net gathered from {@link RobustnessChecker}.
 * 
 * @author Patrick Notz
 *
 */
public class Report{
	private Vector<WSPTransition> sequence = new Vector<WSPTransition>();
	private WSPTransition transition = null;
	private String WSPInfo = "";
	private List<String> allSubjects = new ArrayList<String>();	
	private List<String> relevantSubjects = new ArrayList<String>();	
	private Vector<String> testedSubjects = new Vector<String>();
	private String boDInfo = new String();
	private String soDInfo = new String();

	/**
	 *  A report needs only to be saved, if a transition with additional information is not in the report already.<br>
	 *  Therefore the hashCode of relevant components needs to be compared.
	 */
    public int hashCode() {
        return getSequence().hashCode() + getTransition().hashCode() + getRelevantSubjects().hashCode() + getBoDInfo().hashCode() + getSoDInfo().hashCode();
    }
	
	public Vector<WSPTransition> getSequence() {
		return sequence;
	}
	public void setSequence(Vector<WSPTransition> sequence) {
		this.sequence = sequence;
	}
	public WSPTransition getTransition() {
		return transition;
	}
	public void setTransition(WSPTransition transition) {
		this.transition = transition;
	}
	public List<String> getRelevantSubjects() {
		return relevantSubjects;
	}
	public void setRelevantSubjects(List<String> relevantSubjects) {
		this.relevantSubjects = relevantSubjects;
	}

	public Vector<String> getTestedSubjects() {
		return testedSubjects;
	}

	public void setTestedSubjects(Vector<String> testedSubjects) {
		this.testedSubjects = testedSubjects;
	}
	
	public List<String> getAllSubjects() {
		return allSubjects;
	}

	public void setAllSubjects(List<String> allSubjects) {
		this.allSubjects = allSubjects;
	}

	public String getBoDInfo() {
		return boDInfo;
	}

	public void setBoDInfo(String boDInfo) {
		this.boDInfo = boDInfo;
	}
	
	public String getSoDInfo() {
		return soDInfo;
	}

	public void setSoDInfo(String soDInfo) {
		this.soDInfo = soDInfo;
	}
	
	/**
	 * Function to convert and format a vector containing transitions to String.
	 * @param sequence Stores a sequence of a workflow containing transitions.
	 * @return Formated sequence
	 */
	public String sequenceToString(Vector<WSPTransition> sequence) {
		String sequenceString = "";
		for (int i = 0; i < sequence.size(); i++) {
			sequenceString = sequenceString + sequence.get(i).getName() + ", ";
		}
		sequenceString = sequenceString.substring(0, sequenceString.length()-2);
		return sequenceString;	
	}

	public String getWSPInfo() {
		return WSPInfo;
	}

	public void setWSPInfo(String wSPInfo) {
		WSPInfo = wSPInfo;
	}
}
