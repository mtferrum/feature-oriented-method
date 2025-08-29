package com.optimizer.splitter;

import com.optimizer.model.SubQuery;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.metadata.RelMetadataProvider;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Класс для разбиения SQL запросов на подзапросы с использованием cost-based оптимизации
 */
public class QuerySplitter {
    private static final Logger logger = LoggerFactory.getLogger(QuerySplitter.class);
    private int tempTableCounter = 0;

    /**
     * Разбивает оптимизированный запрос на подзапросы
     */
    public List<SubQuery> splitQuery(RelNode optimizedNode, double costThreshold, FrameworkConfig config) {
        List<SubQuery> subQueries = new ArrayList<>();
        
        try {
            // Упрощенная логика: создаем один подзапрос
            // В реальной реализации здесь должна быть логика разбиения на основе стоимости
            logger.info("Создаем упрощенный подзапрос");
            
            SubQuery singleQuery = createSubQuery(optimizedNode, config, "Q1", Collections.emptyList());
            subQueries.add(singleQuery);
            
            logger.info("Создан 1 подзапрос");
            
        } catch (Exception e) {
            logger.error("Ошибка разбиения запроса", e);
            // В случае ошибки возвращаем исходный запрос как один подзапрос
            SubQuery fallbackQuery = createSubQuery(optimizedNode, config, "Q1", Collections.emptyList());
            subQueries.add(fallbackQuery);
        }
        
        return subQueries;
    }

    /**
     * Разбивает RelNode на части на основе стоимости
     */
    private List<RelNode> splitRelNode(RelNode node, double costThreshold, RelMetadataQuery metadataQuery) {
        List<RelNode> parts = new ArrayList<>();
        
        // Проверяем, можно ли разбить узел
        if (canSplit(node)) {
            List<RelNode> children = getChildren(node);
            
            for (RelNode child : children) {
                double childCost = metadataQuery.getCumulativeCost(child).getRows();
                
                if (childCost > costThreshold) {
                    // Рекурсивно разбиваем дочерний узел
                    parts.addAll(splitRelNode(child, costThreshold, metadataQuery));
                } else {
                    parts.add(child);
                }
            }
        } else {
            // Узел нельзя разбить, добавляем как есть
            parts.add(node);
        }
        
        return parts;
    }

    /**
     * Проверяет, можно ли разбить узел
     */
    private boolean canSplit(RelNode node) {
        return node instanceof Join || 
               node instanceof Project || 
               node instanceof Filter || 
               node instanceof Aggregate;
    }

    /**
     * Получает дочерние узлы
     */
    private List<RelNode> getChildren(RelNode node) {
        List<RelNode> children = new ArrayList<>();
        
        if (node instanceof Join) {
            Join join = (Join) node;
            children.add(join.getLeft());
            children.add(join.getRight());
        } else {
            // Для других типов узлов берем всех дочерних
            for (RelNode input : node.getInputs()) {
                children.add(input);
            }
        }
        
        return children;
    }

    /**
     * Создает подзапрос из RelNode
     */
    private SubQuery createSubQuery(RelNode relNode, FrameworkConfig config, String queryId, List<String> dependencies) {
        try {
            // Преобразуем RelNode обратно в SQL
            String sql = relNodeToSql(relNode, config);
            
            // Вычисляем стоимость
            RelMetadataQuery metadataQuery = RelMetadataQuery.instance();
            double cost = metadataQuery.getCumulativeCost(relNode).getRows();
            
            // Определяем, нужна ли временная таблица
            boolean isTemporaryTable = dependencies.size() > 0;
            String tempTableName = isTemporaryTable ? "temp_" + (++tempTableCounter) : null;
            
            SubQuery subQuery = new SubQuery(queryId, sql, cost, dependencies);
            subQuery.setTemporaryTable(isTemporaryTable);
            subQuery.setTemporaryTableName(tempTableName);
            subQuery.setDescription("Подзапрос " + queryId + " со стоимостью " + cost);
            
            return subQuery;
            
        } catch (Exception e) {
            logger.error("Ошибка создания подзапроса", e);
            // Возвращаем базовый подзапрос в случае ошибки
            return new SubQuery(queryId, "SELECT 1", 1.0, dependencies);
        }
    }

    /**
     * Преобразует RelNode в SQL строку
     */
    private String relNodeToSql(RelNode relNode, FrameworkConfig config) {
        try {
            // Создаем RelBuilder для преобразования
            RelBuilder relBuilder = RelBuilder.create(config);
            
            // Преобразуем в SQL (упрощенная версия)
            // В реальной реализации здесь должен быть более сложный код
            return "SELECT * FROM (" + relNode.toString() + ")";
            
        } catch (Exception e) {
            logger.error("Ошибка преобразования RelNode в SQL", e);
            return "SELECT 1"; // Fallback
        }
    }

    /**
     * Определяет зависимости между подзапросами
     */
    private List<String> getDependencies(RelNode node, int currentIndex, int totalParts) {
        List<String> dependencies = new ArrayList<>();
        
        // Простая логика: если это не первый подзапрос, 
        // то он зависит от предыдущего
        if (currentIndex > 0) {
            dependencies.add("Q" + currentIndex);
        }
        
        return dependencies;
    }
}
