-- =====================================================================
-- GesMant - Sistema de gestion de ordenes de trabajo para mantenimiento
-- Script 01: creacion de la base de datos y de las tablas (MySQL 8)
-- Autor: Gabriel Fernando Mina
-- =====================================================================

CREATE DATABASE IF NOT EXISTS gesmant
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gesmant;

-- Se borran en orden inverso a las dependencias para poder re-ejecutar el script
DROP TABLE IF EXISTS dependencia_orden;
DROP TABLE IF EXISTS evidencia_foto;
DROP TABLE IF EXISTS material_utilizado;
DROP TABLE IF EXISTS registro_llaves;
DROP TABLE IF EXISTS item_presupuesto;
DROP TABLE IF EXISTS presupuesto;
DROP TABLE IF EXISTS orden_trabajo;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS tecnico;
DROP TABLE IF EXISTS unidad_funcional;
DROP TABLE IF EXISTS consorcio;
DROP TABLE IF EXISTS administracion;

-- Administracion que gestiona los consorcios
CREATE TABLE administracion (
  id_administracion INT AUTO_INCREMENT PRIMARY KEY,
  nombre            VARCHAR(100) NOT NULL,
  cuit              VARCHAR(13)  NOT NULL UNIQUE,
  telefono          VARCHAR(30)
) ENGINE=InnoDB;

-- Edificio administrado
CREATE TABLE consorcio (
  id_consorcio      INT AUTO_INCREMENT PRIMARY KEY,
  id_administracion INT          NOT NULL,
  nombre            VARCHAR(100) NOT NULL,
  direccion         VARCHAR(150) NOT NULL,
  FOREIGN KEY (id_administracion) REFERENCES administracion(id_administracion)
) ENGINE=InnoDB;

-- Departamento particular o espacio comun dentro de un consorcio
CREATE TABLE unidad_funcional (
  id_unidad          INT AUTO_INCREMENT PRIMARY KEY,
  id_consorcio       INT         NOT NULL,
  numero             VARCHAR(20) NOT NULL,
  piso               VARCHAR(10),
  es_espacio_comun   BOOLEAN     NOT NULL DEFAULT FALSE,
  nombre_inquilino   VARCHAR(100),
  telefono_inquilino VARCHAR(30),
  UNIQUE (id_consorcio, numero),
  FOREIGN KEY (id_consorcio) REFERENCES consorcio(id_consorcio)
) ENGINE=InnoDB;

