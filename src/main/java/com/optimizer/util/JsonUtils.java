package com.optimizer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.optimizer.model.OptimizationRequest;
import com.optimizer.model.OptimizationResult;

/**
 * Утилиты для работы с JSON
 */
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Преобразует объект в JSON строку
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации в JSON", e);
        }
    }

    /**
     * Преобразует JSON строку в объект
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации из JSON", e);
        }
    }

    /**
     * Преобразует OptimizationRequest в JSON
     */
    public static String toJson(OptimizationRequest request) {
        return toJson((Object) request);
    }

    /**
     * Преобразует OptimizationResult в JSON
     */
    public static String toJson(OptimizationResult result) {
        return toJson((Object) result);
    }
}

