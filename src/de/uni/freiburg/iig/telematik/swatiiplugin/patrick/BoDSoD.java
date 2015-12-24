package de.uni.freiburg.iig.telematik.swatiiplugin.patrick;

/**
 * This class creates BoD and SoD constraints containing two transitions.
 * 
 * @author Patrick Notz
 * 
 */
public class BoDSoD{
	private WSPTransition t1 = null;
	private WSPTransition t2 = null;
	public BoDSoD(WSPTransition t1, WSPTransition t2) {
		setT1(t1);
		setT2(t2);
	}
	public WSPTransition getT1() {
		return t1;
	}
	public void setT1(WSPTransition t1) {
		this.t1 = t1;
	}
	public WSPTransition getT2() {
		return t2;
	}
	public void setT2(WSPTransition t2) {
		this.t2 = t2;
	}	
}
