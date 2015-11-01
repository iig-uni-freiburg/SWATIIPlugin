/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class Originator extends AbstractRuleObject{
    private String compareToString;
    
    /**
     * Constructs a Originator using the superclass constructor
     * Compares to a String
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Originator(Letter typeCase, Comparator compareWith, String compareTo) {
        super(typeCase, compareWith);
        this.compareToString = compareTo;
    }
    
    /**
     * Constructs a Origiator using the superclass constructor
     * Compares with another Originator, given by Letter compareTo
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Originator(Letter typeCase, Comparator compareWith, Letter compareTo) {
        super(typeCase, compareWith, compareTo);
    }
    
    /**
     * Prints the Originator to a prolog rule object
     * @return 
     */
    @Override
    public String toString(){
        if(compareTo == null) {
            return typeCase + "Originator" + compareWith.toString() + "'" + compareToString + "'";
        } else {
            return typeCase + "Originator" + compareWith.toString() + compareTo + "Originator";
        }
        
    }
}
