package com.mindshare.quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestionDraft {
    private String questionText = "";
    private final List<String> options = new ArrayList<>();
    private int correctOptionIndex = 0;
    private int marks = 1;

    public QuizQuestionDraft() {
        options.add("");
        options.add("");
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public void ensureMinimumOptions(int minimumOptions) {
        while (options.size() < minimumOptions) {
            options.add("");
        }
    }
}
