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

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_id")
    private Integer groupId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("is_owner")
    private boolean owner;

    @JsonProperty("announced")
    private boolean announced;

    @JsonProperty("announced_at_display")
    private String announcedAtDisplay;

    @JsonProperty("can_announce")
    private boolean canAnnounce;

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

    public String getGroupName() {
        return groupName;
    }

    public Integer getGroupId() { return groupId; }

    public String getStatus() {
        return status;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isAnnounced() {
        return announced;
    }

    public String getAnnouncedAtDisplay() {
        return announcedAtDisplay;
    }

    public boolean canAnnounce() {
        return canAnnounce;
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
