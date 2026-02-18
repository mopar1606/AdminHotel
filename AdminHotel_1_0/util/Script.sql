CREATE DATABASE IF NOT EXISTS hotel_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE hotel_db;

CREATE TABLE estado_registro (
    id_estado_registro INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(30) NOT NULL
);

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
(numero_habitacion, piso, id_tipo_habitacion, id_estado_habitacion, id_estado_registro, descripcion)
VALUES
(101, 1, 2, 1, 1, 'DOS CAMAS UNA DOBLE Y UNA SENCILLA BAÑO PRIVADO'),
(102, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO'),
(103, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO'),
(104, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO'),
(105, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO COMPARTIDO'),
(106, 1, 1, 1, 1, 'UNA CAMA DOBLE BAÑO PRIVADO'),

(201, 2, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(202, 2, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(203, 2, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(204, 2, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),
(205, 2, 2, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(206, 2, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),

(301, 3, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(302, 3, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(303, 3, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(304, 3, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),
(305, 3, 2, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(306, 3, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),
(307, 3, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),

(401, 4, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(403, 4, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(404, 4, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),
(405, 4, 2, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(406, 4, 1, 1, 1, 'CAMA DOBLE BAÑO COMPARTIDO'),

(501, 5, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(502, 5, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO'),
(503, 5, 1, 1, 1, 'CAMA DOBLE BAÑO PRIVADO');

CREATE TABLE habitacion_movimiento (
    id_habitacion_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_habitacion INT NOT NULL,
    id_cliente INT NOT NULL,
    fecha_entrada DATETIME NOT NULL,
    fecha_salida DATETIME NULL,
	id_estado_registro INT NOT NULL,

    CONSTRAINT fk_mov_habitacion FOREIGN KEY (id_habitacion) REFERENCES habitacion(id_habitacion),
    CONSTRAINT fk_mov_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
	CONSTRAINT fk_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro(id_estado_registro),
	
	INDEX idx_mov_habitacion (id_habitacion),
    INDEX idx_mov_cliente (id_cliente),
    INDEX idx_mov_fecha_entrada (fecha_entrada),
    INDEX idx_mov_estado_registro (id_estado_registro)
);

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

CREATE TABLE venta (
  id_venta int NOT NULL AUTO_INCREMENT,
  id_cliente int DEFAULT NULL,
  fecha datetime DEFAULT CURRENT_TIMESTAMP,
  total decimal(10,2) DEFAULT '0.00',
  observacion varchar(255) DEFAULT NULL,
  id_estado_registro int NOT NULL,
  
  PRIMARY KEY (id_venta),
  
  KEY idx_venta_cliente (id_cliente),
  KEY idx_venta_estado_registro (id_estado_registro),
  
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
  CONSTRAINT fk_venta_estado_registro FOREIGN KEY (id_estado_registro) REFERENCES estado_registro (id_estado_registro)
);

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

UPDATE producto
SET stock = stock - 1
WHERE id = ?;




ALTER TABLE habitacion ADD COLUMN precio DECIMAL(10,2) NULL DEFAULT 0.00 AFTER descripcion;