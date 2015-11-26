package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Report{
	private Vector<String> sequence = new Vector<String>();
	private String transition = new String();
	private List<String> relevantUsers = new ArrayList<String>();	
	private Vector<String> TestedUsers = new Vector<String>();

    public int hashCode() {
        return getSequence().hashCode() + getTransition().hashCode() + getRelevantUsers().hashCode();
    }
	
	public Vector<String> getSequence() {
		return sequence;
	}
	public void setSequence(Vector<String> sequence) {
		this.sequence = sequence;
	}
	public String getTransition() {
		return transition;
	}
	public void setTransition(String transition) {
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
}
