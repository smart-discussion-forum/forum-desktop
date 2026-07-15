package com.mindshare.discussion.model;

//Representing one discussion topic withing a group
public class Topic {

        private int id;
        private String title;
        private String category;

        public Topic(int id, String title, String category) {
            this.id = id;
            this.title = title;
            this.category = category;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        @Override
        public String toString() {
            return title + "  [" + category + "]"; // shown directly in the ListView
        }
    }

