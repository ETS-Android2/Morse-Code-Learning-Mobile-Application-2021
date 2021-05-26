package com.example.aibasedmorsecode.model;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private int id, wins, losses, points;


    /**
     * Create User object with three parameters
     * @param id
     * @param username
     * @param points
     */
    public User(int id, String username, int points) {
        this.id = id;
        this.username = username;
        this.points = points;
    }

    /**
     * Create User object with two parameters
     * @param username
     * @param points
     */
    public User(String username, int points) {
        this.username = username;
        this.points = points;
    }

    /**
     * Create User object with one parameter
     * @param username
     */
    public User(String username) {
        this.username = username;
    }

    /**
     * @return username as String
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return wins as int
     */
    public int getWins() {
        return wins;
    }

    /**
     * Increase wins by 1
     */
    public void increaseWins() {
        this.wins++;
    }

    /**
     * @return losses as int
     */
    public int getLosses() {
        return losses;
    }

    /**
     * Increase losses by 1
     */
    public void increaseLosses() {
        this.losses++;
    }

    /**
     * @return id int
     */
    public int getId() {
        return id;
    }

    /**
     * @param id as int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return points as int
     */
    public int getPoints() {
        return points;
    }

    /**
     * @param points as int
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * @return "Play More to See Rank" if wins + losses < 5, or ("PLATINUM", or "BRONZE", or "SILVER") based on num. wins and losses
     */
    public String getCurrentRank() {
        int result = wins - losses; // bronze, silver, platinum

        if(wins + losses < 5) { return "Play More to See Rank";}
        if(result > 5) { return "PLATINUM";}
        if(result < -5) { return "BRONZE";}
        return "SILVER"; // between 5 and -5
    }

    /**
     * increase points by 5
     */
    public void increasePoints() {
        points += 5;
    }

    @Override
    public String toString() {
        return getUsername();
    }
}
