/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.data;

import java.util.ArrayList;



/**
 *
 * @author mosers
 * @param <LogEntry>
 */
public class LogTrace<LogEntry> extends ArrayList<LogEntry>{
    public String toPrologTrace(){
        String out = "";
        for(LogEntry log : this) {
            out += "hap(" + log.toString() + ").\n";
        }
        return out;
    }
}
