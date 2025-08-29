package com.optimizer;

import com.optimizer.core.QueryOptimizer;
import com.optimizer.model.OptimizationRequest;
import com.optimizer.model.OptimizationResult;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Тесты производительности и стресс-тесты для SQL Query Optimizer
 */
public class PerformanceAndStressTest {
    
    private QueryOptimizer optimizer;
    private String metadataJson;
    private String statisticsJson;
    
    @Before
    public void setUp() {
        optimizer = new QueryOptimizer();
        
        // Метаданные для производительных тестов
        metadataJson = "{\n" +
            "  \"tables\": [\n" +
            "    {\n" +
            "      \"name\": \"large_table\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"name\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"category_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"value1\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"value2\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"value3\", \"type\": \"decimal\"},\n" +
            "        {\"name\": \"date_field\", \"type\": \"date\"},\n" +
            "        {\"name\": \"status\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"description\", \"type\": \"varchar\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"categories\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"name\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"parent_id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"level\", \"type\": \"integer\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"dimensions\",\n" +
            "      \"columns\": [\n" +
            "        {\"name\": \"id\", \"type\": \"integer\"},\n" +
            "        {\"name\": \"dim1\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"dim2\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"dim3\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"dim4\", \"type\": \"varchar\"},\n" +
            "        {\"name\": \"dim5\", \"type\": \"varchar\"}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        // Статистика для производительных тестов
        statisticsJson = "{\n" +
            "  \"tables\": [\n" +
            "    {\n" +
            "      \"name\": \"large_table\",\n" +
            "      \"rowCount\": 1000000,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 1000000, \"nullCount\": 0},\n" +
            "        {\"name\": \"name\", \"distinctValues\": 500000, \"nullCount\": 0},\n" +
            "        {\"name\": \"category_id\", \"distinctValues\": 1000, \"nullCount\": 0},\n" +
            "        {\"name\": \"value1\", \"distinctValues\": 10000, \"nullCount\": 0},\n" +
            "        {\"name\": \"value2\", \"distinctValues\": 10000, \"nullCount\": 0},\n" +
            "        {\"name\": \"value3\", \"distinctValues\": 10000, \"nullCount\": 0},\n" +
            "        {\"name\": \"date_field\", \"distinctValues\": 3650, \"nullCount\": 0},\n" +
            "        {\"name\": \"status\", \"distinctValues\": 10, \"nullCount\": 0},\n" +
            "        {\"name\": \"description\", \"distinctValues\": 200000, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"categories\",\n" +
            "      \"rowCount\": 1000,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 1000, \"nullCount\": 0},\n" +
            "        {\"name\": \"name\", \"distinctValues\": 1000, \"nullCount\": 0},\n" +
            "        {\"name\": \"parent_id\", \"distinctValues\": 100, \"nullCount\": 0},\n" +
            "        {\"name\": \"level\", \"distinctValues\": 5, \"nullCount\": 0}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"dimensions\",\n" +
            "      \"rowCount\": 10000,\n" +
            "      \"columnStats\": [\n" +
            "        {\"name\": \"id\", \"distinctValues\": 10000, \"nullCount\": 0},\n" +
            "        {\"name\": \"dim1\", \"distinctValues\": 100, \"nullCount\": 0},\n" +
            "        {\"name\": \"dim2\", \"distinctValues\": 200, \"nullCount\": 0},\n" +
            "        {\"name\": \"dim3\", \"distinctValues\": 50, \"nullCount\": 0},\n" +
            "        {\"name\": \"dim4\", \"distinctValues\": 300, \"nullCount\": 0},\n" +
            "        {\"name\": \"dim5\", \"distinctValues\": 150, \"nullCount\": 0}\n" +
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

    // ==================== ТЕСТЫ ПРОИЗВОДИТЕЛЬНОСТИ ====================

    @Test
    public void testSimpleQueryPerformance() {
        String sql = "SELECT * FROM large_table WHERE category_id = 1";
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        System.out.println("Время выполнения простого запроса: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Время выполнения должно быть менее 5 секунд", executionTime < 5000);
    }

    @Test
    public void testComplexQueryPerformance() {
        String sql = "SELECT c.name, COUNT(*), AVG(lt.value1), SUM(lt.value2) " +
                    "FROM large_table lt " +
                    "JOIN categories c ON lt.category_id = c.id " +
                    "WHERE lt.date_field >= '2023-01-01' " +
                    "GROUP BY c.name " +
                    "HAVING COUNT(*) > 100 " +
                    "ORDER BY AVG(lt.value1) DESC";
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        System.out.println("Время выполнения сложного запроса: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Время выполнения должно быть менее 10 секунд", executionTime < 10000);
    }

    @Test
    public void testLargeJoinPerformance() {
        String sql = "SELECT lt.id, lt.name, c.name as category_name, " +
                    "d.dim1, d.dim2, d.dim3 " +
                    "FROM large_table lt " +
                    "JOIN categories c ON lt.category_id = c.id " +
                    "JOIN dimensions d ON lt.id = d.id " +
                    "WHERE lt.value1 > 1000 AND c.level = 1";
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        System.out.println("Время выполнения запроса с большими JOIN: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Время выполнения должно быть менее 15 секунд", executionTime < 15000);
    }

    @Test
    public void testAggregationPerformance() {
        String sql = "SELECT c.name, " +
                    "COUNT(*) as total_count, " +
                    "COUNT(DISTINCT lt.id) as distinct_count, " +
                    "AVG(lt.value1) as avg_value1, " +
                    "STDDEV(lt.value2) as stddev_value2, " +
                    "MIN(lt.value3) as min_value3, " +
                    "MAX(lt.value3) as max_value3 " +
                    "FROM large_table lt " +
                    "JOIN categories c ON lt.category_id = c.id " +
                    "GROUP BY c.name " +
                    "ORDER BY total_count DESC";
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        System.out.println("Время выполнения запроса с агрегацией: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Время выполнения должно быть менее 12 секунд", executionTime < 12000);
    }

    @Test
    public void testWindowFunctionPerformance() {
        String sql = "SELECT name, category_id, value1, " +
                    "ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY value1 DESC) as rank, " +
                    "AVG(value1) OVER (PARTITION BY category_id) as avg_by_category, " +
                    "SUM(value1) OVER (ORDER BY value1 ROWS BETWEEN 10 PRECEDING AND 10 FOLLOWING) as moving_sum " +
                    "FROM large_table " +
                    "WHERE value1 > 500";
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        System.out.println("Время выполнения запроса с оконными функциями: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Время выполнения должно быть менее 8 секунд", executionTime < 8000);
    }

    // ==================== ТЕСТЫ РАЗБИЕНИЯ ЗАПРОСОВ ====================

    @Test
    public void testQuerySplittingPerformance() {
        String sql = "SELECT lt.id, lt.name, c.name as category_name, " +
                    "d.dim1, d.dim2, d.dim3, " +
                    "AVG(lt.value1) OVER (PARTITION BY c.name) as avg_by_category " +
                    "FROM large_table lt " +
                    "JOIN categories c ON lt.category_id = c.id " +
                    "JOIN dimensions d ON lt.id = d.id " +
                    "WHERE lt.value1 > 1000 AND c.level = 1 " +
                    "ORDER BY lt.value1 DESC";
        
        // Тестируем с разными порогами стоимости
        double[] thresholds = {100.0, 500.0, 1000.0, 5000.0, 10000.0};
        
        for (double threshold : thresholds) {
            long startTime = System.currentTimeMillis();
            OptimizationRequest request = createRequest(sql, threshold);
            OptimizationResult result = optimizer.optimize(request);
            long endTime = System.currentTimeMillis();
            
            long executionTime = endTime - startTime;
            System.out.println("Порог " + threshold + ": " + executionTime + "ms, " + 
                             result.getSubQueries().size() + " подзапросов");
            
            assertOptimizationSuccess(result);
            assertTrue("Время выполнения должно быть менее 20 секунд", executionTime < 20000);
        }
    }

    // ==================== СТРЕСС-ТЕСТЫ ====================

    @Test
    public void testConcurrentOptimization() throws InterruptedException {
        int threadCount = 10;
        int queriesPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        String[] testQueries = {
            "SELECT * FROM large_table WHERE category_id = 1",
            "SELECT COUNT(*) FROM large_table WHERE value1 > 1000",
            "SELECT c.name, AVG(lt.value1) FROM large_table lt JOIN categories c ON lt.category_id = c.id GROUP BY c.name",
            "SELECT * FROM large_table WHERE value1 > 500 ORDER BY value1 DESC LIMIT 100",
            "SELECT lt.name, c.name FROM large_table lt JOIN categories c ON lt.category_id = c.id WHERE lt.value1 > 1000"
        };
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < queriesPerThread; j++) {
                    try {
                        String sql = testQueries[j % testQueries.length];
                        OptimizationRequest request = createRequest(sql, 1000.0);
                        OptimizationResult result = optimizer.optimize(request);
                        
                        if (result.isSuccess()) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Ошибка в потоке " + threadId + ": " + e.getMessage());
                    }
                }
            });
        }
        
        executor.shutdown();
        boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);
        
        System.out.println("Стресс-тест завершен:");
        System.out.println("Успешных оптимизаций: " + successCount.get());
        System.out.println("Неудачных оптимизаций: " + failureCount.get());
        System.out.println("Всего запросов: " + (threadCount * queriesPerThread));
        
        assertTrue("Тест должен завершиться в течение 60 секунд", finished);
        assertTrue("Должно быть больше успешных оптимизаций чем неудачных", 
                  successCount.get() > failureCount.get());
    }

    @Test
    public void testMemoryUsage() {
        // Тестируем использование памяти при обработке больших запросов
        String complexSql = "SELECT " +
                           "c1.name as category1, c2.name as category2, " +
                           "COUNT(*) as total_count, " +
                           "AVG(lt1.value1) as avg_value1, " +
                           "AVG(lt2.value2) as avg_value2, " +
                           "SUM(lt1.value3) as sum_value3 " +
                           "FROM large_table lt1 " +
                           "JOIN large_table lt2 ON lt1.category_id = lt2.category_id " +
                           "JOIN categories c1 ON lt1.category_id = c1.id " +
                           "JOIN categories c2 ON lt2.category_id = c2.id " +
                           "WHERE lt1.value1 > 1000 AND lt2.value2 > 1000 " +
                           "GROUP BY c1.name, c2.name " +
                           "HAVING COUNT(*) > 10 " +
                           "ORDER BY total_count DESC";
        
        Runtime runtime = Runtime.getRuntime();
        
        // Измеряем память до выполнения
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(complexSql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        // Измеряем память после выполнения
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        long executionTime = endTime - startTime;
        
        System.out.println("Использование памяти: " + (memoryUsed / 1024 / 1024) + "MB");
        System.out.println("Время выполнения: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Использование памяти должно быть менее 1GB", memoryUsed < 1024 * 1024 * 1024);
        assertTrue("Время выполнения должно быть менее 30 секунд", executionTime < 30000);
    }

    @Test
    public void testLargeResultSet() {
        // Тестируем обработку запросов с большими результирующими наборами
        String sql = "SELECT lt.id, lt.name, lt.value1, lt.value2, lt.value3, " +
                    "c.name as category_name, c.level, " +
                    "d.dim1, d.dim2, d.dim3, d.dim4, d.dim5 " +
                    "FROM large_table lt " +
                    "JOIN categories c ON lt.category_id = c.id " +
                    "JOIN dimensions d ON lt.id = d.id " +
                    "WHERE lt.value1 BETWEEN 100 AND 10000 " +
                    "ORDER BY lt.value1 DESC " +
                    "LIMIT 10000";
        
        long startTime = System.currentTimeMillis();
        OptimizationRequest request = createRequest(sql, 1000.0);
        OptimizationResult result = optimizer.optimize(request);
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        System.out.println("Время обработки большого результирующего набора: " + executionTime + "ms");
        
        assertOptimizationSuccess(result);
        assertTrue("Время выполнения должно быть менее 25 секунд", executionTime < 25000);
    }

    @Test
    public void testExtremeThresholds() {
        String sql = "SELECT * FROM large_table WHERE category_id = 1";
        
        // Тестируем экстремально низкие и высокие пороги
        double[] extremeThresholds = {1.0, 10.0, 100000.0, 1000000.0};
        
        for (double threshold : extremeThresholds) {
            long startTime = System.currentTimeMillis();
            OptimizationRequest request = createRequest(sql, threshold);
            OptimizationResult result = optimizer.optimize(request);
            long endTime = System.currentTimeMillis();
            
            long executionTime = endTime - startTime;
            System.out.println("Экстремальный порог " + threshold + ": " + executionTime + "ms, " + 
                             result.getSubQueries().size() + " подзапросов");
            
            assertOptimizationSuccess(result);
            assertTrue("Время выполнения должно быть менее 10 секунд", executionTime < 10000);
        }
    }
}




