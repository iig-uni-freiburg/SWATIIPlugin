package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class stores every single information about the resilience of a Petri net gathered from {@link ResilienceChecker}.
 * 
 * @author Patrick Notz
 *
 */
public class Report{
	private Vector<WSPTransition> sequence = new Vector<WSPTransition>();
	private WSPTransition transition = null;
	private String WSPInfo = "";
	private List<String> relevantUsers = new ArrayList<String>();	
	private Vector<String> TestedUsers = new Vector<String>();
	private String boDInfo = new String();
	private String soDInfo = new String();

	/**
	 *  A report needs only to be saved, if a transition with additional information is not in the report already.<br>
	 *  Therefore the hashCode of relevant components needs to be compared.
	 */
    public int hashCode() {
        return getSequence().hashCode() + getTransition().hashCode() + getRelevantUsers().hashCode() + getBoDInfo().hashCode() + getSoDInfo().hashCode();
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
	public List<String> getRelevantUsers() {
		return relevantUsers;
	}
	public void setRelevantUsers(List<String> relevantUsers) {
		this.relevantUsers = relevantUsers;
	}

	public Vector<String> getTestedUsers() {
		return TestedUsers;
	}

	public void setTestedUsers(Vector<String> testedUsers) {
		TestedUsers = testedUsers;
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
