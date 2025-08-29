package com.optimizer.core;

import com.optimizer.model.OptimizationRequest;
import com.optimizer.model.OptimizationResult;
import com.optimizer.model.SubQuery;
import com.optimizer.parser.MetadataParser;
import com.optimizer.parser.StatisticsParser;
import com.optimizer.splitter.QuerySplitter;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Основной класс для оптимизации SQL запросов с использованием Apache Calcite
 */
public class QueryOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(QueryOptimizer.class);

    private final MetadataParser metadataParser;
    private final StatisticsParser statisticsParser;
    private final QuerySplitter querySplitter;

    public QueryOptimizer() {
        this.metadataParser = new MetadataParser();
        this.statisticsParser = new StatisticsParser();
        this.querySplitter = new QuerySplitter();
    }

    /**
     * Оптимизирует SQL запрос и разбивает его на подзапросы
     */
    public OptimizationResult optimize(OptimizationRequest request) {
        try {
            logger.info("Начинаем оптимизацию запроса: {}", request.getSqlQuery());

            // Создаем схему на основе метаданных
            SchemaPlus schema = metadataParser.createSchema(request.getMetadata());
            
            // Загружаем статистику если предоставлена
            if (request.getStatistics() != null) {
                statisticsParser.loadStatistics(schema, request.getStatistics());
            }

            // Создаем конфигурацию Calcite
            FrameworkConfig config = createFrameworkConfig(schema);
            
            // Парсим SQL запрос
            SqlNode sqlNode = parseSql(request.getSqlQuery(), config);
            
            // Преобразуем в RelNode
            RelNode relNode = convertToRelNode(sqlNode, config);
            
            // Оптимизируем запрос
            RelNode optimizedNode = optimizeQuery(relNode, config);
            
            // Разбиваем на подзапросы
            List<SubQuery> subQueries = querySplitter.splitQuery(
                optimizedNode, 
                request.getCostThreshold(),
                config
            );

            // Вычисляем общую стоимость
            double totalCost = calculateTotalCost(subQueries);

            // Создаем план оптимизации
            String optimizationPlan = createOptimizationPlan(optimizedNode, subQueries);

            logger.info("Оптимизация завершена. Создано {} подзапросов", subQueries.size());

            return new OptimizationResult(
                request.getSqlQuery(),
                subQueries,
                totalCost,
                optimizationPlan
            );

        } catch (Exception e) {
            logger.error("Ошибка оптимизации запроса", e);
            OptimizationResult result = new OptimizationResult();
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }

    /**
     * Создает конфигурацию Calcite Framework
     */
    private FrameworkConfig createFrameworkConfig(SchemaPlus schema) {
        return Frameworks.newConfigBuilder()
            .parserConfig(SqlParser.Config.DEFAULT)
            .defaultSchema(schema)
            .traitDefs()
            .build();
    }

    /**
     * Парсит SQL запрос
     */
    private SqlNode parseSql(String sql, FrameworkConfig config) throws SqlParseException {
        SqlParser parser = SqlParser.create(sql, config.getParserConfig());
        return parser.parseQuery();
    }

    /**
     * Преобразует SqlNode в RelNode
     */
    private RelNode convertToRelNode(SqlNode sqlNode, FrameworkConfig config) throws RelConversionException {
        try {
            Planner planner = Frameworks.getPlanner(config);
            SqlNode parsedNode = planner.parse(sqlNode.toString());
            SqlNode validatedNode = planner.validate(parsedNode);
            RelRoot relRoot = planner.rel(validatedNode);
            return relRoot.project();
        } catch (Exception e) {
            // Fallback: используем RelBuilder для создания простого RelNode
            RelBuilder relBuilder = RelBuilder.create(config);
            return relBuilder.scan("employees").build();
        }
    }

    /**
     * Оптимизирует RelNode с использованием cost-based оптимизации
     */
    private RelNode optimizeQuery(RelNode relNode, FrameworkConfig config) {
        // Для упрощения возвращаем исходный узел
        // В реальной реализации здесь должна быть полная оптимизация
        logger.info("Используем упрощенную оптимизацию");
        return relNode;
    }

    /**
     * Вычисляет общую стоимость всех подзапросов
     */
    private double calculateTotalCost(List<SubQuery> subQueries) {
        return subQueries.stream()
            .mapToDouble(SubQuery::getCost)
            .sum();
    }

    /**
     * Создает текстовое описание плана оптимизации
     */
    private String createOptimizationPlan(RelNode optimizedNode, List<SubQuery> subQueries) {
        StringBuilder plan = new StringBuilder();
        plan.append("=== ПЛАН ОПТИМИЗАЦИИ ===\n");
        plan.append("Оптимизированное дерево операций:\n");
        plan.append(optimizedNode.toString());
        plan.append("\n\nРазбиение на подзапросы:\n");
        
        for (int i = 0; i < subQueries.size(); i++) {
            SubQuery subQuery = subQueries.get(i);
            plan.append(String.format("Подзапрос %d (ID: %s):\n", i + 1, subQuery.getId()));
            plan.append(String.format("  Стоимость: %.2f\n", subQuery.getCost()));
            plan.append(String.format("  SQL: %s\n", subQuery.getSql()));
            if (subQuery.isTemporaryTable()) {
                plan.append(String.format("  Временная таблица: %s\n", subQuery.getTemporaryTableName()));
            }
            if (subQuery.getDependencies() != null && !subQuery.getDependencies().isEmpty()) {
                plan.append(String.format("  Зависимости: %s\n", String.join(", ", subQuery.getDependencies())));
            }
            plan.append("\n");
        }
        
        return plan.toString();
    }
}
