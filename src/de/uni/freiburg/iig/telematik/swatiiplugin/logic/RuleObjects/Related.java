package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class Related extends AbstractRuleObject{
    private String compareToString;
    
    /**
     * Constructs a relation using two letters
     * @param typeCase
     * @param compareTo 
     */
    public Related(Letter typeCase, Letter compareTo) {
        super(typeCase);
        super.compareTo = compareTo;
    }
    
    /**
     * Constructs a relation using a letter and a string
     * @param typeCase
     * @param compareTo 
     */
    public Related(Letter typeCase, String compareTo) {
        super(typeCase);
        this.compareToString = compareTo;
    }
    
    
    @Override
    public String toString() {
        if(compareTo == null) {
            return "related(" + typeCase + "Role,'" + compareToString + "')";
        } else {
            return "related(" + typeCase + "Role," + compareTo + "Role)";
        }
    }
    
}
