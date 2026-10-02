-- Script de creación de tablas - Sistema de Gestión de Biblioteca Escolar
-- Basado en el script del AVA. Ajustes (aprobados por el docente, documentados en el README):
--   * NOT NULL en las columnas obligatorias
--   * UNIQUE en categorias.nombre
--   * CHECK de stock no negativo en libros
--   * Columna prestamos.fecha_devolucion_real (NULL mientras no se devuelva)
--   * CHECK de coherencia entre devuelto y fecha_devolucion_real
--   * prestamos.fecha_devolucion se interpreta como fecha de vencimiento

CREATE DATABASE IF NOT EXISTS biblioteca;
USE biblioteca;

CREATE TABLE usuarios (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          nombre VARCHAR(100) NOT NULL,
                          rut VARCHAR(12) NOT NULL UNIQUE,
                          correo VARCHAR(100) NOT NULL,
                          contraseña VARCHAR(100) NOT NULL,  -- hash SHA-256 en hexadecimal (64 caracteres)
                          rol ENUM('bibliotecario', 'estudiante') NOT NULL
);

CREATE TABLE estudiantes (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             nombre VARCHAR(100) NOT NULL,
                             rut VARCHAR(12) NOT NULL UNIQUE,
                             curso VARCHAR(20),
                             correo VARCHAR(100) NOT NULL
);

CREATE TABLE categorias (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE libros (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        titulo VARCHAR(200) NOT NULL,
                        autor VARCHAR(100) NOT NULL,
                        isbn VARCHAR(20) NOT NULL UNIQUE,
                        editorial VARCHAR(100) NOT NULL,
                        stock INT NOT NULL,
                        id_categoria INT NOT NULL,
                        CONSTRAINT chk_libros_stock CHECK (stock >= 0),
                        FOREIGN KEY (id_categoria) REFERENCES categorias(id)
);

CREATE TABLE prestamos (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           id_estudiante INT NOT NULL,
                           id_libro INT NOT NULL,
                           fecha_prestamo DATE NOT NULL,
                           fecha_devolucion DATE NOT NULL,       -- fecha de vencimiento (préstamo + 7 días)
                           fecha_devolucion_real DATE NULL,      -- fecha en que se devolvió; NULL si sigue prestado
                           devuelto BOOLEAN NOT NULL DEFAULT FALSE,
                           CONSTRAINT chk_prestamos_devuelto CHECK (devuelto = (fecha_devolucion_real IS NOT NULL)),
                           FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id),
                           FOREIGN KEY (id_libro) REFERENCES libros(id)
);