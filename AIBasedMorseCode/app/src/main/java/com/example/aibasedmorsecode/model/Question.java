package com.example.aibasedmorsecode.model;

public class Question {
    private int id;
    private String message;
    private Integer categoryID;

    /**
     * Create Question object with three parameters
     * @param id
     * @param message
     * @param categoryID
     */
    public Question(int id, String message, Integer categoryID) {
        this.id = id;
        this.message = message;
        this.categoryID = categoryID;
    }

    /**
     * Create Question object with two parameters
     * @param message
     * @param categoryID
     */
    public Question(String message, Integer categoryID) {
        this.message = message;
        this.categoryID = categoryID;
    }

    /**
     * @return id as int
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
     * @return message as String
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return categoryID as Integer
     */
    public Integer getCategoryID() {
        return categoryID;
    }
}
