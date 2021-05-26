package com.example.aibasedmorsecode.model;

public class Receiver extends User{
    private Integer sessionID;

    /**
     * Create Receiver object with four parameters
     * @param id
     * @param username
     * @param points
     * @param sessionID
     */
    public Receiver(int id, String username, int points, Integer sessionID) {
        super(id, username, points);
        this.sessionID = sessionID;
    }

    /**
     * Create Receiver object with one parameter
     * @param username
     */
    public Receiver(String username) {
        super(username);
    }

    /**
     * @return sessionID as Integer
     */
    public Integer getSessionID() {
        return sessionID;
    }

    /**
     * @param sessionID as Integer
     */
    public void setSessionID(Integer sessionID) {
        this.sessionID = sessionID;
    }
}
