package com.example.shared.model;

import java.util.List;

public class UserRequest {
    private String id;
    private String state;
    private String userQuery;
    private String queries_for_db;
    private String list_of_result;
    private String result;

    public UserRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }

    public String getQueries_for_db() { return queries_for_db; }
    public void setQueries_for_db(String queries_for_db) { this.queries_for_db = queries_for_db; }

    public String getList_of_result() { return list_of_result; }
    public void setList_of_result(String list_of_result) { this.list_of_result = list_of_result; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}