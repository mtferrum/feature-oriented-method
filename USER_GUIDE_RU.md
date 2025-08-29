# Руководство пользователя SQL Query Optimizer

## Быстрый старт

### Установка

1. **Требования к системе**:
   - Java 11 или выше
   - Maven 3.6 или выше

2. **Сборка проекта**:
   ```bash
   git clone <repository-url>
   cd optimizer
   mvn clean package
   ```

3. **Проверка установки**:
   ```bash
   java -jar target/optimizer-1.0-SNAPSHOT.jar --help
   ```

### Первый запуск

1. **Подготовьте файлы**:
   - SQL-запрос в файле `query.sql`
   - Метаданные в файле `metadata.json`
   - Статистику в файле `statistics.json`

2. **Запустите оптимизацию**:
   ```bash
   java -jar target/optimizer-1.0-SNAPSHOT.jar \
     -q query.sql \
     -m metadata.json \
     -s statistics.json \
     -t 1000.0
   ```

## Основные команды

### Базовые параметры

| Параметр | Описание | Пример |
|----------|----------|--------|
| `-q, --query` | SQL-запрос или файл | `-q "SELECT * FROM table"` |
| `-m, --metadata` | Файл метаданных | `-m metadata.json` |
| `-s, --statistics` | Файл статистики | `-s statistics.json` |
| `-t, --threshold` | Порог стоимости | `-t 1000.0` |

### Дополнительные параметры

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `-o, --output` | Выходной файл | stdout |
| `-v, --verbose` | Подробный вывод | false |
| `--help` | Справка | - |

### Примеры использования

#### Простая оптимизация
```bash
java -jar optimizer.jar \
  -q "SELECT e.name, d.name FROM employees e JOIN departments d ON e.dept_id = d.id" \
  -m metadata.json \
  -s statistics.json
```

#### Оптимизация с порогом стоимости
```bash
java -jar optimizer.jar \
  -q "SELECT d.name, COUNT(*), AVG(e.salary) FROM employees e JOIN departments d ON e.dept_id = d.id GROUP BY d.id" \
  -m metadata.json \
  -s statistics.json \
  -t 500.0
```

#### Сохранение результата в файл
```bash
java -jar optimizer.jar \
  -q query.sql \
  -m metadata.json \
  -s statistics.json \
  -o result.json \
  -v
```

## Форматы файлов

### SQL-запрос

Можно указать SQL-запрос напрямую или в файле:

```sql
SELECT e.name, d.name, AVG(e.salary) as avg_salary
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.hire_date > '2020-01-01'
GROUP BY d.id, d.name
HAVING COUNT(*) > 10
ORDER BY avg_salary DESC
```

### Метаданные (metadata.json)

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
        {"name": "name", "type": "VARCHAR(50)", "nullable": false}
      ]
    }
  ],
  "views": [
    {
      "name": "employee_summary",
      "sql": "SELECT dept_id, COUNT(*) as emp_count FROM employees GROUP BY dept_id"
    }
  ]
}
```

### Статистика (statistics.json)

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

## Интерпретация результатов

### Структура результата

```json
{
  "originalQuery": "SELECT e.name, d.name FROM employees e JOIN departments d ON e.dept_id = d.id",
  "subQueries": [
    {
      "id": 1,
      "sql": "SELECT e.id, e.name, e.dept_id FROM employees e",
      "cost": 500.0,
      "dependencies": [],
      "isTemporaryTable": true,
      "temporaryTableName": "temp_result_1",
      "description": "Сканирование таблицы employees"
    },
    {
      "id": 2,
      "sql": "SELECT d.id, d.name FROM departments d",
      "cost": 50.0,
      "dependencies": [],
      "isTemporaryTable": false,
      "temporaryTableName": null,
      "description": "Сканирование таблицы departments"
    },
    {
      "id": 3,
      "sql": "SELECT t1.name, t2.name FROM temp_result_1 t1 JOIN departments t2 ON t1.dept_id = t2.id",
      "cost": 2500.0,
      "dependencies": [1, 2],
      "isTemporaryTable": false,
      "temporaryTableName": null,
      "description": "Соединение результатов"
    }
  ],
  "totalCost": 3050.0,
  "optimizationPlan": "Оптимизированный план выполнения",
  "success": true,
  "errorMessage": null
}
```

### Поля результата

| Поле | Описание |
|------|----------|
| `originalQuery` | Исходный SQL-запрос |
| `subQueries` | Список подзапросов |
| `totalCost` | Общая стоимость всех подзапросов |
| `optimizationPlan` | План оптимизации |
| `success` | Успешность оптимизации |
| `errorMessage` | Сообщение об ошибке (если есть) |

### Поля подзапроса

| Поле | Описание |
|------|----------|
| `id` | Уникальный идентификатор |
| `sql` | SQL-код подзапроса |
| `cost` | Стоимость выполнения |
| `dependencies` | Список ID зависимых подзапросов |
| `isTemporaryTable` | Создается ли временная таблица |
| `temporaryTableName` | Имя временной таблицы |
| `description` | Описание подзапроса |

## Порог стоимости

### Настройка порога

Порог стоимости (`-t`) определяет, когда запрос должен быть разбит:

- **Низкий порог (100-500)**: Агрессивное разбиение, много подзапросов
- **Средний порог (500-2000)**: Умеренное разбиение
- **Высокий порог (2000+)**: Минимальное разбиение

### Рекомендации по выбору порога

| Размер данных | Сложность запроса | Рекомендуемый порог |
|---------------|-------------------|---------------------|
| < 10K строк | Простой | 1000-2000 |
| 10K-100K строк | Средний | 500-1000 |
| 100K-1M строк | Сложный | 200-500 |
| > 1M строк | Очень сложный | 100-200 |

## Временные таблицы

### Когда создаются

Временные таблицы создаются автоматически, когда:
- Результат подзапроса используется многократно
- Стоимость подзапроса превышает половину порога
- Размер промежуточного результата значителен

### Управление временными таблицами

```sql
-- Создание временной таблицы
CREATE TEMPORARY TABLE temp_result_1 AS
SELECT dept_id, COUNT(*) as emp_count
FROM employees
GROUP BY dept_id;

