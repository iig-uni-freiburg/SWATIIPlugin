package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public class PartnerOf extends AbstractRuleObject{
    private String compareToString;
    
    /**
     * Constructs a partner using two letters
     * @param typeCase
     * @param compareTo 
     */
    public PartnerOf(Letter typeCase, Letter compareTo) {
        super(typeCase);
        super.compareTo = compareTo;
    }
    
    /**
     * Constructs a partner using a letter and a string
     * @param typeCase
     * @param compareTo 
     */
    public PartnerOf(Letter typeCase, String compareTo) {
        super(typeCase);
        this.compareToString = compareTo;
    }
    
    
    @Override
    public String toString() {
        if(compareTo == null) {
            return "partner_of(" + typeCase + "Role,'" + compareToString + "')";
        } else {
            return "partner_of(" + typeCase + "Role," + compareTo + "Role)";
        }
    }
    
}
