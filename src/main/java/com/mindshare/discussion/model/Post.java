package com.mindshare.discussion.model;

//Represents one post/Reply within a topic
public class Post {

    private int id;
    private String authorName;
    private String content;
    private String createdAt;

    public Post(int id, String authorName, String content, String createdAt) {
        this.id = id;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "[" + createdAt + "] " + authorName + ": " + content;
    }

}
