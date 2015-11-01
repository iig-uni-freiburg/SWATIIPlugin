/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleObjects;

/**
 *
 * @author mosers
 */
public abstract class AbstractRuleObject {
    protected Letter typeCase;
    protected Letter compareTo;
    protected Comparator compareWith;
    
    
    public enum Comparator {
        EQUAL("="), LESSER("<"), GREATER(">"), NOT_EQUAL("!=");      
        private String value;
        private Comparator(String value){ this.value = value;}
        @Override
        public String toString(){ return this.value;}
    }
    
    public enum Letter {
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
    }
    
    public AbstractRuleObject(Letter typeCase) {
        this.typeCase = typeCase;   
    }
    
    public AbstractRuleObject(Letter typeCase, Comparator compareWith) {
        this(typeCase);
        this.compareWith = compareWith;
        this.compareTo = null;
    }
    
    public AbstractRuleObject(Letter typeCase, Comparator compareWith, Letter compareTo) {
        this(typeCase);
        this.compareWith = compareWith;
        this.compareTo = compareTo;
    }
    
    @Override
    public abstract String toString();
}
