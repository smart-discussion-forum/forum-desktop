package com.mindshare.database;
//This represents one locally-cached message awaiting sync to the server

public class CachedMessage {

    private int senderId;
    private int groupId;
    private String content;
    private String sentAt;
    private boolean isSynced;

    public CachedMessage(int senderId, int groupId, String content, String sentAt) {
        this.senderId = senderId;
        this.groupId = groupId;
        this.content = content;
        this.sentAt = sentAt;
        this.isSynced= false;
    }

    public int getSenderId() { return senderId;}
    public int getGroupId() { return groupId;}
    public String getContent() { return content;}
    public String getSentAt() { return sentAt;}
    public boolean isSynced() {return isSynced;}
}
