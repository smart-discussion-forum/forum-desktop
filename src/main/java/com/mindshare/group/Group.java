package com.mindshare.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    private int id;
    private String name;
    private String description;

    @JsonProperty("members_count")
    private Integer membersCount;

    private Pivot pivot;

    //No-args constructor needed by the JSON library to build the object first, then fill in fields one at a time using the setters.
    public Group() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMembersCount() { return membersCount; }
    public void setMembersCount(Integer membersCount) { this.membersCount = membersCount; }

    public Pivot getPivot() { return pivot; }
    public void setPivot(Pivot pivot) { this.pivot = pivot; }

    public boolean isMember() { return pivot != null; }
    @Override
    public String toString() {
        return name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pivot {
        private String role;

        @JsonProperty("joined_at")
        private String joinedAt;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getJoinedAt() { return joinedAt; }
        public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
    }
}

