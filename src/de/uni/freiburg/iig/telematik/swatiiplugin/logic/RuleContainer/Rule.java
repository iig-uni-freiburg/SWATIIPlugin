package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer;

/**
 *
 * @author mosers
 */
public class Rule {
    private Object content;
    private String name;
    
    /**
     * Constructor for not
     * @param name
     * @param content 
     */
    public Rule(String name, Object content) {
        this.name = name;
        this.content = content;
    }
    
    /**
     * Constructor for not 
     * @param name
     */
    public Rule(String name) {
        this.name = name;
    }
    
    public void add(Object content) {
        this.content = content;
    }
    
    /**
     * Prints the object to prolog
     * @return 
     */
    @Override
    public String toString(){
        return name + ":-(\n" + content.toString() + "\n).";
    }
}
