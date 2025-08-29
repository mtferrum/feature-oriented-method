# Техническая документация SQL Query Optimizer

## Содержание

1. [Обзор проекта](#обзор-проекта)
2. [Архитектура системы](#архитектура-системы)
3. [Технические требования](#технические-требования)
4. [Установка и настройка](#установка-и-настройка)
5. [API и интерфейсы](#api-и-интерфейсы)
6. [Алгоритмы и методы](#алгоритмы-и-методы)
7. [Конфигурация](#конфигурация)
8. [Мониторинг и логирование](#мониторинг-и-логирование)
9. [Безопасность](#безопасность)
10. [Производительность](#производительность)
11. [Устранение неполадок](#устранение-неполадок)
12. [Примеры использования](#примеры-использования)

## Обзор проекта

### Назначение
SQL Query Optimizer - это консольная утилита для оптимизации и разбиения сложных SQL-запросов на более мелкие подзапросы с использованием Apache Calcite. Утилита применяет cost-based оптимизацию для определения оптимального способа выполнения запросов.

### Основные возможности
- Парсинг и валидация SQL-запросов
- Cost-based оптимизация с использованием Apache Calcite
- Автоматическое разбиение запросов по критерию стоимости
- Создание временных таблиц для промежуточных результатов
- Интеграция с внешними метаданными и статистикой
- Поддержка всех SQL-возможностей Apache Calcite

### Технологический стек
- **Java 11** - основной язык разработки
- **Apache Calcite** - ядро оптимизации и парсинга SQL
- **Maven** - система сборки и управления зависимостями
- **Jackson** - сериализация/десериализация JSON
- **SLF4J** - система логирования
- **JUnit 4** - фреймворк тестирования

## Архитектура системы

### Компонентная архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                    SqlQueryOptimizer                        │
│                    (Main Entry Point)                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    QueryOptimizer                           │
│                    (Core Logic)                             │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌────▼────┐ ┌─────▼─────┐
│MetadataParser│ │Statistics│ │QuerySplitter│
│              │ │ Parser   │ │            │
└──────────────┘ └─────────┘ └────────────┘
```

### Поток данных

1. **Входные данные**: SQL-запрос, метаданные, статистика
2. **Парсинг**: Валидация и преобразование в RelNode
3. **Оптимизация**: Cost-based оптимизация с Calcite
4. **Разбиение**: Разделение на подзапросы по стоимости
5. **Выходные данные**: Список подзапросов с планом выполнения

## Технические требования

### Системные требования
- **Java**: версия 11 или выше
- **Maven**: версия 3.6 или выше
- **Операционная система**: Linux, macOS, Windows
- **Память**: минимум 512MB RAM
- **Дисковое пространство**: 100MB для установки

### Зависимости

#### Основные зависимости
```xml
<dependency>
    <groupId>org.apache.calcite</groupId>
    <artifactId>calcite-core</artifactId>
    <version>1.32.0</version>
</dependency>
<dependency>
    <groupId>org.apache.calcite</groupId>
    <artifactId>calcite-linq4j</artifactId>
    <version>1.32.0</version>
</dependency>
```

#### Вспомогательные зависимости
- **Jackson**: JSON-обработка
- **Commons CLI**: парсинг командной строки
- **SLF4J**: логирование
- **JUnit 4**: тестирование

## Установка и настройка

### Сборка проекта

```bash
# Клонирование репозитория
git clone <repository-url>
cd optimizer

# Сборка проекта
mvn clean compile

# Создание исполняемого JAR
mvn package
```

### Структура каталогов

```
optimizer/
├── src/
│   ├── main/java/com/optimizer/
│   │   ├── SqlQueryOptimizer.java      # Главный класс
│   │   ├── core/
│   │   │   └── QueryOptimizer.java     # Основная логика
│   │   ├── parser/
│   │   │   ├── MetadataParser.java     # Парсер метаданных
│   │   │   └── StatisticsParser.java   # Парсер статистики
│   │   ├── splitter/
│   │   │   └── QuerySplitter.java      # Разбиение запросов
│   │   ├── model/
│   │   │   ├── OptimizationRequest.java
│   │   │   ├── OptimizationResult.java
│   │   │   └── SubQuery.java
│   │   └── util/
│   │       └── JsonUtils.java          # JSON-утилиты
│   └── test/java/com/optimizer/
│       ├── SqlQueryOptimizerTest.java
│       ├── AdvancedSqlFeaturesTest.java
│       └── PerformanceAndStressTest.java
├── examples/
│   ├── query.sql
│   ├── metadata.json
│   └── statistics.json
├── pom.xml
└── README.md
```

## API и интерфейсы

### Основной API

#### QueryOptimizer
```java
public class QueryOptimizer {
    public OptimizationResult optimize(OptimizationRequest request)
}
```

#### OptimizationRequest
```java
public class OptimizationRequest {
    private String sqlQuery;           // SQL-запрос
    private String metadata;           // JSON метаданных
    private String statistics;         // JSON статистики
    private double costThreshold;      // Порог стоимости
}
```

#### OptimizationResult
```java
public class OptimizationResult {
    private String originalQuery;      // Исходный запрос
    private List<SubQuery> subQueries; // Подзапросы
    private double totalCost;          // Общая стоимость
    private String optimizationPlan;   // План оптимизации
    private boolean success;           // Успешность
    private String errorMessage;       // Сообщение об ошибке
}
```

### Форматы данных

#### Метаданные (JSON)
```json
{
  "tables": [
    {
      "name": "employees",
      "columns": [
        {"name": "id", "type": "INTEGER"},
        {"name": "name", "type": "VARCHAR(100)"}
      ]
    }
  ],
  "views": [
    {
      "name": "employee_summary",
      "sql": "SELECT dept_id, COUNT(*) FROM employees GROUP BY dept_id"
    }
  ]
}
```

#### Статистика (JSON)
```json
{
  "tables": [
    {
      "name": "employees",
      "rowCount": 1000,
      "columnStats": [
        {
          "name": "id",
          "distinctValues": 1000,
          "nullCount": 0
        }
      ]
    }
  ]
}
```

## Алгоритмы и методы

### Cost-Based Оптимизация

#### Принцип работы
1. **Парсинг SQL**: Преобразование в SqlNode
2. **Валидация**: Проверка синтаксиса и семантики
3. **Преобразование в RelNode**: Создание логического плана
4. **Расчет стоимости**: Использование RelMetadataQuery.getCumulativeCost()
5. **Оптимизация**: Применение правил оптимизации Calcite

#### Критерии разбиения
- **Порог стоимости**: Запрос разбивается, если стоимость > threshold
- **Сложность операций**: JOIN, агрегации, подзапросы
- **Размер данных**: Количество строк и колонок
- **Типы операций**: Сканирование, фильтрация, соединение

### Алгоритм разбиения запросов

```java
public List<SubQuery> splitQuery(RelNode relNode, double threshold) {
    List<SubQuery> subQueries = new ArrayList<>();
    
    // 1. Расчет стоимости текущего узла
    double cost = RelMetadataQuery.getCumulativeCost(relNode);
    
    // 2. Проверка необходимости разбиения
    if (cost <= threshold) {
        subQueries.add(createSubQuery(relNode));
        return subQueries;
    }
    
    // 3. Поиск точек разбиения
    List<RelNode> children = getChildren(relNode);
    
    // 4. Рекурсивное разбиение
    for (RelNode child : children) {
        subQueries.addAll(splitQuery(child, threshold));
    }
    
    return subQueries;
}
```

### Создание временных таблиц

#### Критерии создания
- Результат подзапроса используется многократно
- Размер промежуточного результата значителен
- Сложность вычислений высока

#### Формат временных таблиц
```sql
CREATE TEMPORARY TABLE temp_result_1 AS
SELECT dept_id, COUNT(*) as emp_count
FROM employees
GROUP BY dept_id;
```

## Конфигурация

### Параметры командной строки

| Параметр | Описание | Обязательный | По умолчанию |
|----------|----------|--------------|--------------|
| `-q, --query` | SQL-запрос | Да | - |
| `-m, --metadata` | Файл метаданных | Да | - |
| `-s, --statistics` | Файл статистики | Да | - |
| `-t, --threshold` | Порог стоимости | Нет | 1000.0 |
| `-o, --output` | Выходной файл | Нет | stdout |
| `-v, --verbose` | Подробный вывод | Нет | false |

### Конфигурационные файлы

#### logback.xml
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## Мониторинг и логирование

### Уровни логирования

- **ERROR**: Критические ошибки
- **WARN**: Предупреждения
- **INFO**: Информационные сообщения
- **DEBUG**: Отладочная информация
- **TRACE**: Детальная трассировка

### Метрики производительности

- **Время парсинга**: Длительность парсинга SQL
- **Время оптимизации**: Длительность cost-based оптимизации
- **Время разбиения**: Длительность разбиения на подзапросы
- **Общая стоимость**: Суммарная стоимость всех подзапросов
- **Количество подзапросов**: Число созданных подзапросов

### Примеры логов

```
INFO  - Запуск оптимизации SQL-запроса
DEBUG - Парсинг метаданных из файла: metadata.json
INFO  - Загружено 3 таблицы и 1 представление
DEBUG - Парсинг статистики из файла: statistics.json
INFO  - Загружена статистика для 3 таблиц
DEBUG - Преобразование SQL в RelNode
INFO  - Расчет стоимости запроса: 2500.0
WARN  - Стоимость превышает порог (1000.0), выполняется разбиение
INFO  - Создано 3 подзапроса
INFO  - Общая стоимость подзапросов: 1800.0
INFO  - Оптимизация завершена успешно
```

## Безопасность

### Валидация входных данных

#### SQL-запросы
- Проверка синтаксиса
- Валидация имен таблиц и колонок
- Проверка прав доступа (если применимо)

#### Метаданные
- Валидация JSON-структуры
- Проверка типов данных
- Верификация ссылок между таблицами

#### Статистика
- Проверка корректности числовых значений
- Валидация соответствия метаданным

### Обработка ошибок

```java
try {
    OptimizationResult result = optimizer.optimize(request);
    if (result.isSuccess()) {
        // Обработка успешного результата
    } else {
        logger.error("Ошибка оптимизации: {}", result.getErrorMessage());
    }
} catch (Exception e) {
    logger.error("Критическая ошибка: {}", e.getMessage(), e);
}
```

## Производительность

### Оптимизация памяти

- **Ленивая загрузка**: Метаданные загружаются по требованию
- **Кэширование**: Кэширование результатов парсинга
- **Очистка ресурсов**: Автоматическое освобождение памяти

### Оптимизация CPU

- **Параллельная обработка**: Одновременная обработка независимых подзапросов
- **Кэширование планов**: Переиспользование оптимизированных планов
- **Эвристики**: Быстрые эвристики для простых случаев

### Бенчмарки

| Тип запроса | Время парсинга | Время оптимизации | Время разбиения |
|-------------|----------------|-------------------|-----------------|
| Простой SELECT | 5ms | 10ms | 2ms |
| JOIN 2 таблиц | 15ms | 25ms | 8ms |
| Сложная агрегация | 30ms | 50ms | 15ms |
| Подзапросы | 45ms | 80ms | 25ms |

## Устранение неполадок

### Частые проблемы

#### 1. Ошибка парсинга SQL
```
ERROR - SqlParseException: Encountered ";" at line 14, column 10
```
**Решение**: Удалить точку с запятой в конце SQL-запроса

#### 2. Ошибка JAR-файла
```
SecurityException: Invalid signature file digest
```
**Решение**: Использовать maven-shade-plugin с фильтрами

#### 3. Ошибка Calcite Planner
```
IllegalArgumentException: cannot move from STATE_1_RESET to STATE_4_VALIDATED
```
**Решение**: Проверить корректность SQL и метаданных

### Диагностика

#### Включение отладочного режима
```bash
java -jar optimizer.jar -q query.sql -m metadata.json -s statistics.json -v
```

#### Проверка метаданных
```bash
# Валидация JSON
cat metadata.json | jq .

# Проверка структуры
java -cp optimizer.jar com.optimizer.util.JsonUtils metadata.json
```

## Примеры использования

### Базовый пример

```bash
# Простая оптимизация
java -jar optimizer.jar \
  -q "SELECT e.name, d.name FROM employees e JOIN departments d ON e.dept_id = d.id" \
  -m metadata.json \
  -s statistics.json \
  -t 1000.0
```

### Сложный пример

```bash
# Оптимизация с выводом в файл
java -jar optimizer.jar \
  -q "SELECT d.name, COUNT(*), AVG(e.salary) 
      FROM employees e 
      JOIN departments d ON e.dept_id = d.id 
      WHERE e.hire_date > '2020-01-01' 
      GROUP BY d.id, d.name 
      HAVING COUNT(*) > 10 
      ORDER BY AVG(e.salary) DESC" \
  -m metadata.json \
  -s statistics.json \
  -t 500.0 \
  -o result.json \
  -v
```

### Пример метаданных

```json
{
  "tables": [
    {
      "name": "employees",
      "columns": [
        {"name": "id", "type": "INTEGER", "nullable": false},
        {"name": "name", "type": "VARCHAR(100)", "nullable": false},
        {"name": "dept_id", "type": "INTEGER", "nullable": true},
        {"name": "salary", "type": "DECIMAL(10,2)", "nullable": true},
        {"name": "hire_date", "type": "DATE", "nullable": true}
      ]
    },
    {
      "name": "departments",
      "columns": [
        {"name": "id", "type": "INTEGER", "nullable": false},
        {"name": "name", "type": "VARCHAR(50)", "nullable": false},
        {"name": "location", "type": "VARCHAR(100)", "nullable": true}
      ]
    }
  ],
  "views": [
    {
      "name": "employee_summary",
      "sql": "SELECT dept_id, COUNT(*) as emp_count, AVG(salary) as avg_salary FROM employees GROUP BY dept_id"
    }
  ]
}
```

### Пример статистики

```json
{
  "tables": [
    {
      "name": "employees",
      "rowCount": 10000,
      "columnStats": [
        {
          "name": "id",
          "distinctValues": 10000,
          "nullCount": 0,
          "minValue": 1,
          "maxValue": 10000
        },
        {
          "name": "dept_id",
          "distinctValues": 50,
          "nullCount": 100,
          "minValue": 1,
          "maxValue": 50
        },
        {
          "name": "salary",
          "distinctValues": 5000,
          "nullCount": 50,
          "minValue": 30000,
          "maxValue": 200000
        }
      ]
    },
    {
      "name": "departments",
      "rowCount": 50,
      "columnStats": [
        {
          "name": "id",
          "distinctValues": 50,
          "nullCount": 0,
          "minValue": 1,
          "maxValue": 50
        }
      ]
    }
  ]
}
```

---

*Документация создана для версии 1.0 SQL Query Optimizer*

