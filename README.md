# SQL Query Optimizer

Консольная утилита для оптимизации и разбиения SQL запросов с использованием Apache Calcite. Утилита использует cost-based оптимизацию для разбиения сложных запросов на более мелкие подзапросы, которые можно выполнить последовательно.

## Возможности

- **Cost-based оптимизация**: Использует Apache Calcite для анализа стоимости выполнения запросов
- **Разбиение запросов**: Автоматически разбивает сложные запросы на подзапросы с учетом порога стоимости
- **Временные таблицы**: Создает временные таблицы для промежуточных результатов при необходимости
- **Анализ метаданных**: Работает с метаданными хранилища в JSON формате
- **Статистика**: Учитывает статистику таблиц для более точной оценки стоимости

## Требования

- Java 11 или выше
- Maven 3.6 или выше

## Сборка

```bash
mvn clean package
```

После сборки будет создан исполняемый JAR файл `target/sql-query-optimizer-1.0.0.jar`.

## Использование

### Базовый синтаксис

```bash
java -jar target/sql-query-optimizer-1.0.0.jar [опции]
```

### Параметры командной строки

- `-s, --sql <запрос>` - SQL запрос для оптимизации
- `-f, --sql-file <файл>` - Файл с SQL запросом
- `-m, --metadata <файл>` - Файл с метаданными хранилища (JSON) **обязательный**
- `-t, --statistics <файл>` - Файл со статистикой таблиц (JSON) **опциональный**
- `-o, --output <файл>` - Файл для сохранения результата (JSON) **опциональный**
- `-c, --threshold <значение>` - Порог стоимости для разбиения запроса (по умолчанию: 1000.0)
- `-h, --help` - Показать справку

### Примеры использования

#### 1. Оптимизация запроса с указанием SQL в командной строке

```bash
java -jar target/sql-query-optimizer-1.0.0.jar \
  --sql "SELECT * FROM employees WHERE salary > 50000" \
  --metadata examples/metadata.json \
  --statistics examples/statistics.json \
  --threshold 500 \
  --output result.json
```

#### 2. Оптимизация запроса из файла

```bash
java -jar target/sql-query-optimizer-1.0.0.jar \
  --sql-file examples/query.sql \
  --metadata examples/metadata.json \
  --output result.json
```

#### 3. Вывод результата в консоль

```bash
java -jar target/sql-query-optimizer-1.0.0.jar \
  --sql "SELECT e.name, d.name FROM employees e JOIN departments d ON e.department_id = d.id" \
  --metadata examples/metadata.json
```

## Форматы файлов

### Метаданные (metadata.json)

```json
{
  "tables": [
    {
      "name": "table_name",
      "columns": [
        {"name": "column_name", "type": "column_type"}
      ]
    }
  ],
  "views": [
    {
      "name": "view_name",
      "sql": "SELECT ..."
    }
  ]
}
```

### Статистика (statistics.json)

```json
{
  "tables": [
    {
      "name": "table_name",
      "rowCount": 1000,
      "columnStats": [
        {
          "name": "column_name",
          "distinctValues": 500,
          "nullCount": 0
        }
      ]
    }
  ]
}
```

### Результат оптимизации

```json
{
  "originalQuery": "SELECT ...",
  "subQueries": [
    {
      "id": "Q1",
      "sql": "SELECT ...",
      "cost": 100.0,
      "dependencies": [],
      "isTemporaryTable": false,
      "temporaryTableName": null,
      "description": "Подзапрос Q1 со стоимостью 100.0"
    }
  ],
  "totalCost": 100.0,
  "optimizationPlan": "=== ПЛАН ОПТИМИЗАЦИИ ===\n...",
  "success": true,
  "errorMessage": null
}
```

## Алгоритм работы

1. **Парсинг метаданных**: Создание схемы Calcite на основе метаданных хранилища
2. **Загрузка статистики**: Применение статистики таблиц для улучшения оценки стоимости
3. **Парсинг SQL**: Преобразование SQL запроса в дерево операций Calcite
4. **Оптимизация**: Применение cost-based оптимизации с использованием правил Calcite
5. **Разбиение**: Анализ стоимости узлов и разбиение на подзапросы
6. **Генерация результата**: Создание плана выполнения с подзапросами

## Критерии разбиения

Утилита использует метод `getCumulativeCost` класса `RelMetadataQuery` Apache Calcite для вычисления стоимости выполнения каждого узла запроса. Запрос разбивается на подзапросы, если:

- Стоимость узла превышает установленный порог
- Узел можно разбить (JOIN, Project, Filter, Aggregate)
- Разбиение не нарушает семантику запроса

## Временные таблицы

При разбиении запроса утилита может создавать временные таблицы для хранения промежуточных результатов. Это позволяет:

- Выполнять подзапросы последовательно
- Переиспользовать результаты между подзапросами
- Уменьшить нагрузку на основное хранилище

## Логирование

Утилита использует SLF4J для логирования. Уровень логирования можно настроить через системные свойства:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG -jar target/sql-query-optimizer-1.0.0.jar ...
```

## Примеры

В папке `examples/` содержатся примеры файлов для тестирования:

- `query.sql` - Пример SQL запроса
- `metadata.json` - Пример метаданных
- `statistics.json` - Пример статистики

## Лицензия

Apache License 2.0




