package com.example.llmnode.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

import com.example.shared.model.LLMRequest;
import com.example.shared.model.RecipeFilter;
import com.example.shared.model.RecipeQuery;

@Service
public class LLMService {
    private ChatModel model;
    private UserQueryParser parser;

    public LLMService() {
        this.model = GoogleAiGeminiChatModel.builder()
        //System.getenv("GOOGLE_AI_GEMINI_API_KEY")
        .apiKey("keyhere")
        .modelName("gemini-2.5-flash")
        .logRequestsAndResponses(true)
        .build();

        this.parser = AiServices.create(UserQueryParser.class, this.model);
    }

    interface UserQueryParser { //maybe put in shared model
        @UserMessage("""
            You are a recipe search query parser. Extract search intent and filters from the user query.
            Return ONLY valid JSON. No explanation, no markdown.

            Schema:
            {
                "search_type": "ingredients" | "recipe_name" | "both",
                "keywords": ["list of ingredient or recipe name keywords for vector search"],
                "filters": {
                    "meal_type": null or string,
                    "cuisine": null or string,
                    "cooking_method": null or list,
                    "main_protein": null or string,
                    "diet_flags": null or list,
                    "max_ingredients": null or number,
                    "max_cook_time": null or number,
                    "has_picture": null or boolean
                }
            }

            Now parse this query:
            User: "{user_query}"
            """)
        RecipeQuery parse(@V("user_query") String userQuery);
    }
    
    public String search(LLMRequest request) {
        System.out.println("Received " + request.getUserQuery());

        RecipeQuery test = parser.parse(request.getUserQuery());
        
        System.out.println(test.getKeywords());
        
        return test.getSearch_type();
    }
}