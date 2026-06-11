package com.example.llmnode.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.llmnode.service.LLMService;

import com.example.shared.model.LLMRequest;

@RestController
@RequestMapping("/llm")
public class LLMSearchController {
    private final LLMService llmService;

    public LLMSearchController(LLMService llmService) {
        this.llmService = llmService;
    }

    @PostMapping
    public String search(@RequestBody LLMRequest request) {
        return llmService.search(request);
    }
}