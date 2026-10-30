CREATE DATABASE IF NOT EXISTS evaluador_creditos;
USE evaluador_creditos;

-- Tabla padre: atributos comunes
CREATE TABLE clientes (
	id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(100)  NOT NULL,
    ingresos   DECIMAL(12,2) NOT NULL,
    gastos     DECIMAL(12,2) NOT NULL,
    historial  BOOLEAN       NOT NULL);

-- Tabla hija: personas físicas
CREATE TABLE personas_fisicas (
    id_persona INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL UNIQUE,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE CASCADE);

-- Tabla hija: empresas
CREATE TABLE empresas (
    id_empresa INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL UNIQUE,
    rubro      VARCHAR(100) NOT NULL,
    director   VARCHAR(100) NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE CASCADE);

-- Tabla de acceso (independiente de la jerarquía)
CREATE TABLE usuarios (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(100) COLLATE utf8mb4_bin NOT NULL);

-- Usuario inicial para probar el login
INSERT INTO usuarios (nombre_usuario, clave) VALUES ('admin', 'admin123');