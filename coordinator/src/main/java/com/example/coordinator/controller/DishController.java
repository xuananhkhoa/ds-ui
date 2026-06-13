package com.example.coordinator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dishes")
public class DishController {
    private static final String ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 27;

    private final ObjectMapper objectMapper;
    private final Path dishesFile = Path.of("data", "dishes.json");
    private final Object lock = new Object();
    private final SecureRandom random = new SecureRandom();

    public DishController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        ensureDishesFile();
    }

    @GetMapping
    public List<DishResponse> dishes() {
        Map<String, DishRecord> dishes = readDishes();
        return dishes.entrySet().stream()
                .map(entry -> DishResponse.from(entry.getKey(), entry.getValue()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DishResponse createDish(@RequestBody DishRequest request) {
        String title = clean(firstNonBlank(request.title, request.name));
        List<String> ingredients = safeList(request.ingredients);
        String instructions = clean(firstNonBlank(request.instructions, request.cookingMethod));

        if (title.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dish name is required.");
        }

        if (ingredients.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one ingredient is required.");
        }

        if (instructions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cooking instructions are required.");
        }

        synchronized (lock) {
            Map<String, DishRecord> dishes = readDishes();
            String id = nextId(dishes);

            DishRecord dish = new DishRecord();
            dish.title = title;
            dish.ingredients = ingredients;
            dish.instructions = instructions;
            dish.picture_link = cleanToNull(request.picture_link);

            dishes.put(id, dish);
            writeDishes(dishes);

            return DishResponse.from(id, dish);
        }
    }

    private void ensureDishesFile() {
        synchronized (lock) {
            if (Files.exists(dishesFile)) {
                return;
            }

            try {
                Files.createDirectories(dishesFile.getParent());
                writeDishes(new LinkedHashMap<>());
            } catch (IOException e) {
                throw new IllegalStateException("Could not create dishes file.", e);
            }
        }
    }

    private Map<String, DishRecord> readDishes() {
        ensureDishesFile();

        synchronized (lock) {
            try {
                JsonNode root = objectMapper.readTree(dishesFile.toFile());

                if (root == null || root.isNull()) {
                    return new LinkedHashMap<>();
                }

                if (root.has("dishes") && root.get("dishes").isArray()) {
                    return migrateOldListFormat(root.get("dishes"));
                }

                return objectMapper.convertValue(root, new TypeReference<LinkedHashMap<String, DishRecord>>() {});
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read dishes file.");
            }
        }
    }

    private Map<String, DishRecord> migrateOldListFormat(JsonNode dishesNode) {
        Map<String, DishRecord> dishes = new LinkedHashMap<>();

        for (JsonNode node : dishesNode) {
            DishRecord dish = new DishRecord();
            dish.title = clean(firstNonBlank(text(node, "title"), text(node, "name")));
            dish.ingredients = safeList(objectMapper.convertValue(
                    node.path("ingredients"),
                    new TypeReference<List<String>>() {}
            ));
            dish.instructions = clean(firstNonBlank(text(node, "instructions"), text(node, "cookingMethod")));
            dish.picture_link = cleanToNull(text(node, "picture_link"));

            String id = clean(text(node, "id"));
            if (id.isEmpty() || dishes.containsKey(id)) {
                id = nextId(dishes);
            }

            if (!dish.title.isEmpty()) {
                dishes.put(id, dish);
            }
        }

        writeDishes(dishes);
        return dishes;
    }

    private void writeDishes(Map<String, DishRecord> dishes) {
        synchronized (lock) {
            try {
                objectMapper.writeValue(dishesFile.toFile(), dishes);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save dishes file.");
            }
        }
    }

    private String nextId(Map<String, DishRecord> existing) {
        String id;

        do {
            StringBuilder builder = new StringBuilder(ID_LENGTH);
            for (int i = 0; i < ID_LENGTH; i += 1) {
                builder.append(ID_CHARS.charAt(random.nextInt(ID_CHARS.length())));
            }
            id = builder.toString();
        } while (existing.containsKey(id));

        return id;
    }

    private static String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private static String firstNonBlank(String first, String second) {
        String cleanFirst = clean(first);
        return cleanFirst.isEmpty() ? second : cleanFirst;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String cleanToNull(String value) {
        String cleanValue = clean(value);
        return cleanValue.isEmpty() ? null : cleanValue;
    }

    private static List<String> safeList(List<String> value) {
        if (value == null) {
            return new ArrayList<>();
        }

        return value.stream()
                .map(DishController::clean)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    public static class DishRequest {
        public String title;
        public String name;
        public List<String> ingredients = new ArrayList<>();
        public String instructions;
        public String cookingMethod;
        public String picture_link;
    }

    public static class DishRecord {
        public String instructions;
        public List<String> ingredients = new ArrayList<>();
        public String title;
        public String picture_link;
    }

    public static class DishResponse extends DishRecord {
        public String id;
        public String name;
        public String cookingMethod;

        public static DishResponse from(String id, DishRecord dish) {
            DishResponse response = new DishResponse();
            response.id = id;
            response.instructions = dish.instructions;
            response.ingredients = safeList(dish.ingredients);
            response.title = dish.title;
            response.picture_link = dish.picture_link;
            response.name = dish.title;
            response.cookingMethod = dish.instructions;
            return response;
        }
    }
}
