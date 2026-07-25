package com.mindshare.discussion.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
//Representing one discussion topic withing a group
public class Topic {

    private int id;
    private String title;
    private String category;

    @JsonProperty("posts_count")
    private int postsCount;

    @JsonProperty("recent_posts_count")
    private int recentPostsCount;


    private Creator creator;

    @JsonProperty("latest_post")
    private LatestPost latestPost;


    public Topic() {
    } // needed for Jackson

    public Topic(int id, String title, String category) {
        this.id = id;
        this.title = title;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public int getRecentPostsCount() {
        return recentPostsCount;
    }

    public void setRecentPostsCount(int recentPostsCount) {
        this.recentPostsCount = recentPostsCount;
    }


    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public LatestPost getLatestPost() {
        return latestPost;
    }

    public void setLatestPost(LatestPost latestPost) {
        this.latestPost = latestPost;
    }

    @Override
    public String toString() {
        String cat = category != null ? category : "Uncategorized";
        return title + "  [" + cat + "] (" + postsCount + " posts)";
    }

    // Small nested class to hold the {"id":..,"name":..} creator object
    public static class Creator {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    // Holds the latest post summary: {"content":..,"author":..}
    public static class LatestPost {
        private String content;
        private String author;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }
}

