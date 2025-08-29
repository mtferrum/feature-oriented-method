package com.optimizer;

import com.optimizer.core.QueryOptimizer;
import com.optimizer.model.OptimizationRequest;
import com.optimizer.model.OptimizationResult;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Тесты для проверки продвинутых SQL возможностей Apache Calcite
 */
public class AdvancedSqlFeaturesTest {
    
    private QueryOptimizer optimizer;
    private String metadataJson;
    private String statisticsJson;
    
    @Before
    public void setUp() {
        optimizer = new QueryOptimizer();
        
        // Расширенные метаданные для тестирования продвинутых возможностей
        metadataJson = "{\n" +
            "  \"tables\": [\n" +
            "    {\n" +
            "      \"name\": \"employees\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"name\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"department_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"salary\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"hire_date\", \"type\": \"date\"},\n" +
            "        {\"name\": \"manager_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"is_active\", \"type\": \"boolean\"},\n" +
            "        {\"name\": \"skills\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"rating\", \"type\": \"decimal\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"departments\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"name\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"location\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"budget\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"manager_id\", \"type\": \"integer\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"projects\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"name\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"department_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"start_date\", \"type\": \"date\"},\n" +
            "        {\"name\": \"end_date\", \"type\": \"date\"},\n" +
            "        {\"name\": \"budget\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"status\", \"type\": \"varchar\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"employee_projects\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"employee_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"project_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"role\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"hours_worked\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"start_date\", \"type\": \"date\"},\n" +
            "        {\"name\": \"end_date\", \"type\": \"date\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"skills\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"name\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"category\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"level\", \"type\": \"integer\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"employee_skills\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"employee_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"skill_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"proficiency\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"certified\", \"type\": \"boolean\"}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        // Расширенная статистика
        statisticsJson = "{\n" +
            "  \"tables\": [\n" +
            "    {\n" +
            "      \"name\": \"employees\",\n" +
            "      \"rowCount\": 1000,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 1000, \"nullCount\": 0},\n" +
            "        {\"name\": \"name\", \"distinctValues\": 950, \"nullCount\": 0},\n" +
            "        {\"name\": \"department_id\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"salary\", \"distinctValues\": 500, \"nullCount\": 0},\n" +
            "        {\"name\": \"hire_date\", \"distinctValues\": 365, \"nullCount\": 0},\n" +
            "        {\"name\": \"manager_id\", \"distinctValues\": 50, \"nullCount\": 100},\n" +
            "        {\"name\": \"is_active\", \"distinctValues\": 2, \"nullCount\": 0},\n" +
            "        {\"name\": \"skills\", \"distinctValues\": 200, \"nullCount\": 0},\n" +
            "        {\"name\": \"rating\", \"distinctValues\": 50, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"departments\",\n" +
            "      \"rowCount\": 10,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"name\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"location\", \"distinctValues\": 5, \"nullCount\": 0},\n" +
            "        {\"name\": \"budget\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"manager_id\", \"distinctValues\": 10, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"projects\",\n" +
            "      \"rowCount\": 100,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 100, \"nullCount\": 0},\n" +
            "        {\"name\": \"name\", \"distinctValues\": 100, \"nullCount\": 0},\n" +
            "        {\"name\": \"department_id\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"start_date\", \"distinctValues\": 365, \"nullCount\": 0},\n" +
            "        {\"name\": \"end_date\", \"distinctValues\": 365, \"nullCount\": 0},\n" +
            "        {\"name\": \"budget\", \"distinctValues\": 100, \"nullCount\": 0},\n" +
            "        {\"name\": \"status\", \"distinctValues\": 4, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"employee_projects\",\n" +
            "      \"rowCount\": 2000,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 2000, \"nullCount\": 0},\n" +
            "        {\"name\": \"employee_id\", \"distinctValues\": 800, \"nullCount\": 0},\n" +
            "        {\"name\": \"project_id\", \"distinctValues\": 100, \"nullCount\": 0},\n" +
            "        {\"name\": \"role\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"hours_worked\", \"distinctValues\": 500, \"nullCount\": 0},\n" +
            "        {\"name\": \"start_date\", \"distinctValues\": 365, \"nullCount\": 0},\n" +
            "        {\"name\": \"end_date\", \"distinctValues\": 365, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"skills\",\n" +
            "      \"rowCount\": 50,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 50, \"nullCount\": 0},\n" +
            "        {\"name\": \"name\", \"distinctValues\": 50, \"nullCount\": 0},\n" +
            "        {\"name\": \"category\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"level\", \"distinctValues\": 5, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"employee_skills\",\n" +
            "      \"rowCount\": 3000,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 3000, \"nullCount\": 0},\n" +
            "        {\"name\": \"employee_id\", \"distinctValues\": 800, \"nullCount\": 0},\n" +
            "        {\"name\": \"skill_id\", \"distinctValues\": 50, \"nullCount\": 0},\n" +
            "        {\"name\": \"proficiency\", \"distinctValues\": 5, \"nullCount\": 0},\n" +
            "        {\"name\": \"certified\", \"distinctValues\": 2, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }
    
    private OptimizationRequest createRequest(String sql, double threshold) {
        OptimizationRequest request = new OptimizationRequest();
        request.setSqlQuery(sql);
        request.setMetadata(metadataJson);
        request.setStatistics(statisticsJson);
        request.setCostThreshold(threshold);
        return request;
    }
    
    private void assertOptimizationSuccess(OptimizationResult result) {
        assertNotNull("Результат не должен быть null", result);
        assertTrue("Оптимизация должна быть успешной", result.isSuccess());
        assertNotNull("Подзапросы не должны быть null", result.getSubQueries());
        assertFalse("Должен быть создан хотя бы один подзапрос", result.getSubQueries().isEmpty());
        assertTrue("Общая стоимость должна быть положительной", result.getTotalCost() > 0);
    }

    // ==================== ТЕСТЫ ОКОННЫХ ФУНКЦИЙ ====================

    @Test
    public void testWindowFunctionRowNumber() {
        String sql = "SELECT name, salary, ROW_NUMBER() OVER (ORDER BY salary DESC) as rank FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionRank() {
        String sql = "SELECT name, salary, RANK() OVER (ORDER BY salary DESC) as rank FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionDenseRank() {
        String sql = "SELECT name, salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rank FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionPartitionBy() {
        String sql = "SELECT name, department_id, salary, " +
                    "RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as dept_rank " +
                    "FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionLag() {
        String sql = "SELECT name, salary, LAG(salary, 1) OVER (ORDER BY salary) as prev_salary FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionLead() {
        String sql = "SELECT name, salary, LEAD(salary, 1) OVER (ORDER BY salary) as next_salary FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionSum() {
        String sql = "SELECT name, department_id, salary, " +
                    "SUM(salary) OVER (PARTITION BY department_id) as dept_total_salary " +
                    "FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testWindowFunctionAvg() {
        String sql = "SELECT name, department_id, salary, " +
                    "AVG(salary) OVER (PARTITION BY department_id) as dept_avg_salary " +
                    "FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ РЕКУРСИВНЫХ CTE ====================

    @Test
    public void testRecursiveCTE() {
        String sql = "WITH RECURSIVE emp_hierarchy AS (" +
                    "  SELECT id, name, manager_id, 1 as level " +
                    "  FROM employees " +
                    "  WHERE manager_id IS NULL " +
                    "  UNION ALL " +
                    "  SELECT e.id, e.name, e.manager_id, eh.level + 1 " +
                    "  FROM employees e " +
                    "  JOIN emp_hierarchy eh ON e.manager_id = eh.id" +
                    ") " +
                    "SELECT * FROM emp_hierarchy";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ LATERAL JOIN ====================

    @Test
    public void testLateralJoin() {
        String sql = "SELECT e.name, p.name as project_name " +
                    "FROM employees e " +
                    "CROSS JOIN LATERAL (" +
                    "  SELECT name FROM projects WHERE department_id = e.department_id LIMIT 1" +
                    ") p";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ JSON ФУНКЦИЙ ====================

    @Test
    public void testJsonFunctions() {
        String sql = "SELECT name, " +
                    "JSON_VALUE(skills, '$.primary') as primary_skill, " +
                    "JSON_QUERY(skills, '$.secondary') as secondary_skills " +
                    "FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ МАТЕМАТИЧЕСКИХ ФУНКЦИЙ ====================

    @Test
    public void testMathematicalFunctions() {
        String sql = "SELECT name, salary, " +
                    "POWER(salary, 2) as salary_squared, " +
                    "SQRT(salary) as salary_sqrt, " +
                    "LOG(salary) as salary_log, " +
                    "EXP(salary/10000) as salary_exp " +
                    "FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ УСЛОВНОЙ ЛОГИКИ ====================

    @Test
    public void testComplexCaseExpression() {
        String sql = "SELECT name, salary, " +
                    "CASE " +
                    "  WHEN salary > 80000 THEN 'Executive' " +
                    "  WHEN salary > 60000 THEN 'Senior' " +
                    "  WHEN salary > 40000 THEN 'Mid-level' " +
                    "  WHEN salary > 30000 THEN 'Junior' " +
                    "  ELSE 'Entry-level' " +
                    "END as level, " +
                    "CASE department_id " +
                    "  WHEN 1 THEN 'Engineering' " +
                    "  WHEN 2 THEN 'Sales' " +
                    "  WHEN 3 THEN 'Marketing' " +
                    "  ELSE 'Other' " +
                    "END as dept_name " +
                    "FROM employees";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ АГРЕГАЦИОННЫХ ФУНКЦИЙ ====================

    @Test
    public void testAdvancedAggregations() {
        String sql = "SELECT department_id, " +
                    "COUNT(*) as emp_count, " +
                    "COUNT(DISTINCT manager_id) as manager_count, " +
                    "STDDEV(salary) as salary_stddev, " +
                    "VARIANCE(salary) as salary_variance, " +
                    "PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) as median_salary " +
                    "FROM employees " +
                    "GROUP BY department_id";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ ПОДЗАПРОСОВ В РАЗНЫХ ЧАСТЯХ ====================

    @Test
    public void testSubqueryInHaving() {
        String sql = "SELECT department_id, AVG(salary) as avg_salary " +
                    "FROM employees " +
                    "GROUP BY department_id " +
                    "HAVING AVG(salary) > (SELECT AVG(salary) FROM employees)";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testCorrelatedSubquery() {
        String sql = "SELECT e.name, e.salary, " +
                    "(SELECT COUNT(*) FROM employee_projects ep WHERE ep.employee_id = e.id) as project_count " +
                    "FROM employees e";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testExistsSubquery() {
        String sql = "SELECT e.name " +
                    "FROM employees e " +
                    "WHERE EXISTS (SELECT 1 FROM employee_projects ep WHERE ep.employee_id = e.id)";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testNotExistsSubquery() {
        String sql = "SELECT e.name " +
                    "FROM employees e " +
                    "WHERE NOT EXISTS (SELECT 1 FROM employee_projects ep WHERE ep.employee_id = e.id)";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ СЛОЖНЫХ JOIN УСЛОВИЙ ====================

    @Test
    public void testMultipleJoinConditions() {
        String sql = "SELECT e.name, p.name as project_name, ep.role " +
                    "FROM employees e " +
                    "JOIN employee_projects ep ON e.id = ep.employee_id " +
                    "JOIN projects p ON ep.project_id = p.id AND p.department_id = e.department_id " +
                    "WHERE ep.start_date >= '2023-01-01'";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testSelfJoin() {
        String sql = "SELECT e1.name as employee, e2.name as manager " +
                    "FROM employees e1 " +
                    "LEFT JOIN employees e2 ON e1.manager_id = e2.id";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ СЛОЖНЫХ УСЛОВИЙ ====================

    @Test
    public void testComplexWhereConditions() {
        String sql = "SELECT * FROM employees " +
                    "WHERE (salary > 50000 AND department_id IN (1, 2, 3)) " +
                    "   OR (salary > 40000 AND manager_id IS NOT NULL) " +
                    "   OR (is_active = true AND hire_date >= '2020-01-01')";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testPatternMatching() {
        String sql = "SELECT * FROM employees " +
                    "WHERE name LIKE 'J%' " +
                    "   OR name LIKE '%son' " +
                    "   OR name LIKE '%an%'";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    // ==================== ТЕСТЫ СЛОЖНЫХ АГРЕГАЦИЙ ====================

    @Test
    public void testRollupAggregation() {
        String sql = "SELECT department_id, manager_id, COUNT(*), AVG(salary) " +
                    "FROM employees " +
                    "GROUP BY ROLLUP(department_id, manager_id)";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testCubeAggregation() {
        String sql = "SELECT department_id, is_active, COUNT(*), AVG(salary) " +
                    "FROM employees " +
                    "GROUP BY CUBE(department_id, is_active)";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }

    @Test
    public void testGroupingSets() {
        String sql = "SELECT department_id, manager_id, COUNT(*), AVG(salary) " +
                    "FROM employees " +
                    "GROUP BY GROUPING SETS ((department_id), (manager_id), (department_id, manager_id), ())";
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        
        assertOptimizationSuccess(result);
    }
}




