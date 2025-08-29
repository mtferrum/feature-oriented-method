package com.optimizer.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.calcite.schema.SchemaPlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Парсер статистики таблиц
 */
public class StatisticsParser {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsParser.class);
    private final ObjectMapper objectMapper;

    public StatisticsParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Загружает статистику в схему
     */
    public void loadStatistics(SchemaPlus schema, String statisticsJson) {
        try {
            JsonNode statistics = objectMapper.readTree(statisticsJson);
            
            if (statistics.has("tables")) {
                JsonNode tables = statistics.get("tables");
                for (JsonNode tableStats : tables) {
                    String tableName = tableStats.get("name").asText();
                    loadTableStatistics(schema, tableName, tableStats);
                }
            }
            
            logger.info("Статистика загружена успешно");
            
        } catch (Exception e) {
            logger.error("Ошибка загрузки статистики", e);
            throw new RuntimeException("Не удалось загрузить статистику", e);
        }
    }

    /**
     * Загружает статистику для конкретной таблицы
     */
    private void loadTableStatistics(SchemaPlus schema, String tableName, JsonNode tableStats) {
        // В реальной реализации здесь должна быть логика для установки статистики
        // в метаданные таблицы Calcite
        logger.debug("Загружаем статистику для таблицы: {}", tableName);
        
        if (tableStats.has("rowCount")) {
            long rowCount = tableStats.get("rowCount").asLong();
            logger.debug("Количество строк в таблице {}: {}", tableName, rowCount);
        }
        
        if (tableStats.has("columnStats")) {
            JsonNode columnStats = tableStats.get("columnStats");
            for (JsonNode columnStat : columnStats) {
                String columnName = columnStat.get("name").asText();
                if (columnStat.has("distinctValues")) {
                    long distinctValues = columnStat.get("distinctValues").asLong();
                    logger.debug("Количество уникальных значений в колонке {}.{}: {}", 
                        tableName, columnName, distinctValues);
                }
            }
        }
    }
}

