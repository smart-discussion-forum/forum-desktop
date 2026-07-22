package com.mindshare.discussion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscussionMessage {
    @JsonProperty("id")
    private int id;
    @JsonProperty("sender_name")
    private String senderName;
    @JsonProperty("message")
    private String message;
    @JsonProperty("created_at")
    private String createdAt;

    public int getId() { return id; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return senderName + ": " + message;
    }
}
