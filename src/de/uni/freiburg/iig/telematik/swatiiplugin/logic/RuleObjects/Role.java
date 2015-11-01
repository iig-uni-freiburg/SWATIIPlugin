/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class Role extends AbstractRuleObject{
    private String compareToString;
    
    /**
     * Constructs a Role using the superclass constructor
     * compares to a String
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Role(Letter typeCase, Comparator compareWith, String compareTo) {
        super(typeCase, compareWith);
        this.compareToString = compareTo;
    }

    /**
     * Constructs a Role using the superclass constructor
     * compares to another Role give by Letter compareTo
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Role(Letter typeCase, Comparator compareWith, Letter compareTo) {
        super(typeCase, compareWith, compareTo);        
    }
    
    /**
     * Prints the Role to a prolog rule object     * 
     * @return 
     */
    @Override
    public String toString(){
        if(compareTo == null) {
            return typeCase + "Role" + compareWith.toString() + "'" + compareToString + "'";
        } else {
            return typeCase + "Role" + compareWith.toString() + compareTo + "Role";
        }
    }
}
