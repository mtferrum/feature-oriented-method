package com.optimizer.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimizer.util.JsonUtils;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Парсер метаданных хранилища
 */
public class MetadataParser {
    private static final Logger logger = LoggerFactory.getLogger(MetadataParser.class);
    private final ObjectMapper objectMapper;

    public MetadataParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Создает схему Calcite на основе метаданных в JSON формате
     */
    public SchemaPlus createSchema(String metadataJson) {
        try {
            JsonNode metadata = objectMapper.readTree(metadataJson);
            CalciteSchema calciteSchema = CalciteSchema.createRootSchema(false);
            SchemaPlus schemaPlus = calciteSchema.plus();
            
            // Обрабатываем таблицы
            if (metadata.has("tables")) {
                JsonNode tables = metadata.get("tables");
                for (JsonNode tableNode : tables) {
                    String tableName = tableNode.get("name").asText();
                    Table table = createTable(tableNode);
                    schemaPlus.add(tableName, table);
                }
            }
            
            // Обрабатываем представления
            if (metadata.has("views")) {
                JsonNode views = metadata.get("views");
                for (JsonNode viewNode : views) {
                    String viewName = viewNode.get("name").asText();
                    String viewSql = viewNode.get("sql").asText();
                    Table view = createView(viewName, viewSql);
                    schemaPlus.add(viewName, view);
                }
            }
            
            logger.info("Схема создана успешно");
            return schemaPlus;
            
        } catch (Exception e) {
            logger.error("Ошибка создания схемы из метаданных", e);
            throw new RuntimeException("Не удалось создать схему из метаданных", e);
        }
    }

    /**
     * Создает таблицу на основе метаданных
     */
    private Table createTable(JsonNode tableNode) {
        return new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                return createRowType(tableNode.get("columns"), typeFactory);
            }
        };
    }

    /**
     * Создает представление
     */
    private Table createView(String viewName, String viewSql) {
        // Для простоты создаем таблицу с базовой структурой
        // В реальной реализации здесь должна быть логика для парсинга SQL представления
        return new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                // Создаем базовую структуру для представления
                return typeFactory.createStructType(
                    java.util.Arrays.asList(
                        typeFactory.createSqlType(SqlTypeName.VARCHAR)
                    ),
                    java.util.Arrays.asList("column1")
                );
            }
        };
    }

    /**
     * Создает тип строки на основе колонок
     */
    private RelDataType createRowType(JsonNode columns, RelDataTypeFactory typeFactory) {
        java.util.List<RelDataType> types = new java.util.ArrayList<>();
        java.util.List<String> names = new java.util.ArrayList<>();
        
        for (JsonNode column : columns) {
            String columnName = column.get("name").asText();
            String columnType = column.get("type").asText();
            
            RelDataType sqlType = getSqlType(columnType, typeFactory);
            types.add(sqlType);
            names.add(columnName);
        }
        
        return typeFactory.createStructType(types, names);
    }

    /**
     * Преобразует строковый тип в RelDataType
     */
    private RelDataType getSqlType(String typeName, RelDataTypeFactory typeFactory) {
        switch (typeName.toLowerCase()) {
            case "integer":
            case "int":
                return typeFactory.createSqlType(SqlTypeName.INTEGER);
            case "bigint":
            case "long":
                return typeFactory.createSqlType(SqlTypeName.BIGINT);
            case "double":
            case "float":
                return typeFactory.createSqlType(SqlTypeName.DOUBLE);
            case "decimal":
                return typeFactory.createSqlType(SqlTypeName.DECIMAL);
            case "boolean":
            case "bool":
                return typeFactory.createSqlType(SqlTypeName.BOOLEAN);
            case "date":
                return typeFactory.createSqlType(SqlTypeName.DATE);
            case "timestamp":
                return typeFactory.createSqlType(SqlTypeName.TIMESTAMP);
            case "varchar":
            case "string":
            case "text":
            default:
                return typeFactory.createSqlType(SqlTypeName.VARCHAR);
        }
    }
}
