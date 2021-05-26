package com.example.aibasedmorsecode.model;

import java.util.ArrayList;
import java.util.Date;

public class MultiUserSession {
    private int id;
    private String sessionName;
    private String learningType;
    private int numRounds;
    private String senderName;
    private ArrayList<Receiver> receivers;
    private Date date;
    private Integer categoryid;
    private String categoryName;

    /**
     * Creating MultiUserSession object with seven parameters (the last parameter is int)
     * @param id
     * @param sessionName
     * @param learningType
     * @param numRounds
     * @param date
     * @param senderName
     * @param categoryid
     */
    public MultiUserSession(int id, String sessionName, String learningType, int numRounds, Date date, String senderName, Integer categoryid) {
        this.id = id;
        this.sessionName = sessionName;
        this.learningType = learningType;
        this.categoryid = categoryid;
        this.numRounds = numRounds;
        this.senderName = senderName;
        this.date = date;
    }

    /**
     * Creating MultiUserSession object with seven parameters (the last parameter is String)
     * @param sessionName
     * @param learningType
     * @param numRounds
     * @param senderName
     * @param receivers
     * @param date
     * @param categoryName
     */
    public MultiUserSession(String sessionName, String learningType, int numRounds, String senderName, ArrayList<Receiver> receivers, Date date, String categoryName) {
        this.sessionName = sessionName;
        this.learningType = learningType;
        this.categoryName = categoryName;
        this.numRounds = numRounds;
        this.senderName = senderName;
        this.receivers = receivers;
        this.date = date;
    }

    /**
     * Creating MultiUserSession object with one parameter
     * @param sessionName
     */
    public MultiUserSession(String sessionName) {
        this.sessionName = sessionName;
        this.date = new Date();
        this.receivers = new ArrayList<>();
    }

    /**
     * Creating MultiUserSession object
     */
    public MultiUserSession() {
        receivers = new ArrayList<>();
        date = new Date();
    }

    /**
     * @return learningType as String
     */
    public String getLearningType() {
        return learningType;
    }

    /**
     * @param learningType as String
     */
    public void setLearningType(String learningType) {
        this.learningType = learningType;
    }

    /**
     * @return categoryid as Integer
     */
    public Integer getCategoryid() {
        return categoryid;
    }

    /**
     * @param categoryid as Integer
     */
    public void setCategoryid(Integer categoryid) {
        this.categoryid = categoryid;
    }

    /**
     * @return categoryName as String
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * @param categoryName as String
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * @return id as int
     */
    public int getId() {
        return id;
    }

    /**
     * @param id  as int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return sessionName as String
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * @param sessionName as String
     */
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    /**
     * @return numRounds as int
     */
    public int getNumRounds() {
        return numRounds;
    }

    /**
     * Increasing number of rounds by 1
     */
    public void increaseRoundNum() {
        this.numRounds += 1;
    }

    /**
     * @return senderName (if null, then return "SYSTEM")
     */
    public String getSenderName() {
        if(senderName != null) {return senderName;}
        return "SYSTEM";
    }

    /**
     * @param senderName as String
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * @return receivers as ArrayList<Receiver>
     */
    public ArrayList<Receiver> getReceivers() {
        return receivers;
    }

    /**
     * @param receivers as ArrayList<Receiver>
     */
    public void setReceivers(ArrayList<Receiver> receivers) {
        this.receivers = receivers;
    }

    /**
     * @return date as Date
     */
    public Date getDate() {
        return date;
    }
}
