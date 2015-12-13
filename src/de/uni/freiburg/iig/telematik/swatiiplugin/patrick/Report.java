package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Report{
	private Vector<Transition> sequence = new Vector<Transition>();
	private Transition transition = null;
	private List<String> relevantUsers = new ArrayList<String>();	
	private Vector<String> TestedUsers = new Vector<String>();
	private String boDSoDInfo = new String();

	// A report needs only to be saved, if the transition and the user which are taken from the process are not in the report already.
	// Therefore the hashCode of the transition and the users needs to be compared.
    public int hashCode() {
        return getSequence().hashCode() + getTransition().hashCode() + getRelevantUsers().hashCode();
    }
	
	public Vector<Transition> getSequence() {
		return sequence;
	}
	public void setSequence(Vector<Transition> sequence) {
		this.sequence = sequence;
	}
	public Transition getTransition() {
		return transition;
	}
	public void setTransition(Transition transition) {
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

	public String getBoDSoDInfo() {
		return boDSoDInfo;
	}

	public void setBoDSoDInfo(String boDSoDInfo) {
		this.boDSoDInfo = boDSoDInfo;
	}
	
	// Function to convert Vector<Transition> to String in order to obtain a sequence
	public String sequenceToString(Vector<Transition> sequence) {
		String sequenceString = "";
		for (int i = 0; i < sequence.size(); i++) {
			sequenceString = sequenceString + sequence.get(i).getTransition().getName() + ", ";
		}
		sequenceString = sequenceString.substring(0, sequenceString.length()-2);
		return sequenceString;	
	}
}
