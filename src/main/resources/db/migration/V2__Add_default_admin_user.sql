-- Agregar usuario administrador por defecto
-- Contraseña: admin123 (hasheada con BCrypt)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at)
VALUES (
    'admin',
    'admin@tournament.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- admin123
    'Administrador',
    'Sistema',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Agregar algunos usuarios de prueba
INSERT INTO users (username, email, password_hash, first_name, last_name, role, is_active, created_at, updated_at)
VALUES 
(
    'organizador1',
    'organizador1@tournament.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- admin123
    'Juan',
    'Organizador',
    'SUBADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'participante1',
    'participante1@tournament.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- admin123
    'María',
    'Participante',
    'PARTICIPANT',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'participante2',
    'participante2@tournament.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- admin123
    'Carlos',
    'Gamer',
    'PARTICIPANT',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
); 