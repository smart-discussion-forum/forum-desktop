package com.mindshare.group;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    private int id;
    private String name;
    private String description;

    //No-args constructor needed by the JSON library to build the object first, then fill in fields one at a time using the setters.
    public Group() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return name;
    }
}

