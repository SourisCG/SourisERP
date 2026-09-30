-- Reference data: RBAC roles and the bootstrap administrator
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Full access to every module'),
    ('SALES', 'Customers, sales orders and sales reports'),
    ('WAREHOUSE', 'Products, warehouses, stock and receiving'),
    ('ACCOUNTANT', 'Invoicing, payments and financial reports'),
    ('VIEWER', 'Read-only access to the whole system');

-- Credentials (demo): admin / admin123
INSERT INTO users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
VALUES ('admin', 'admin@erp.local',
        '$2y$10$Pt7s57lMJ3ktJfpoL6.bS.uWK4xbI//g5kwVLJsp63bN0T9/rlMgm',
        'System', 'Administrator', TRUE, now(), now());

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin';
