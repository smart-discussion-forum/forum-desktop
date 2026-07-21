package com.mindshare.quiz;

public class Quiz {
    private int id;
    private String title;
    private int durationMinutes;

    public Quiz(int id, String title, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }

    @Override
    public String toString() { return title; }
}


