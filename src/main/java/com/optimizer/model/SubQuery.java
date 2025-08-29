package com.optimizer.model;

import java.util.List;

/**
 * Модель подзапроса после разбиения
 */
public class SubQuery {
    private String id;
    private String sql;
    private double cost;
    private List<String> dependencies;
    private boolean isTemporaryTable;
    private String temporaryTableName;
    private String description;

    public SubQuery() {
    }

    public SubQuery(String id, String sql, double cost, List<String> dependencies) {
        this.id = id;
        this.sql = sql;
        this.cost = cost;
        this.dependencies = dependencies;
        this.isTemporaryTable = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean isTemporaryTable() {
        return isTemporaryTable;
    }

    public void setTemporaryTable(boolean temporaryTable) {
        isTemporaryTable = temporaryTable;
    }

    public String getTemporaryTableName() {
        return temporaryTableName;
    }

    public void setTemporaryTableName(String temporaryTableName) {
        this.temporaryTableName = temporaryTableName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

