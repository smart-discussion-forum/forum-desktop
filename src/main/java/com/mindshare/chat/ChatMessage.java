package com.mindshare.chat;

public class ChatMessage {
    private final String sender;
    private final String content;
    private final boolean mine;

    public ChatMessage(String sender, String content, boolean mine) {
        this.sender = sender;
        this.content = content;
        this.mine = mine;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public boolean isMine() { return mine; }
}
