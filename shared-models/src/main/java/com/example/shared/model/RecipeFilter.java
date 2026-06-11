package com.example.shared.model;

import java.util.List;

public class RecipeFilter {
    private String meal_type;
    private String cuisine;
    private List<String> cooking_method;
    private String main_protein;
    private List<String> diet_flags;
    private Integer max_ingredients;
    private Integer max_cook_time;
    private Boolean has_picture;

    public RecipeFilter() {}

    public String getMeal_type() {return meal_type;}
    public void setMeal_type(String meal_type) {this.meal_type = meal_type;}

    public String getCuisine() {return cuisine;}
    public void setCuisine(String cuisine) {this.cuisine = cuisine;}

    public List<String> getCooking_method() {return cooking_method;}
    public void setCooking_method(List<String> cooking_method) {this.cooking_method = cooking_method;}

    public String getMain_protein() {return main_protein;}
    public void setMain_protein(String main_protein) {this.main_protein = main_protein;}

    public List<String> getDiet_flags() {return diet_flags;}
    public void setDiet_flags(List<String> diet_flags) {this.diet_flags = diet_flags;}

    public Integer getMax_ingredients() {return max_ingredients;}
    public void setMax_ingredients(Integer max_ingredients) {this.max_ingredients = max_ingredients;}

    public Integer getMax_cook_time() {return max_cook_time;}
    public void setMax_cook_time(Integer max_cook_time) {this.max_cook_time = max_cook_time;}

    public Boolean getHas_picture() {return has_picture;}
    public void setHas_picture(Boolean has_picture) {this.has_picture = has_picture;}
}