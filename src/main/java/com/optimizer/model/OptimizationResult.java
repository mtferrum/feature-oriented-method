package com.optimizer.model;

import java.util.List;

/**
 * Модель результата оптимизации SQL запроса
 */
public class OptimizationResult {
    private String originalQuery;
    private List<SubQuery> subQueries;
    private double totalCost;
    private String optimizationPlan;
    private boolean success;
    private String errorMessage;

    public OptimizationResult() {
    }

    public OptimizationResult(String originalQuery, List<SubQuery> subQueries, double totalCost, String optimizationPlan) {
        this.originalQuery = originalQuery;
        this.subQueries = subQueries;
        this.totalCost = totalCost;
        this.optimizationPlan = optimizationPlan;
        this.success = true;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public List<SubQuery> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(List<SubQuery> subQueries) {
        this.subQueries = subQueries;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getOptimizationPlan() {
        return optimizationPlan;
    }

    public void setOptimizationPlan(String optimizationPlan) {
        this.optimizationPlan = optimizationPlan;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }
}

