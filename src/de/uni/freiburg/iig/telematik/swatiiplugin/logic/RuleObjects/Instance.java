/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class Instance extends AbstractRuleObject{
    private int compareToInt;
    
    /**
     * Constructs a Instance using the superclass constructor
     * Compares to an int
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Instance(Letter typeCase, Comparator compareWith, int compareTo) {
        super(typeCase, compareWith);
        this.compareToInt = compareTo;
    }
    
    /**
     * Constructs a Instance using the superclass constructor
     * compares to another Instance, given by Letter compareTo
     * @param typeCase
     * @param compareWith
     * @param compareTo 
     */
    public Instance(Letter typeCase, Comparator compareWith, Letter compareTo) {
        super(typeCase, compareWith, compareTo);
    }
    
    /**
     * Prints the Instance to a prolog rule object
     * @return 
     */
    @Override
    public String toString(){
        if(compareTo==null){
            return typeCase + "Instance" + compareWith.toString() + compareToInt;
        } else {
            return typeCase + "Instance" + compareWith.toString() + compareTo + "Instance";
        }
        
    } 
}
