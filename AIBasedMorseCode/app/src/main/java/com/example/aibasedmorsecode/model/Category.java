package com.example.aibasedmorsecode.model;

import java.util.ArrayList;

public class Category {
    private int id;
    private String categoryName;
    private ArrayList<Question> questions = new ArrayList();

    /**
     * To create Category object with two parameters: (id, categoryName)
     * @param id
     * @param categoryName
     */
    public Category(int id, String categoryName) {
        this.id = id;
        this.categoryName = categoryName;
    }

    /**
     * To create Category object with one parameter: (categoryName)
     * @param categoryName
     */
    public Category(String categoryName) {
        this.categoryName = categoryName;
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
     * @return categoryName as String
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * @return ArrayList of questions
     */
    public ArrayList<Question> getQuestions() {
        return questions;
    }

    /**
     * @param questions as ArrayList<Question>
     */
    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }
}
