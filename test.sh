#!/bin/bash

# Скрипт для тестирования SQL Query Optimizer

echo "=== SQL Query Optimizer Test ==="

# Проверяем, что JAR файл существует
if [ ! -f "target/sql-query-optimizer-1.0.0.jar" ]; then
    echo "Собираем проект..."
    mvn clean package
fi

echo ""
echo "Тест 1: Простой запрос с выводом в консоль"
echo "----------------------------------------"
java -jar target/sql-query-optimizer-1.0.0.jar \
  --sql "SELECT e.name, d.name FROM employees e JOIN departments d ON e.department_id = d.id" \
  --metadata examples/metadata.json

echo ""
echo "Тест 2: Сложный запрос с сохранением в файл"
echo "-------------------------------------------"
java -jar target/sql-query-optimizer-1.0.0.jar \
  --sql-file examples/query.sql \
  --metadata examples/metadata.json \
  --statistics examples/statistics.json \
  --threshold 1000 \
  --output test_result.json

echo ""
echo "Тест 3: Запрос с низким порогом стоимости"
echo "----------------------------------------"
java -jar target/sql-query-optimizer-1.0.0.jar \
  --sql "SELECT e.name, d.name, COUNT(o.id) FROM employees e JOIN departments d ON e.department_id = d.id LEFT JOIN orders o ON e.id = o.employee_id GROUP BY e.id, e.name, d.name" \
  --metadata examples/metadata.json \
  --threshold 100 \
  --output test_small_threshold.json

echo ""
echo "Тест 4: Показать справку"
echo "------------------------"
java -jar target/sql-query-optimizer-1.0.0.jar --help

echo ""
echo "=== Тестирование завершено ==="
echo "Результаты сохранены в файлы:"
echo "- test_result.json"
echo "- test_small_threshold.json"
