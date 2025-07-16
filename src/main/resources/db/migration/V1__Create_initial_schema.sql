-- Crear esquema inicial para la plataforma de torneos
-- V1__Create_initial_schema.sql

-- Tabla de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'PARTICIPANT',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de categorías de juegos
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    description VARCHAR(100) NOT NULL,
    alias VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de tipos de juegos
CREATE TABLE game_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    players_count INTEGER NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de torneos
CREATE TABLE tournaments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id BIGINT REFERENCES categories(id),
    game_type_id BIGINT REFERENCES game_types(id),
    organizer_id BIGINT REFERENCES users(id),
    is_free BOOLEAN DEFAULT false,
    price DECIMAL(10,2) DEFAULT 0.00,
    max_participants INTEGER NOT NULL,
    current_participants INTEGER DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    commission_percentage DECIMAL(5,2) DEFAULT 5.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de tickets
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    tournament_id BIGINT REFERENCES tournaments(id),
    qr_code VARCHAR(255) UNIQUE NOT NULL,
    unique_code VARCHAR(50) UNIQUE NOT NULL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    price DECIMAL(10,2) NOT NULL,
    service_fee DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de subadministradores de torneos
CREATE TABLE tournament_subadmins (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT REFERENCES tournaments(id),
    user_id BIGINT REFERENCES users(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id),
    UNIQUE(tournament_id, user_id)
);

-- Tabla de notificaciones
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT REFERENCES tournaments(id),
    user_id BIGINT REFERENCES users(id),
    message TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT false,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Tabla de integraciones de streaming
CREATE TABLE streaming_integrations (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT REFERENCES tournaments(id),
    platform VARCHAR(20) NOT NULL, -- TWITCH, DISCORD, ZOOM, MEET
    stream_url VARCHAR(500),
    stream_key VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de etapas de venta de tickets
CREATE TABLE ticket_sale_stages (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT REFERENCES tournaments(id),
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    max_tickets INTEGER,
    sold_tickets INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de auditoría de acciones
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX idx_tournaments_organizer ON tournaments(organizer_id);
CREATE INDEX idx_tournaments_category ON tournaments(category_id);
CREATE INDEX idx_tournaments_status ON tournaments(status);
CREATE INDEX idx_tournaments_dates ON tournaments(start_date, end_date);
CREATE INDEX idx_tickets_tournament ON tickets(tournament_id);
CREATE INDEX idx_tickets_user ON tickets(user_id);
CREATE INDEX idx_tickets_qr_code ON tickets(qr_code);
CREATE INDEX idx_notifications_tournament ON notifications(tournament_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

-- Trigger para actualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tournaments_updated_at BEFORE UPDATE ON tournaments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_streaming_integrations_updated_at BEFORE UPDATE ON streaming_integrations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Datos iniciales
INSERT INTO categories (code, description, alias) VALUES
('MOBA', 'Multiplayer Online Battle Arena', 'MOBA'),
('FPS', 'First Person Shooter', 'FPS'),
('RTS', 'Real Time Strategy', 'RTS'),
('FIGHTING', 'Fighting Games', 'FIGHTING'),
('SPORTS', 'Sports Games', 'SPORTS'),
('CARD', 'Card Games', 'CARD');

INSERT INTO game_types (code, full_name, players_count, category_id) VALUES
('LOL', 'League of Legends', 5, 1),
('DOTA2', 'Dota 2', 5, 1),
('CSGO', 'Counter-Strike: Global Offensive', 5, 2),
('VALORANT', 'Valorant', 5, 2),
('SC2', 'StarCraft II', 1, 3),
('SF6', 'Street Fighter 6', 2, 4),
('FIFA24', 'FIFA 24', 2, 5),
('HS', 'Hearthstone', 1, 6); 