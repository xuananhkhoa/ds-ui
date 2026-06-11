package com.example.coordinator.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.coordinator.service.CoordinatorService;
import com.example.coordinator.service.ProcessingService;
import com.example.coordinator.service.RequestStorage;

import com.example.shared.model.LLMRequest;
import com.example.shared.model.UserRequest;

@RestController
@RequestMapping("/")
public class SearchController {

    private final CoordinatorService coordinatorService;
    private final ProcessingService processingService;
    private final RequestStorage storage;

    public SearchController(CoordinatorService coordinatorService, 
            ProcessingService processingService, RequestStorage storage) {
        this.coordinatorService = coordinatorService;
        this.processingService = processingService;
        this.storage = storage;
    }

    @PostMapping("/search")
    public String search(@RequestBody LLMRequest request) {
        return coordinatorService.search(request);
    }

    @GetMapping("/get")
    public String get(@RequestParam String id) {
        return coordinatorService.get(id);
    }

    @PostMapping("/copy") 
    public boolean copy(@RequestBody UserRequest request) { 
        storage.storeRequest(request.getId(), request);
        return true;
    }
    // returns false if leader??
}