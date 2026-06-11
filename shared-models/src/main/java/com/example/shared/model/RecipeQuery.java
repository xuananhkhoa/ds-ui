package com.example.shared.model;

import java.util.List;

public class RecipeQuery {
    //@Description("List of ingredient or recipe name keywords for vector search")
    public String search_type;
    public List<String> keywords;
    public RecipeFilter filters;

    public RecipeQuery() {}

    public String getSearch_type() {return search_type;}
    public void setSearch_type(String search_type) {this.search_type = search_type;}

    public List<String> getKeywords() {return keywords;}
    public void setKeywords(List<String> keywords) {this.keywords = keywords;}

    public RecipeFilter getFilters() {return filters;}
    public void setFilters(RecipeFilter filters) {this.filters = filters;}
}