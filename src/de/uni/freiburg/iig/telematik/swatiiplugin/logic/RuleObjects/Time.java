/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

import java.util.Date;

/**
 *
 * @author mosers
 */
public class Time extends AbstractRuleObject{
    private Date compareToDate;  
    
    
    /**
     * Constructs a Time using the superclass constructor
     * compares to a Date
     * @param typeCase
     * @param compareTo 
     * @param compareWith 
     */
    public Time(Letter typeCase, Comparator compareWith, Date compareTo) {
        super(typeCase, compareWith);
        this.compareToDate = compareTo;
    }
    
    /**
     * Constructs a Time using the superclass constructor
     * compares to another Time, selected by Letter compareTo
     * @param typeCase
     * @param compareTo 
     * @param compareWith 
     */
    public Time(Letter typeCase, Comparator compareWith, Letter compareTo) {
        super(typeCase, compareWith, compareTo);
    }
    
    /**
     * Prints the Time to a prolog rule object
     * @return 
     */
    @Override
    public String toString(){
        if(compareTo == null){
            return typeCase + "Time" + compareWith.toString() + "'" + compareToDate.getTime() + "'";
        } else {
            return typeCase + "Time" + compareWith.toString() + compareTo + "Time";
        }
        
    }
}