-- Persona que ejecuta los trabajos
CREATE TABLE tecnico (
  id_tecnico   INT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(100) NOT NULL,
  especialidad VARCHAR(60)  NOT NULL,
  telefono     VARCHAR(30),
  activo       BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- Usuarios del sistema. El rol define que puede hacer cada uno.
CREATE TABLE usuario (
  id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
  nombre_usuario VARCHAR(30)  NOT NULL UNIQUE,
  clave          VARCHAR(60)  NOT NULL,
  rol            VARCHAR(15)  NOT NULL,
  id_tecnico     INT NULL,
  CHECK (rol IN ('ADMINISTRADOR', 'TECNICO')),
  FOREIGN KEY (id_tecnico) REFERENCES tecnico(id_tecnico)
) ENGINE=InnoDB;

-- Registro central: una tarea desde que se origina hasta que se cierra
CREATE TABLE orden_trabajo (
  id_orden              INT AUTO_INCREMENT PRIMARY KEY,
  codigo                VARCHAR(10)  NOT NULL UNIQUE,
  id_unidad             INT          NOT NULL,
  id_tecnico            INT NULL,
  descripcion_problema  VARCHAR(255) NOT NULL,
  prioridad             VARCHAR(5)   NOT NULL,
  estado                VARCHAR(15)  NOT NULL DEFAULT 'REGISTRADA',
  requiere_coordinacion BOOLEAN      NOT NULL DEFAULT FALSE,
  diagnostico           VARCHAR(255),
  trabajo_realizado     VARCHAR(255),
  fecha_creacion        DATETIME     NOT NULL,
  fecha_inicio          DATETIME NULL,
  fecha_fin             DATETIME NULL,
  CHECK (prioridad IN ('ALTA', 'MEDIA', 'BAJA')),
  CHECK (estado IN ('REGISTRADA', 'ASIGNADA', 'PRESUPUESTADA', 'APROBADA',
                    'EN_EJECUCION', 'FINALIZADA', 'PAGADA')),
  FOREIGN KEY (id_unidad)  REFERENCES unidad_funcional(id_unidad),
  FOREIGN KEY (id_tecnico) REFERENCES tecnico(id_tecnico)
) ENGINE=InnoDB;

-- Presupuesto de una orden (puede haber mas de uno: original y ampliaciones)
CREATE TABLE presupuesto (
  id_presupuesto        INT AUTO_INCREMENT PRIMARY KEY,
  id_orden              INT          NOT NULL,
  fecha_creacion        DATETIME     NOT NULL,
  estado                VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE',
  materiales_a_cargo_de VARCHAR(15)  NOT NULL DEFAULT 'TECNICO',
  observacion           VARCHAR(255),
  CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
  CHECK (materiales_a_cargo_de IN ('TECNICO', 'ADMINISTRACION')),
  FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Cada renglon del presupuesto (mano de obra o material)
CREATE TABLE item_presupuesto (
  id_item         INT AUTO_INCREMENT PRIMARY KEY,
  id_presupuesto  INT           NOT NULL,
  descripcion     VARCHAR(150)  NOT NULL,
  tipo            VARCHAR(10)   NOT NULL,
  cantidad        INT           NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  CHECK (tipo IN ('MANO_OBRA', 'MATERIAL')),
  CHECK (cantidad > 0),
  FOREIGN KEY (id_presupuesto) REFERENCES presupuesto(id_presupuesto) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Constancia de entrega y devolucion de llaves
CREATE TABLE registro_llaves (
  id_registro      INT AUTO_INCREMENT PRIMARY KEY,
  id_orden         INT          NOT NULL,
  detalle_juego    VARCHAR(150) NOT NULL,
  fecha_entrega    DATETIME     NOT NULL,
  fecha_devolucion DATETIME NULL,
  FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Materiales efectivamente usados en el trabajo
CREATE TABLE material_utilizado (
  id_material    INT AUTO_INCREMENT PRIMARY KEY,
  id_orden       INT           NOT NULL,
  nombre         VARCHAR(100)  NOT NULL,
  cantidad       INT           NOT NULL,
  costo_unitario DECIMAL(12,2) NOT NULL,
  CHECK (cantidad > 0),
  FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Fotos de respaldo. Solo se guarda la ruta del archivo.
CREATE TABLE evidencia_foto (
  id_foto       INT AUTO_INCREMENT PRIMARY KEY,
  id_orden      INT          NOT NULL,
  ruta_archivo  VARCHAR(255) NOT NULL,
  etapa         VARCHAR(10)  NOT NULL,
  fecha_subida  DATETIME     NOT NULL,
  CHECK (etapa IN ('INICIO', 'AVANCE', 'FINAL')),
  FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Una orden (bloqueada) no puede iniciar hasta que termine otra (previa)
CREATE TABLE dependencia_orden (
  id_dependencia      INT AUTO_INCREMENT PRIMARY KEY,
  id_orden_bloqueada  INT NOT NULL,
  id_orden_previa     INT NOT NULL,
  tiempo_espera_horas INT NOT NULL DEFAULT 0,
  UNIQUE (id_orden_bloqueada, id_orden_previa),
  -- Que una orden no dependa de si misma se controla en Java (GestorOrdenes.vincularDependencia):
  -- MySQL no permite un CHECK sobre columnas que tienen ON DELETE CASCADE.
  CHECK (tiempo_espera_horas >= 0),
  FOREIGN KEY (id_orden_bloqueada) REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
  FOREIGN KEY (id_orden_previa)    REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Indice para filtrar ordenes por estado (las claves foraneas ya tienen indice propio en MySQL)
CREATE INDEX idx_orden_estado ON orden_trabajo(estado);
