package com.mindshare.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizListItem {

    @JsonProperty("id")
    private int id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("start_time_display")
    private String startTimeDisplay;

    @JsonProperty("status")
    private String status;

    @JsonProperty("is_owner")
    private boolean owner;

    @JsonProperty("my_attempt_id")
    private Integer myAttemptId;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    @JsonProperty("Duration")
    private Integer legacyDurationMinutes;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getStartTimeDisplay() {
        return startTimeDisplay;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOwner() {
        return owner;
    }

    public Integer getMyAttemptId() {
        return myAttemptId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes != null ? durationMinutes : legacyDurationMinutes;
    }

    @Override
    public String toString() {
        return title + (startTimeDisplay != null ? " - " + startTimeDisplay : "");
    }
}
