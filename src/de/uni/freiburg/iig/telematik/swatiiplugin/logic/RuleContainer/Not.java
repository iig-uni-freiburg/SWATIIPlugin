package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer;

/**
 *
 * @author mosers
 */
public class Not {
    private Object content;
    
    /**
     * Constructor for not
     * @param content 
     */
    public Not(Object content) {
        this.content = content;
    }
    
    /**
     * Prints the object to prolog
     * @return 
     */
    @Override
    public String toString(){
        return "not(\n" + content.toString() + "\n).";
    }
}
