package com.optimizer.model;

/**
 * Модель запроса на оптимизацию SQL запроса
 */
public class OptimizationRequest {
    private String sqlQuery;
    private String metadata;
    private String statistics;
    private double costThreshold;

    public OptimizationRequest() {
    }

    public OptimizationRequest(String sqlQuery, String metadata, String statistics, double costThreshold) {
        this.sqlQuery = sqlQuery;
        this.metadata = metadata;
        this.statistics = statistics;
        this.costThreshold = costThreshold;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getStatistics() {
        return statistics;
    }

    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    public double getCostThreshold() {
        return costThreshold;
    }

    public void setCostThreshold(double costThreshold) {
        this.costThreshold = costThreshold;
    }
}

