/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer;

/**
 *
 * @author mosers
 */
public class Brace extends java.util.ArrayList{
    @Override
    public String toString(){
        String out = "(\n";
        int i = 0;
        while(i+1<this.size()) {
            out += this.get(i).toString() + ",\n";
            i++;
        }
        out += this.get(i).toString() + "\n)";
        return out;
    }
}
