package com.optimizer;

import com.optimizer.core.QueryOptimizer;
import com.optimizer.model.OptimizationRequest;
import com.optimizer.model.OptimizationResult;
import com.optimizer.util.JsonUtils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Консольная утилита для оптимизации и разбиения SQL запросов с использованием Apache Calcite
 */
public class SqlQueryOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(SqlQueryOptimizer.class);

    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("sql-query-optimizer", options);
                return;
            }

            // Получаем параметры из командной строки
            String sqlQuery = cmd.getOptionValue("sql");
            String sqlFile = cmd.getOptionValue("sql-file");
            String metadataFile = cmd.getOptionValue("metadata");
            String statisticsFile = cmd.getOptionValue("statistics");
            String outputFile = cmd.getOptionValue("output");
            double costThreshold = Double.parseDouble(cmd.getOptionValue("threshold", "1000.0"));

            // Загружаем SQL запрос
            String query;
            if (sqlQuery != null) {
                query = sqlQuery;
            } else if (sqlFile != null) {
                query = new String(Files.readAllBytes(Paths.get(sqlFile)));
            } else {
                throw new IllegalArgumentException("Необходимо указать SQL запрос через --sql или --sql-file");
            }

            // Загружаем метаданные
            String metadata;
            if (metadataFile != null) {
                metadata = new String(Files.readAllBytes(Paths.get(metadataFile)));
            } else {
                throw new IllegalArgumentException("Необходимо указать файл метаданных через --metadata");
            }

            // Загружаем статистику (опционально)
            String statistics = null;
            if (statisticsFile != null) {
                statistics = new String(Files.readAllBytes(Paths.get(statisticsFile)));
            }

            // Создаем запрос на оптимизацию
            OptimizationRequest request = new OptimizationRequest();
            request.setSqlQuery(query);
            request.setMetadata(metadata);
            request.setStatistics(statistics);
            request.setCostThreshold(costThreshold);

            logger.info("Начинаем оптимизацию SQL запроса...");
            logger.info("SQL запрос: {}", query);
            logger.info("Порог стоимости: {}", costThreshold);

            // Выполняем оптимизацию
            QueryOptimizer optimizer = new QueryOptimizer();
            OptimizationResult result = optimizer.optimize(request);

            // Выводим результат
            String resultJson = JsonUtils.toJson(result);
            if (outputFile != null) {
                Files.write(Paths.get(outputFile), resultJson.getBytes());
                logger.info("Результат сохранен в файл: {}", outputFile);
            } else {
                System.out.println(resultJson);
            }

            if (result.isSuccess()) {
                logger.info("Оптимизация завершена успешно");
                logger.info("Количество созданных подзапросов: {}", result.getSubQueries().size());
                logger.info("Общая стоимость: {}", result.getTotalCost());
            } else {
                logger.error("Оптимизация завершена с ошибкой: {}", result.getErrorMessage());
            }

        } catch (ParseException e) {
            logger.error("Ошибка парсинга аргументов командной строки: {}", e.getMessage());
            formatter.printHelp("sql-query-optimizer", options);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Ошибка чтения файла: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Ошибка оптимизации: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder("s")
                .longOpt("sql")
                .hasArg()
                .desc("SQL запрос для оптимизации")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("sql-file")
                .hasArg()
                .desc("Файл с SQL запросом")
                .build());

        options.addOption(Option.builder("m")
                .longOpt("metadata")
                .hasArg()
                .required()
                .desc("Файл с метаданными хранилища (JSON)")
                .build());

        options.addOption(Option.builder("t")
                .longOpt("statistics")
                .hasArg()
                .desc("Файл со статистикой таблиц (JSON)")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .desc("Файл для сохранения результата (JSON)")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("threshold")
                .hasArg()
                .desc("Порог стоимости для разбиения запроса (по умолчанию: 1000.0)")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Показать справку")
                .build());

        return options;
    }
}
