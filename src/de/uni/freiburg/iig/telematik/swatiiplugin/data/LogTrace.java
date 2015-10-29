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
