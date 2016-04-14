package de.uni.freiburg.iig.telematik.swatiiplugin.logic;

import de.invation.code.toval.types.HashList;
import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mosers
 */
public class Violation {

    private String name;
    private HashMap<String, String> values;
    private HashList<LogEntry> entries;

    public Violation() {
        this.values = new HashMap<>();
        this.entries = new HashList<>();
    }

    public Violation(String toParse) throws LockingException {
        this();
        String[] splitted = toParse.split("><");
        LogEntry e = new LogEntry("0");
        for (String part : splitted) {
            if (part.contains("name>")) {
                String pName = part.split(">")[1].split("<")[0];
                this.name = pName;
            } else if (part.contains("</value")) {
                String key = part.split("\"")[1];
                String value = part.split(">")[1].split("<")[0];
                this.values.put(key, value);
            } else if (part.contains("</process")) {
                String instance = part.split(">")[1].split("<")[0];
                e.addMetaAttribute(new DataAttribute("instance", instance));
            } else if (part.contains("</activity")) {
                String act = part.split(">")[1].split("<")[0];
                e.setActivity(act);
            } else if (part.contains("</subject")) {
                String sub = part.split(">")[1].split("<")[0];
                e.setOriginator(sub);
            } else if (part.contains("</role")) {
                String role = part.split(">")[1].split("<")[0];
                e.setRole(role);
            } else if (part.contains("</time")) {
                long time = Long.parseLong(part.split(">")[1].split("<")[0]);
                e.setTimestamp(new Date(time));
            }            
        }
        if(!e.getActivity().equals("0")) {
            entries.add(e);
        }        
    }

    public Violation(String name, String[] keyValues, int count) {
        this();
        this.name = name;
        for (int i = 0; i < count; i++) {
            this.values.put(keyValues[2 * i], keyValues[2 * i + 1]);
        }
    }

    public Violation(String name, String[] keyValues, int count, LogEntry e) {
        this(name, keyValues, count);
        this.entries.add(e);
    }

    public Violation(String name, String[] keyValues, int count, LogEntry[] e) {
        this(name, keyValues, count);
        this.entries.add(e);
    }

    public boolean equals(Violation v) {
        if (!this.name.equals(v.getName())) {
            return false;
        }
        if (!this.values.equals(v.getValues())) {
            return false;
        }
        return true;
    }

    public void append(Violation v) {
        this.entries.addAll(v.getEntries());
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the values
     */
    public HashMap<String, String> getValues() {
        return values;
    }

    /**
     * @return the entries
     */
    public HashList<LogEntry> getEntries() {
        return entries;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param values the values to set
     */
    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(HashList<LogEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        String echo = "There was a violation named '" + this.getName() + "' including ";
        boolean first = true;
        for (Map.Entry<String, String> entry : this.getValues().entrySet()) {
            if (!first) {
                echo += " and ";
            }
            echo += entry.getKey() + " '" + entry.getValue() + "'";
            first = false;
        }
        echo += " at this entries:\n";
        for (LogEntry entry : this.getEntries()) {
            String instance = "0";
            for(DataAttribute data : entry.getMetaAttributes()){
                if(data.name.equals("instance")){
                    instance = (String) data.value;
                }                
            }
            String actString = "hap(activity(" + instance + ", complete,'";
            actString += entry.getActivity() + "','" + entry.getOriginator() + "','";
            actString += entry.getRole() + "')," + entry.getTimestamp().getTime() + ").\n";
            echo += actString;
        }
        return echo;
    }

    public String shortString() {
        String echo = "<" + this.getName();
        boolean first = true;
        for (Map.Entry<String, String> entry : this.getValues().entrySet()) {
            echo += " " + entry.getKey() + "='" + entry.getValue() + "'";
            first = false;
        }
        echo += ">";
        return echo;
    }

}
