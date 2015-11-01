/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class Type extends AbstractRuleObject{
    private String compareToString;
    
    /**
     * Constructs a Type using the superclass constructor
     * compares to a String
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Type(Letter typeCase, Comparator compareWith, String compareTo) {
        super(typeCase, compareWith);
        this.compareToString = compareTo;
    }
    
        /**
     * Constructs a Type using the superclass constructor
     * compares to another Type selected by Letter compareTo
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Type(Letter typeCase, Comparator compareWith, Letter compareTo) {
        super(typeCase, compareWith, compareTo);        
    }
    
    /**
     * Prints the Type to a prolog rule object
     * @return 
     */
    @Override
    public String toString(){
        if(compareTo == null) {
            return typeCase + "Type" + compareWith.toString() + "'" + compareToString + "'";
        } else {
            return typeCase + "Type" + compareWith.toString() + compareTo + "Type";
        }
    }
}
