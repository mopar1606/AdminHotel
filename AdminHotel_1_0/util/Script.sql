CREATE DATABASE IF NOT EXISTS hotel_db;
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE hotel_db;

-- 1. Desactivar revisión de llaves foráneas para evitar errores de dependencia
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Borrado de tablas (en orden inverso a su creación preferiblemente)
DROP TABLE IF EXISTS prestamo_consumible;
DROP TABLE IF EXISTS parametro;
DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS permiso;
DROP TABLE IF EXISTS recibo;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS venta_detalle;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS consumible;
DROP TABLE IF EXISTS habitacion_movimiento;
DROP TABLE IF EXISTS habitacion;
DROP TABLE IF EXISTS tarifa_habitacion;
DROP TABLE IF EXISTS tipo_habitacion;
DROP TABLE IF EXISTS estado_habitacion;
DROP TABLE IF EXISTS metodo_pago;
DROP TABLE IF EXISTS concepto_venta;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS modulo;
DROP TABLE IF EXISTS usuario;
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

    FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
    UNIQUE KEY uk_cliente_documento (documento)
);

INSERT INTO cliente (nombre, documento, telefono, id_estado_registro) VALUES ('OCASIONAL', '123000', '123000', 1);
INSERT INTO cliente (nombre, documento, telefono, id_estado_registro) VALUES ('DUCHA', '456000', '456000', 1);
INSERT INTO cliente (nombre, documento, telefono, id_estado_registro) VALUES ('BANO', '789000', '789000', 1);
INSERT INTO cliente (nombre, documento, telefono, id_estado_registro) VALUES ('LAVANDERIA', '987000', '987000', 1);


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
	precio_sencilla decimal(10,2) DEFAULT '0.00',
    precio_doble decimal(10,2) DEFAULT '0.00',
	precio_tres decimal(10,2) DEFAULT '0.00',
    id_tipo_habitacion INT NOT NULL,
    id_estado_habitacion INT NOT NULL,
    id_estado_registro INT NOT NULL,
	
    UNIQUE KEY uk_numero_habitacion (numero_habitacion),
    KEY fk_habitacion_tipo (id_tipo_habitacion),
    KEY fk_habitacion_estado (id_estado_habitacion),
    KEY fk_habitacion_estado_registro (id_estado_registro),
    CONSTRAINT fk_habitacion_estado FOREIGN KEY (id_estado_habitacion) REFERENCES estado_habitacion (id_estado_habitacion),
    CONSTRAINT fk_habitacion_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro (id_estado_registro),
    CONSTRAINT fk_habitacion_tipo FOREIGN KEY (id_tipo_habitacion) REFERENCES tipo_habitacion (id_tipo_habitacion)
);

INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (101, 1, 'DOS CAMAS UNA DOBLE Y UNA SENCILLA BANO PRIVADO', 55000, 60000, 70000, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (102, 1, 'UNA CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (103, 1, 'UNA CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (104, 1, 'UNA CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (105, 1, 'UNA CAMA DOBLE BANO COMPARTIDO', 30000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (106, 1, 'UNA CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);

INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (201, 2, 'CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (202, 2, 'CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (203, 2, 'CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (204, 2, 'CAMA SENCILLA BANO COMPARTIDO', 30000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (205, 2, 'DOBLE CAMA SENCILLA BANO PRIVADO', 55000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (206, 2, 'CAMA DOBLE BANO COMPARTIDO', 30000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (207, 2, 'CAMA DOBLE BANO PRIVADO', 40000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (208, 2, 'CAMA DOBLE BANO PRIVADO', 40000, 50000, 0, 1, 1, 1);

INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (301, 3, 'CAMA DOBLE BANO COMPARTIDO', 33000, 40000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (302, 3, 'CAMA SENCILLA BANO COMPARTIDO', 30000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (303, 3, 'CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (304, 3, 'CAMA DOBLE BANO PRIVADO', 35000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (305, 3, 'CAMA DOBLE BANO COMPARTIDO', 30000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (306, 3, 'CAMA DOBLE Y CAMA SENCILLA BANO PRIVADO', 55000, 60000, 70000, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (307, 3, 'CAMA DOBLE Y CAMA SENCILLA BANO PRIVADO', 55000, 60000, 70000, 1, 1, 1);

INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (401, 4, 'CAMA DOBLE BANO PRIVADO', 40000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (403, 4, 'CAMA DOBLE BANO PRIVADO', 40000, 50000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (404, 4, 'CAMA DOBLE BANO PRIVADO', 55000, 0, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (405, 4, 'CAMA DOBLE BANO PRIVADO', 35000, 40000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (406, 4, 'CAMA DOBLE BANO PRIVADO', 40000, 50000, 0, 1, 1, 1);

INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (501, 5, 'CAMA DOBLE BANO PRIVADO', 45000, 55000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (502, 5, 'CAMA DOBLE BANO PRIVADO', 45000, 55000, 0, 1, 1, 1);
INSERT INTO habitacion (numero_habitacion, piso, descripcion, precio_sencilla, precio_doble, precio_tres, id_tipo_habitacion, id_estado_habitacion, id_estado_registro) VALUES (503, 5, 'CAMA DOBLE BANO PRIVADO', 45000, 55000, 0, 1, 1, 1);

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
	codigo_barras varchar(50) DEFAULT NULL,
    id_estado_registro INT NOT NULL,
	
	CONSTRAINT fk_producto_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
    CONSTRAINT chk_producto_precio_compra CHECK (precio_compra >= 0),
    CONSTRAINT chk_producto_precio_venta CHECK (precio_venta >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    UNIQUE KEY uk_producto_nombre (nombre),
	UNIQUE KEY uk_producto_codigo_barras (codigo_barras),
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
('VENTA MOSTRADOR', 1),
('MULTAS', 1),
('SERVICIO', 1);

--Tabla de Ventas
CREATE TABLE venta (
  id_venta int NOT NULL AUTO_INCREMENT,
  id_cliente int DEFAULT NULL,
  id_habitacion int DEFAULT NULL,
  id_concepto_venta int NOT NULL,
  fecha datetime NOT NULL,
  total decimal(10,2) DEFAULT '0.00',
  observacion varchar(255) DEFAULT NULL,
  id_estado_registro int NOT NULL,
  
  PRIMARY KEY (id_venta),
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
  CONSTRAINT fk_venta_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion (id_habitacion),
  CONSTRAINT fk_venta_concepto FOREIGN KEY (id_concepto_venta) REFERENCES concepto_venta (id_concepto_venta),
  CONSTRAINT fk_venta_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro (id_estado_registro),
  KEY idx_venta_cliente (id_cliente),
  KEY idx_venta_habitacion (id_habitacion),
  KEY idx_venta_concepto (id_concepto_venta),
  KEY idx_venta_estado_registro (id_estado_registro)
);

--Tabla Detalles de las Ventas
CREATE TABLE venta_detalle (
    id_venta_detalle INT AUTO_INCREMENT,
    id_venta INT NOT NULL,
    tipo_item VARCHAR(20) NOT NULL,
    id_referencia INT NOT NULL,
    cantidad INT DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    id_estado_registro INT NOT NULL,
	
	PRIMARY KEY (id_venta_detalle),
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT fk_detalle_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
    INDEX idx_detalle_venta (id_venta),
    INDEX idx_detalle_estado_registro (id_estado_registro)
);

--Tabla Pago de la Venta
CREATE TABLE pago (
    id_pago INT AUTO_INCREMENT,
    id_venta INT NOT NULL,
    fecha DATETIME NOT NULL,
    id_metodo INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    id_estado_registro INT NOT NULL,
	
	PRIMARY KEY (id_pago),
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
	id_habitacion_movimiento int NOT NULL,
	noches int NOT NULL,  
	fecha_emision datetime NOT NULL,
	
	PRIMARY KEY (id_recibo),
	CONSTRAINT fk_recibo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
	CONSTRAINT fk_recibo_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),
	CONSTRAINT fk_recibo_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
	KEY fk_recibo_cliente (id_cliente),
    KEY fk_recibo_habitacion (id_habitacion),
    KEY fk_recibo_venta (id_venta)
);

--Tabla de Usuarios
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT,
    nombre_usuario VARCHAR(50) NOT NULL,
    clave VARCHAR(255) NOT NULL, -- Guardarás el Hash aquí
    nombre_completo VARCHAR(100),
    id_estado_registro INT NOT NULL DEFAULT 1,
	
	PRIMARY KEY (id_usuario),
    UNIQUE KEY nombre_usuario (nombre_usuario),
    KEY fk_user_estado (id_estado_registro),
    CONSTRAINT fk_user_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO usuario (nombre_usuario, clave, nombre_completo, id_estado_registro) values ('ADMIN', '@12345@', 'ADMIN', 1);
INSERT INTO usuario (nombre_usuario, clave, nombre_completo, id_estado_registro) values ('USUARIO', '12345', 'ADMIN', 1);
INSERT INTO usuario (nombre_usuario, clave, nombre_completo, id_estado_registro) values ('RECEPCION', '12345', 'ADMIN', 1);
INSERT INTO usuario (nombre_usuario, clave, nombre_completo, id_estado_registro) values ('ADMINISTRADOR', '5050', 'ADMIN', 1);

--Tabla de Módulos (Las puertas del sistema)
CREATE TABLE modulo (
    id_modulo INT AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    id_estado_registro INT NOT NULL DEFAULT 1,
	
	
	PRIMARY KEY (id_modulo),
    KEY fk_modulo_estado (id_estado_registro),
    CONSTRAINT fk_modulo_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('INVENTARIO', 'Control del inventario', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('CONTROL HABITACIONES', 'Control y Asignacion de las Habitaciones', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('VENTAS VITRINA', 'Control ventas de vitrina', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) values ('ADMIN USUARIOS', 'Administracion de Usuarios', 1);
INSERT INTO modulo (nombre, descripcion, id_estado_registro) VALUES ('CONSUMIBLES', 'Control de consumibles del hotel', 1);

-- Tabla de Permisos (Relación Usuario-Módulo)
-- Si un usuario tiene el registro aquí, tiene acceso al módulo
CREATE TABLE permiso (
    id_permiso INT AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_modulo INT NOT NULL,
	id_estado_registro INT NOT NULL DEFAULT 1,
	
	PRIMARY KEY (id_permiso),
    KEY fk_permiso_usuario (id_usuario),
    KEY fk_permiso_modulo (id_modulo),
    KEY fk_permiso_estado (id_estado_registro),
    CONSTRAINT fk_permiso_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_permiso_modulo FOREIGN KEY (id_modulo) REFERENCES modulo(id_modulo),
	CONSTRAINT fk_permiso_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (2, 1, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (2, 2, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (2, 3, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (2, 4, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (2, 5, 1);

INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (3, 2, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (3, 3, 1);

INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (4, 1, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (4, 2, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (4, 3, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (4, 4, 1);
INSERT INTO permiso (id_usuario, id_modulo, id_estado_registro) values (4, 5, 1);

--Tabla de Auditoría (La "caja negra" del hotel)
CREATE TABLE auditoria (
    id_auditoria INT AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    evento VARCHAR(100) NOT NULL, -- Ejemplo: 'LOGIN', 'INSERT_PRODUCTO', 'UPDATE_PRECIO'
    tabla_afectada VARCHAR(50),    -- Ejemplo: 'producto', 'venta'
    descripcion TEXT,             -- Ejemplo: 'Cambió precio de $500 a $800'
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_maquina VARCHAR(45),       -- Opcional, para saber desde qué PC lo hicieron
	
	PRIMARY KEY (id_auditoria),
    KEY fk_auditoria_usuario (id_usuario),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

--Tabla para conrol de consumibles del hotel
CREATE TABLE consumible (
    id_consumible INT AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
	precio_compra decimal(10,2) NOT NULL,
    precio_multa decimal(10,2) NOT NULL DEFAULT '0.00',
    fecha_compra date DEFAULT NULL,
    stock int NOT NULL DEFAULT '0',
    id_estado_registro int NOT NULL DEFAULT '1',
    es_prestable tinyint(1) NOT NULL DEFAULT '0',
	
    PRIMARY KEY (id_consumible),
    UNIQUE KEY nombre (nombre),
    KEY fk_consumible_estado (id_estado_registro),
    CONSTRAINT fk_consumible_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
    CONSTRAINT chk_consumible_stock CHECK (stock >= 0)
);

--Tabla de parametros estaticos a nivel del negocio
CREATE TABLE parametro (
  id_parametro int NOT NULL AUTO_INCREMENT,
  clave varchar(50) NOT NULL,
  valor decimal(10,2) NOT NULL,
  descripcion varchar(255) DEFAULT NULL,
  id_estado_registro int NOT NULL,
  
  PRIMARY KEY (id_parametro),
  UNIQUE KEY clave (clave),
  KEY idx_parametro_clave (clave),
  KEY fk_parametro_estado_registro (id_estado_registro),
  CONSTRAINT fk_parametro_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro (id_estado_registro)
);

INSERT INTO parametro (clave, valor, descripcion, id_estado_registro) VALUES ('TARIFA_PERSONA_ADICIONAL', 10000.00, 'Cobro por persona adicional en habitacion ocupada', 1);
INSERT INTO parametro (clave, valor, descripcion, id_estado_registro) VALUES ('TARIFA_RATO', 30000, 'Cobro por rato en habitacion', 1);
INSERT INTO parametro (clave, valor, descripcion, id_estado_registro) VALUES ('TARIFA_DUCHA', 13000, 'Cobro de una ducha', 1);
INSERT INTO parametro (clave, valor, descripcion, id_estado_registro) VALUES ('TARIFA_BANO', 2000, 'Cobro de uso de bano', 1);

--Tabla de registro de articulos prestados para uso durante la estadia
CREATE TABLE prestamo_consumible (
  id_prestamo int NOT NULL AUTO_INCREMENT,
  id_habitacion_movimiento int NOT NULL,
  id_consumible int NOT NULL,
  cantidad_entregada int NOT NULL,
  cantidad_devuelta int DEFAULT '0',
  estado varchar(20) DEFAULT 'PRESTADO',
  id_estado_registro int NOT NULL,
  
  PRIMARY KEY (id_prestamo),
  KEY fk_prestamo_consumible_estado_registro (id_estado_registro),
  KEY fk_prestamo_movimiento (id_habitacion_movimiento),
  KEY fk_prestamo_consumible (id_consumible),
  CONSTRAINT fk_prestamo_consumible FOREIGN KEY (id_consumible) REFERENCES consumible (id_consumible),
  CONSTRAINT fk_prestamo_consumible_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro (id_estado_registro),
  CONSTRAINT fk_prestamo_movimiento FOREIGN KEY (id_habitacion_movimiento) REFERENCES habitacion_movimiento (id_habitacion_movimiento)
);

CREATE TABLE caja (
    id_caja INT AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    id_concepto_venta INT NOT NULL,
    id_estado_registro INT NOT NULL,
	
    PRIMARY KEY (id_caja),
    UNIQUE KEY uk_caja_nombre (nombre),
    CONSTRAINT fk_caja_concepto FOREIGN KEY (id_concepto_venta) REFERENCES concepto_venta(id_concepto_venta),
    CONSTRAINT fk_caja_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);
INSERT INTO caja (nombre, id_concepto_venta, id_estado_registro) VALUES ('CAJA HOTEL', 1, 1);
INSERT INTO caja (nombre, id_concepto_venta, id_estado_registro) VALUES ('CAJA MOSTRADOR', 2, 1);

CREATE TABLE turno_caja (
    id_turno_caja INT AUTO_INCREMENT,
    id_caja INT NOT NULL,
    id_usuario INT NOT NULL,
    fecha_apertura DATETIME NOT NULL,
    base_inicial_efectivo DECIMAL(10,2) NOT NULL,
    observacion_apertura VARCHAR(255) DEFAULT NULL,
    fecha_cierre DATETIME DEFAULT NULL,
    estado VARCHAR(20) DEFAULT 'ABIERTA',
    observacion_cierre VARCHAR(255) DEFAULT NULL,
    id_estado_registro INT NOT NULL,
    turno_diurno tinyint(1) DEFAULT '1' COMMENT '1=Diurno (7AM-7PM), 0=Nocturno (7PM-7AM)',
    forzado_cierre tinyint(1) DEFAULT '0' COMMENT '1=Cierre forzado por sistema, 0=Cierre normal',
	
    PRIMARY KEY (id_turno_caja),
    UNIQUE KEY uk_turno_activo (id_caja, estado),
    CONSTRAINT fk_turno_caja FOREIGN KEY (id_caja) REFERENCES caja(id_caja),
    CONSTRAINT fk_turno_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_turno_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

CREATE TABLE turno_caja_detalle (
    id_turno_detalle INT AUTO_INCREMENT,
    id_turno_caja INT NOT NULL,
    id_metodo INT NOT NULL,
    total_ingresos DECIMAL(10,2) DEFAULT '0.00',
    total_egresos DECIMAL(10,2) DEFAULT '0.00',
    monto_esperado DECIMAL(10,2) DEFAULT '0.00',
    monto_real DECIMAL(10,2) DEFAULT '0.00',
    diferencia DECIMAL(10,2) DEFAULT '0.00',

    PRIMARY KEY (id_turno_detalle),
    UNIQUE KEY uk_detalle_turno_metodo (id_turno_caja, id_metodo),
    CONSTRAINT fk_detalle_turno FOREIGN KEY (id_turno_caja) REFERENCES turno_caja(id_turno_caja),
    CONSTRAINT fk_detalle_metodo FOREIGN KEY (id_metodo) REFERENCES metodo_pago(id_metodo)
);

CREATE TABLE gasto_caja (
    id_gasto INT AUTO_INCREMENT,
    id_turno_caja INT NOT NULL,
    id_usuario INT NOT NULL,
    concepto VARCHAR(100) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    fecha DATETIME NULL,
    id_estado_registro INT NOT NULL,
	
	PRIMARY KEY (id_gasto),
	CONSTRAINT chk_gasto_valor CHECK (valor > 0),
    CONSTRAINT fk_gasto_turno FOREIGN KEY (id_turno_caja) REFERENCES turno_caja(id_turno_caja),
    CONSTRAINT fk_gasto_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_gasto_estado FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro)
);

INSERT INTO parametro (clave, valor, descripcion, id_estado_registro) 
VALUES ('BASE_CAJA_HOTEL', 150000.00, 'Dinero base inicial para la caja de habitaciones', 1);
INSERT INTO parametro (clave, valor, descripcion, id_estado_registro) 
VALUES ('BASE_CAJA_MOSTRADOR', 50000.00, 'Dinero base inicial para la caja de la vitrina', 1);

ALTER TABLE pago 
ADD COLUMN id_turno_caja INT NULL AFTER id_estado_registro,
ADD CONSTRAINT fk_pago_turno FOREIGN KEY (id_turno_caja) 
    REFERENCES turno_caja(id_turno_caja);
	
	
SELECT 
    c.nombre                          AS caja,
    mp.descripcion                    AS metodo_pago,
    SUM(p.valor)                      AS total_acumulado
FROM pago p
    INNER JOIN turno_caja tc ON p.id_turno_caja = tc.id_turno_caja
    INNER JOIN caja c ON tc.id_caja = c.id_caja
    INNER JOIN metodo_pago mp ON p.id_metodo = mp.id_metodo
WHERE 
    tc.estado = 'ABIERTA'
    AND p.id_estado_registro = 1
GROUP BY c.id_caja, mp.id_metodo;