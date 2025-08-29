SELECT 
    e.name as employee_name,
    d.name as department_name,
    COUNT(o.id) as order_count,
    AVG(o.amount) as avg_order_amount
FROM employees e
JOIN departments d ON e.department_id = d.id
LEFT JOIN orders o ON e.id = o.employee_id
WHERE e.salary > 50000
    AND o.order_date >= '2023-01-01'
GROUP BY e.id, e.name, d.name
HAVING COUNT(o.id) > 5
ORDER BY avg_order_amount DESC
LIMIT 100
