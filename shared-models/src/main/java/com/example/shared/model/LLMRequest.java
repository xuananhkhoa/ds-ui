package com.example.shared.model;

public class LLMRequest {
    private String userQuery;

    public LLMRequest() {}

    public String getUserQuery() {return userQuery;}
    public void setUserQuery(String userQuery) {this.userQuery = userQuery;}
}