package com.mindshare.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizQuestion {

    @JsonProperty("Question_id")
    private int questionId;

    @JsonProperty("Question")
    private String question;

    @JsonProperty("Options")
    private String optionsJson; // raw JSON string, e.g. ["A","B","C"]

    @JsonProperty("Correct_answer")
    private String correctAnswer;

    @JsonProperty("Marks")
    private int marks;

    public QuizQuestion() {}

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    //parsing JSON string for options into a real array
    public String[] getOptions() {
        try {
            return new ObjectMapper().readValue(optionsJson, String[].class);
        } catch (Exception e) {
            return new String[0];
        }
    }
}