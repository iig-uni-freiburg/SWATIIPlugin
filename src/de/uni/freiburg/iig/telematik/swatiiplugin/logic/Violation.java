package de.uni.freiburg.iig.telematik.swatiiplugin.logic;

import de.uni.freiburg.iig.telematik.sewol.log.DataAttribute;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author mosers
 */
public class Violation {

    private String name;
    private HashMap<String, String> values;
    private ArrayList<LogEntry> entries;

    public Violation() {
        this.values = new HashMap<>();
        this.entries = new ArrayList<>();
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
        if (!e.getActivity().equals("0")) {
            this.entries.add(e);
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

    public Violation(String name, String[] keyValues, int count, ArrayList eV) {
        this(name, keyValues, count);
        this.entries.addAll(eV);
    }

    public boolean equals(Violation v) {
        if (getName().equals(v.getName()) && getValues().equals(v.getValues())) {
            return true;
        }
        return false;
    }

    public void append(Violation v) {
        getEntries().addAll(v.getEntries());
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
    public ArrayList<LogEntry> getEntries() {
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

    @Override
    public String toString() {
        String echo = "There was a violation '" + getName() + "' with ";
        boolean first = true;
        for (String key : getValues().keySet()) {
            if (!first) {
                echo += " and ";
            }
            echo += key + " '" + getValues().get(key) + "'";
            first = false;
        }
        if (!getEntries().isEmpty()) {
            echo += " at this entries:";
            for (LogEntry entry : getEntries()) {
                String instance = "0";
                for (DataAttribute data : entry.getMetaAttributes()) {
                    if (data.name.equals("instance")) {
                        instance = (String) data.value;
                    }
                }
                String actString = "\nhap(activity(" + instance + ", complete,'";
                actString += entry.getActivity() + "','" + entry.getOriginator() + "','";
                actString += entry.getRole() + "')," + entry.getTimestamp().getTime() + ").";
                echo += actString;
            }
        }
        return echo + "\n";
    }

    public String shortString() {
        String echo = "<" + getName();
        for (String key : getValues().keySet()) {
            echo += " " + key + "='" + getValues().get(key) + "'";
        }
        echo += ">";
        return echo;
    }

}
