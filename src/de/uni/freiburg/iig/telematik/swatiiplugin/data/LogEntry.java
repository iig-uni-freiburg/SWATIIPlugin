/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.swatiiplugin.data;

import java.sql.Timestamp;

/**
 *
 * @author mosers
 */
public class LogEntry {
    private final String activity;
    private ActivityStatus status;
    private String originator;
    private String role;
    private long time;
    
    /*
    *   Getter and Setter
    */
    /**
     * @return the activity
     */
    public String getActivity() {
        return activity;
    }

    /**
     * @return the status
     */
    public ActivityStatus getStatus() {
        return status;
    }

    /**
     * @return the originator
     */
    public String getOriginator() {
        return originator;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    /**
     * @param originator the originator to set
     */
    public void setOriginator(String originator) {
        this.originator = originator;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }
    
    /*
    *   Constructor
    */
    /**
     * The constructor for LogEntries
     * @param activity 
     */
    public LogEntry(String activity) {
        this.activity = activity;
    }
    
    public LogEntry(ActivityStatus status, String activity, String originator, String role, long time) {
        this.activity = activity;
        this.setStatus(status);
        this.setOriginator(originator);
        this.setRole(role);
        this.setTime(time);
    }
    
    /**
     * Prints the LogEntry
     * @return 
     */
    @Override
    public String toString(){
        String out = "activity(";
        out += getStatus().toString() + ",'";
        out += getActivity() + "','";
        out += getOriginator() + "','";
        out += getRole() + "'),";
        out += getTime();
        return out;
    }
}
