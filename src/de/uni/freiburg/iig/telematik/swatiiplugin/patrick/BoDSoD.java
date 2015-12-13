package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

public class BoDSoD implements Comparable{
	private Transition t1 = null;
	private Transition t2 = null;
	public BoDSoD(Transition t1, Transition t2) {
		setT1(t1);
		setT2(t2);
	}
	public Transition getT1() {
		return t1;
	}
	public void setT1(Transition t1) {
		this.t1 = t1;
	}
	public Transition getT2() {
		return t2;
	}
	public void setT2(Transition t2) {
		this.t2 = t2;
	}
	
	// This is neccessary to sort BoDs and SoDs by the first transition
	@Override
	public int compareTo(Object o) {
		BoDSoD e = (BoDSoD) o;
		return this.getT1().getTransition().getName().compareTo(e.getT1().getTransition().getName());
	}	
}