-- Использование временной таблицы
SELECT d.name, t.emp_count
FROM departments d
JOIN temp_result_1 t ON d.id = t.dept_id;

-- Удаление временной таблицы
DROP TEMPORARY TABLE temp_result_1;
```

## Поддерживаемые SQL-возможности

### Основные операции

- ✅ SELECT, FROM, WHERE
- ✅ JOIN (INNER, LEFT, RIGHT, FULL)
- ✅ GROUP BY, HAVING
- ✅ ORDER BY, LIMIT, OFFSET
- ✅ Подзапросы (в WHERE, FROM, SELECT)
- ✅ Агрегатные функции (COUNT, SUM, AVG, MIN, MAX)
- ✅ Оконные функции (ROW_NUMBER, RANK, LAG, LEAD)

### Продвинутые возможности

- ✅ Рекурсивные CTE
- ✅ LATERAL JOIN
- ✅ JSON функции
- ✅ Математические функции
- ✅ Строковые функции
- ✅ Функции дат
- ✅ CASE выражения
- ✅ UNION, UNION ALL

## Устранение неполадок

### Частые ошибки

#### 1. Ошибка парсинга SQL
```
ERROR: SqlParseException: Encountered ";" at line 14, column 10
```
**Решение**: Удалите точку с запятой в конце SQL-запроса

#### 2. Ошибка метаданных
```
ERROR: Table 'employees' not found in schema
```
**Решение**: Проверьте, что таблица определена в metadata.json

#### 3. Ошибка статистики
```
ERROR: Statistics for table 'employees' not found
```
**Решение**: Добавьте статистику для таблицы в statistics.json

### Отладка

#### Включение подробного вывода
```bash
java -jar optimizer.jar -q query.sql -m metadata.json -s statistics.json -v
```

#### Проверка файлов
```bash
# Валидация JSON
cat metadata.json | jq .
cat statistics.json | jq .

# Проверка SQL
cat query.sql
```

## Примеры сценариев

### Сценарий 1: Простая оптимизация

**Задача**: Оптимизировать простой JOIN-запрос

**Входные данные**:
```sql
SELECT e.name, d.name 
FROM employees e 
JOIN departments d ON e.dept_id = d.id
```

**Результат**: Один подзапрос (запрос не разбивается)

### Сценарий 2: Сложная агрегация

**Задача**: Оптимизировать запрос с группировкой и агрегацией

**Входные данные**:
```sql
SELECT d.name, COUNT(*), AVG(e.salary)
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.hire_date > '2020-01-01'
GROUP BY d.id, d.name
HAVING COUNT(*) > 10
```

**Результат**: 3-4 подзапроса с временными таблицами

### Сценарий 3: Очень сложный запрос

**Задача**: Оптимизировать запрос с подзапросами и оконными функциями

**Входные данные**:
```sql
WITH ranked_employees AS (
  SELECT e.*, 
         ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) as rank
  FROM employees e
)
SELECT d.name, re.name, re.salary
FROM ranked_employees re
JOIN departments d ON re.dept_id = d.id
WHERE re.rank <= 5
```

**Результат**: 5+ подзапросов с множественными временными таблицами

## Производительность

### Метрики

Утилита предоставляет следующие метрики:
- Время парсинга SQL
- Время оптимизации
- Время разбиения на подзапросы
- Общая стоимость выполнения
- Количество созданных подзапросов

### Оптимизация

Для улучшения производительности:
1. Используйте актуальную статистику
2. Настройте оптимальный порог стоимости
3. Минимизируйте количество таблиц в запросе
4. Используйте индексы в базе данных

## Интеграция

### Автоматизация

#### Скрипт для пакетной обработки
```bash
#!/bin/bash
for query_file in queries/*.sql; do
    echo "Обработка $query_file"
    java -jar optimizer.jar \
      -q "$query_file" \
      -m metadata.json \
      -s statistics.json \
      -o "results/$(basename "$query_file" .sql).json"
done
```

#### Интеграция с CI/CD
```yaml
# .github/workflows/optimize.yml
name: SQL Optimization
on: [push, pull_request]
jobs:
  optimize:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Java
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build and test
        run: mvn clean package
      - name: Run optimization
        run: |
          java -jar target/optimizer-1.0-SNAPSHOT.jar \
            -q examples/query.sql \
            -m examples/metadata.json \
            -s examples/statistics.json \
            -o optimization-result.json
```

---

*Руководство пользователя создано для версии 1.0 SQL Query Optimizer*

