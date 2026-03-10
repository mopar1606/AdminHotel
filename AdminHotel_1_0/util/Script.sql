CREATE DATABASE IF NOT EXISTS hotel_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE hotel_db;

-- 1. Desactivar revisión de llaves foráneas para evitar errores de dependencia
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Borrado de tablas (en orden inverso a su creación preferiblemente)
DROP TABLE IF EXISTS recibo;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS venta_detalle;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS habitacion_movimiento;
DROP TABLE IF EXISTS habitacion;
DROP TABLE IF EXISTS tarifa_habitacion;
DROP TABLE IF EXISTS tipo_habitacion;
DROP TABLE IF EXISTS estado_habitacion;
DROP TABLE IF EXISTS metodo_pago;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS estado_registro;

-- 3. Reactivar revisión de llaves foráneas
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE estado_registro (
    id_estado_registro INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL
);

--Tabla control registro
INSERT INTO estado_registro (descripcion)
VALUES ('ACTIVO'), ('INACTIVO'), ('ELIMINADO');

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(400) NOT NULL,
    documento VARCHAR(50) NOT NULL,
    telefono VARCHAR(30),
    email VARCHAR(200),

    id_estado_registro INT NOT NULL,

    FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

--Tabla Estado Habitacion
CREATE TABLE estado_habitacion (
    id_estado_habitacion INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL,	
	id_estado_registro INT NOT NULL,

    FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO estado_habitacion (descripcion, id_estado_registro)
VALUES
('DISPONIBLE', 1),
('OCUPADA', 1),
('POR ASEO', 1),
('NO DISPONIBLE', 1);

--Tabla Tipo Habitacion
CREATE TABLE tipo_habitacion (
    id_tipo_habitacion INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL,
    capacidad INT NOT NULL,
    id_estado_registro INT NOT NULL,

    CONSTRAINT fk_tipo_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO tipo_habitacion (descripcion, capacidad, id_estado_registro)
VALUES
('SENCILLA', 1, 1),
('DOBLE', 2, 1);

--Tabla Tarifa Habitacion
CREATE TABLE tarifa_habitacion (
    id_tarifa INT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_habitacion INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NULL,
    fecha_fin DATE NULL,
    id_estado_registro INT NOT NULL,

    CONSTRAINT fk_tarifa_tipo FOREIGN KEY (id_tipo_habitacion) REFERENCES tipo_habitacion(id_tipo_habitacion),
    CONSTRAINT fk_tarifa_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

--Tabla Habitacion
CREATE TABLE habitacion (
    id_habitacion INT AUTO_INCREMENT PRIMARY KEY,
    numero_habitacion INT NOT NULL,
    piso INT NOT NULL,
	descripcion varchar(500) DEFAULT NULL,
	precio DECIMAL(10,2) NULL DEFAULT 0.00,
    id_tipo_habitacion INT NOT NULL,
    id_estado_habitacion INT NOT NULL,
    id_estado_registro INT NOT NULL,

    UNIQUE KEY uk_numero_habitacion (numero_habitacion),
    CONSTRAINT fk_habitacion_tipo FOREIGN KEY (id_tipo_habitacion) REFERENCES tipo_habitacion(id_tipo_habitacion),
    CONSTRAINT fk_habitacion_estado FOREIGN KEY (id_estado_habitacion) REFERENCES estado_habitacion(id_estado_habitacion),
    CONSTRAINT fk_habitacion_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO habitacion
(numero_habitacion, piso, id_tipo_habitacion, id_estado_habitacion, id_estado_registro, descripcion, precio)
VALUES
(101, 1, 2, 1, 1, 'DOS CAMAS UNA DOBLE Y UNA SENCILLA BAÑO PRIVADO',11000),
(102, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO',12000),
(103, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO',13000),
(104, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO',14000),
(105, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO COMPARTIDO',150000),
(106, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO',16000),

(201, 2, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',21000),
(202, 2, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',22000),
(203, 2, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',23000),
(204, 2, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',24000),
(205, 2, 2, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',25000),
(206, 2, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',26000),

(301, 3, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',31000),
(302, 3, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',32000),
(303, 3, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',33000),
(304, 3, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',34000),
(305, 3, 2, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',35000),
(306, 3, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',36000),
(307, 3, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',37000),

(401, 4, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',41000),
(403, 4, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',43000),
(404, 4, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',44000),
(405, 4, 2, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',45000),
(406, 4, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO',46000),

(501, 5, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',51000),
(502, 5, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',52000),
(503, 5, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO',53000);

--Tabla Historial Movimiento Habitacion
CREATE TABLE habitacion_movimiento (
    id_habitacion_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_habitacion INT NOT NULL,
    id_cliente INT NOT NULL,
    fecha_entrada DATETIME NOT NULL,
	noches int NOT NULL,
	fecha_salida_prevista datetime NOT NULL,
    fecha_salida DATETIME DEFAULT NULL,
	id_estado_registro INT NOT NULL,

    CONSTRAINT fk_mov_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),
    CONSTRAINT fk_mov_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
	CONSTRAINT fk_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
	
	INDEX idx_mov_habitacion (id_habitacion),
    INDEX idx_mov_cliente (id_cliente),
    INDEX idx_mov_fecha_entrada (fecha_entrada),
    INDEX idx_mov_estado_registro (id_estado_registro)
);

--Tabla Productos de vitrina
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    id_estado_registro INT NOT NULL,
	
	CONSTRAINT fk_producto_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
    CONSTRAINT chk_producto_precio_compra CHECK (precio_compra >= 0),
    CONSTRAINT chk_producto_precio_venta CHECK (precio_venta >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    UNIQUE KEY uk_producto_nombre (nombre),
    INDEX idx_producto_estado_registro (id_estado_registro)
);

--Tabla Metodos de Pago
CREATE TABLE metodo_pago (
    id_metodo INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL,
    id_estado_registro INT NOT NULL,

    CONSTRAINT fk_metodo_pago_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
    UNIQUE KEY uk_metodo_pago_descripcion (descripcion),
    INDEX idx_metodo_pago_estado_registro (id_estado_registro)
);

INSERT INTO metodo_pago (descripcion, id_estado_registro)
VALUES
('EFECTIVO', 1),
('NEQUI', 1),
('DAVIPLATA', 1);

--Tabla Concepto De Venta
CREATE TABLE concepto_venta (
    id_concepto_venta INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL,
    id_estado_registro INT NOT NULL,
    
    CONSTRAINT fk_concepto_estado_registro FOREIGN KEY (id_estado_registro) 
        REFERENCES estado_registro(id_estado_registro)
);

-- Insertar los conceptos base
INSERT INTO concepto_venta (descripcion, id_estado_registro)
VALUES 
('VENTA HABITACION', 1),
('VENTA MOSTRADOR', 1);

--Tabla de Ventas
CREATE TABLE venta (
  id_venta int NOT NULL AUTO_INCREMENT,
  id_cliente int DEFAULT NULL,
  id_concepto_venta int NOT NULL,
  fecha datetime DEFAULT CURRENT_TIMESTAMP,
  total decimal(10,2) DEFAULT '0.00',
  observacion varchar(255) DEFAULT NULL,
  id_estado_registro int NOT NULL,
  
  PRIMARY KEY (id_venta),
  
  -- Relaciones y llaves
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
  CONSTRAINT fk_venta_concepto FOREIGN KEY (id_concepto_venta) REFERENCES concepto_venta (id_concepto_venta), -- <--- NUEVA RELACIÓN
  CONSTRAINT fk_venta_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro (id_estado_registro),
  
  -- Índices para que las consultas sean rápidas
  KEY idx_venta_cliente (id_cliente),
  KEY idx_venta_concepto (id_concepto_venta),
  KEY idx_venta_estado_registro (id_estado_registro)
);

--Tabla Detalles de las Ventas
CREATE TABLE venta_detalle (
    id_venta_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    tipo_item VARCHAR(20) NOT NULL,
    id_referencia INT NOT NULL,
    cantidad INT DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    id_estado_registro INT NOT NULL,

    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT fk_detalle_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),

    INDEX idx_detalle_venta (id_venta),
    INDEX idx_detalle_estado_registro (id_estado_registro)
);

--Tabla Pago de la Venta
CREATE TABLE pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_metodo INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    id_estado_registro INT NOT NULL,

    CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT fk_pago_metodo FOREIGN KEY (id_metodo) REFERENCES metodo_pago(id_metodo),
    CONSTRAINT fk_pago_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),

    INDEX idx_pago_venta (id_venta),
    INDEX idx_pago_metodo (id_metodo),
    INDEX idx_pago_estado_registro (id_estado_registro)
);

--Tabla de Recibo de las ventas
CREATE TABLE recibo (
  id_recibo int NOT NULL AUTO_INCREMENT,
  id_cliente int NOT NULL,
  id_habitacion int NOT NULL,
  id_pago int NOT NULL,
  id_venta int NOT NULL,
  id_habitacionMovimiento int NOT NULL,
  noches int NOT NULL,  
  fecha_emision datetime NOT NULL,
  PRIMARY KEY (id_recibo),
  
	CONSTRAINT fk_recibo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
	CONSTRAINT fk_recibo_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),
	CONSTRAINT fk_recibo_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
);

--Tabla de Usuarios
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL, -- Guardarás el Hash aquí
    nombre_completo VARCHAR(100),
    id_estado_registro INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_user_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);
INSERT INTO usuario (nombre_usuario, clave, nombre_completo, id_estado_registro) values ('ADMIN', '@12345@', 'ADMIN', 1);

--Tabla de Módulos (Las puertas del sistema)
CREATE TABLE modulo (
    id_modulo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    id_estado_registro INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_modulo_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('INVENTARIO', 'Control del inventario', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('CONTROL HABITACIONES', 'Control y Asignacion de las Habitaciones', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('VENTAS VITRINA', 'Control ventas de vitrina', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('ADMIN USUARIOS', 'Administracion de Usuarios', 1);

-- Tabla de Permisos (Relación Usuario-Módulo)
-- Si un usuario tiene el registro aquí, tiene acceso al módulo
CREATE TABLE permiso (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_modulo INT NOT NULL,
	id_estado_registro INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_permiso_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_permiso_modulo FOREIGN KEY (id_modulo) REFERENCES modulo(id_modulo),
	CONSTRAINT fk_permiso_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

--Tabla de Auditoría (La "caja negra" del hotel)
CREATE TABLE auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    evento VARCHAR(100) NOT NULL, -- Ejemplo: 'LOGIN', 'INSERT_PRODUCTO', 'UPDATE_PRECIO'
    tabla_afectada VARCHAR(50),    -- Ejemplo: 'producto', 'venta'
    descripcion TEXT,             -- Ejemplo: 'Cambió precio de $500 a $800'
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_maquina VARCHAR(45),       -- Opcional, para saber desde qué PC lo hicieron
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);