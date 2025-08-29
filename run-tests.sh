#!/bin/bash

# Скрипт для запуска всех тестов SQL Query Optimizer

echo "=== SQL Query Optimizer - Запуск всех тестов ==="

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода с цветом
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверяем, что Maven установлен
if ! command -v mvn &> /dev/null; then
    print_error "Maven не найден. Установите Maven для запуска тестов."
    exit 1
fi

# Проверяем, что Java установлена
if ! command -v java &> /dev/null; then
    print_error "Java не найдена. Установите Java для запуска тестов."
    exit 1
fi

print_status "Проверка версии Java..."
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
print_status "Используется Java версии: $java_version"

# Очищаем и собираем проект
print_status "Сборка проекта..."
if mvn clean compile test-compile; then
    print_success "Проект успешно собран"
else
    print_error "Ошибка сборки проекта"
    exit 1
fi

echo ""
print_status "Запуск тестов..."

# Счетчики результатов
total_tests=0
passed_tests=0
failed_tests=0

# Функция для запуска тестов с подсчетом результатов
run_test_suite() {
    local test_name="$1"
    local test_class="$2"
    
    print_status "Запуск $test_name..."
    
    # Запускаем тесты и сохраняем результат
    if mvn test -Dtest="$test_class" -q; then
        print_success "$test_name: ВСЕ ТЕСТЫ ПРОЙДЕНЫ"
        ((passed_tests++))
    else
        print_error "$test_name: ЕСТЬ ОШИБКИ"
        ((failed_tests++))
    fi
    
    ((total_tests++))
    echo ""
}

# Запускаем основные тесты SQL возможностей
run_test_suite "Основные SQL возможности" "SqlQueryOptimizerTest"

# Запускаем тесты продвинутых SQL возможностей
run_test_suite "Продвинутые SQL возможности" "AdvancedSqlFeaturesTest"

# Запускаем тесты производительности и стресс-тесты
run_test_suite "Тесты производительности" "PerformanceAndStressTest"

# Запускаем все тесты вместе для проверки интеграции
print_status "Запуск всех тестов вместе..."
if mvn test -q; then
    print_success "Все тесты пройдены успешно"
else
    print_error "Некоторые тесты не прошли"
fi

echo ""
echo "=== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ ==="
echo "Всего тестовых наборов: $total_tests"
echo "Успешно пройдено: $passed_tests"
echo "С ошибками: $failed_tests"

if [ $failed_tests -eq 0 ]; then
    print_success "Все тесты пройдены успешно!"
    echo ""
    echo "=== ПРОВЕРЕННЫЕ SQL ВОЗМОЖНОСТИ ==="
    echo "✅ Базовые SQL операции (SELECT, WHERE, ORDER BY, LIMIT)"
    echo "✅ JOIN операции (INNER, LEFT, RIGHT, FULL)"
    echo "✅ Агрегация (COUNT, SUM, AVG, MIN, MAX, GROUP BY, HAVING)"
    echo "✅ Подзапросы (в WHERE, SELECT, FROM)"
    echo "✅ Оконные функции (ROW_NUMBER, RANK, LAG, LEAD)"
    echo "✅ Рекурсивные CTE"
    echo "✅ LATERAL JOIN"
    echo "✅ JSON функции"
    echo "✅ Математические функции"
    echo "✅ Сложные CASE выражения"
    echo "✅ Продвинутые агрегации (ROLLUP, CUBE, GROUPING SETS)"
    echo "✅ Сложные условия WHERE"
    echo "✅ Паттерн-матчинг (LIKE)"
    echo "✅ UNION и UNION ALL"
    echo "✅ EXISTS и NOT EXISTS"
    echo "✅ Коррелированные подзапросы"
    echo "✅ Self JOIN"
    echo "✅ Множественные JOIN условия"
    echo ""
    echo "=== ПРОВЕРЕННЫЕ АСПЕКТЫ ==="
    echo "✅ Парсинг SQL запросов"
    echo "✅ Обработка метаданных"
    echo "✅ Загрузка статистики"
    echo "✅ Cost-based оптимизация"
    echo "✅ Разбиение запросов"
    echo "✅ Создание подзапросов"
    echo "✅ Генерация результатов в JSON"
    echo "✅ Обработка ошибок"
    echo "✅ Производительность"
    echo "✅ Многопоточность"
    echo "✅ Использование памяти"
    echo "✅ Стресс-тестирование"
    exit 0
else
    print_error "Некоторые тесты не прошли. Проверьте логи выше."
    exit 1
fi




