package edu.neu.groupassignment.stickittoem.model;

public class User {
    private String userId;
    private String name;
    private String token;
    private int sentCount;

    public User() {
    }

    public User(String userId, String name, String token) {
        this.userId = userId;
        this.name = name;
        this.token = token;
        this.sentCount = 0;
    }

    public User(String userId, String name, String token, int sentCount) {
        this.userId = userId;
        this.name = name;
        this.token = token;
        this.sentCount = sentCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void accumulateSentCount() {
        this.sentCount += 1;
    }
}
