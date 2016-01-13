package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class SameGroup extends AbstractRuleObject{
    private String compareToString;
    
    /**
     * Constructs a "groupie" using two letters
     * @param typeCase
     * @param compareTo 
     */
    public SameGroup(Letter typeCase, Letter compareTo) {
        super(typeCase);
        super.compareTo = compareTo;
    }
    
    /**
     * Constructs a "groupie" using a letter and a string
     * @param typeCase
     * @param compareTo 
     */
    public SameGroup(Letter typeCase, String compareTo) {
        super(typeCase);
        this.compareToString = compareTo;
    }
    
    
    @Override
    public String toString() {
        if(compareTo == null) {
            return "same_group(" + typeCase + "Role,'" + compareToString + "')";
        } else {
            return "same_group(" + typeCase + "Role," + compareTo + "Role)";
        }
    }
    
}
