/*
 * 2015 mosers
 *
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.logic.RuleContainer;

import java.util.ArrayList;

/**
 *
 * @author mosers
 */
public class RuleFalse extends ArrayList{
    @Override
    public String toString(){
        String out = "rule_false:-(\n";
        int i = 0;
        while(i+1<this.size()) {
            out += this.get(i).toString() + ",\n";
            i++;
        }
        out += this.get(i).toString() + "\n).";
        return out;
    }
}
