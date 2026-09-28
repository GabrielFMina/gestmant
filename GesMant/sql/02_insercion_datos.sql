-- =====================================================================
-- GesMant - Script 02: insercion de datos de prueba
-- Los nombres de personas, edificios y la administracion son ficticios.
-- =====================================================================
USE gesmant;

INSERT INTO administracion (nombre, cuit, telefono) VALUES
  ('Administración de Consorcios Centro', '30-71234567-8', '351-4221100');

INSERT INTO consorcio (id_administracion, nombre, direccion) VALUES
  (1, 'Edificio Los Álamos', 'Av. Colón 1250, Córdoba'),
  (1, 'Edificio Mirador',    'Bv. San Juan 480, Córdoba');

INSERT INTO unidad_funcional (id_consorcio, numero, piso, es_espacio_comun, nombre_inquilino, telefono_inquilino) VALUES
  (1, '3B',       '3',  FALSE, 'Laura Giménez',  '351-5550101'),
  (1, 'HALL-PB',  'PB', TRUE,  NULL,             NULL),
  (1, '1A',       '1',  FALSE, 'Martin Sosa',    '351-5550102'),
  (2, '5A',       '5',  FALSE, 'Carla Ríos',     '351-5550103'),
  (2, 'TERRAZA',  '8',  TRUE,  NULL,             NULL),
  (2, '6B',       '6',  FALSE, 'Jorge Molina',   '351-5550104'),
  (2, '2C',       '2',  FALSE, 'Ana Ferreyra',   '351-5550105');

INSERT INTO tecnico (nombre, especialidad, telefono, activo) VALUES
  ('Gabriel Mina',  'Mantenimiento general', '351-6001001', TRUE),
  ('Luis Pereyra',  'Plomería',              '351-6001002', TRUE),
  ('Diego Almada',  'Electricidad',          '351-6001003', TRUE);

INSERT INTO usuario (nombre_usuario, clave, rol, id_tecnico) VALUES
  ('admin',    'admin123', 'ADMINISTRADOR', NULL),
  ('gmina',    'tec123',   'TECNICO',       1),
  ('lpereyra', 'tec123',   'TECNICO',       2);

INSERT INTO orden_trabajo (codigo, id_unidad, id_tecnico, descripcion_problema, prioridad, estado,
  requiere_coordinacion, diagnostico, trabajo_realizado, fecha_creacion, fecha_inicio, fecha_fin) VALUES
  ('OT-0001', 1, 2, 'Filtración de agua en pared del baño', 'ALTA', 'PAGADA', TRUE,
     'Caño de agua fría roto dentro de la pared', 'Se reemplazó tramo de caño y se cerró la pared',
     '2026-09-01 09:00:00', '2026-09-03 08:30:00', '2026-09-04 17:00:00'),
  ('OT-0002', 1, 1, 'Revoque húmedo y descascarado en pared del baño', 'MEDIA', 'APROBADA', TRUE,
     'Revoque flojo por humedad, hay que picar y rehacer', NULL,
     '2026-09-02 10:00:00', NULL, NULL),
  ('OT-0003', 2, 1, 'Luminarias del hall de entrada sin funcionar', 'MEDIA', 'EN_EJECUCION', FALSE,
     'Tres artefactos con equipo quemado', NULL,
     '2026-09-15 11:00:00', '2026-09-22 09:00:00', NULL),
  ('OT-0004', 4, 1, 'Puerta de placard descolgada', 'BAJA', 'PRESUPUESTADA', TRUE,
     'Bisagras rotas, se cambian las cuatro', NULL,
     '2026-09-18 16:00:00', NULL, NULL),
  ('OT-0005', 5, 2, 'Membrana de terraza con fisuras', 'ALTA', 'ASIGNADA', FALSE,
     NULL, NULL, '2026-09-20 08:00:00', NULL, NULL),
  ('OT-0006', 6, 1, 'Mancha de humedad en cielorraso del living', 'BAJA', 'APROBADA', TRUE,
     'Humedad que baja desde la terraza, pintar cuando se repare la membrana', NULL,
     '2026-09-20 09:00:00', NULL, NULL),
  ('OT-0007', 7, 1, 'Pintura de pared del dormitorio', 'BAJA', 'FINALIZADA', TRUE,
     'Pared con manchas, requiere enduido y dos manos de látex', 'Se enduyó y pintó la pared completa',
     '2026-09-10 10:00:00', '2026-09-23 08:00:00', '2026-09-24 18:00:00'),
  ('OT-0008', 3, NULL, 'Pérdida en canilla de la cocina', 'MEDIA', 'REGISTRADA', TRUE,
     NULL, NULL, '2026-09-27 12:00:00', NULL, NULL);

INSERT INTO presupuesto (id_orden, fecha_creacion, estado, materiales_a_cargo_de, observacion) VALUES
  (1, '2026-09-02 12:00:00', 'APROBADO',  'TECNICO', NULL),
  (2, '2026-09-05 10:00:00', 'APROBADO',  'TECNICO', NULL),
  (3, '2026-09-16 10:00:00', 'APROBADO',  'ADMINISTRACION', 'Los equipos los compra la administración'),
  (4, '2026-09-19 09:00:00', 'PENDIENTE', 'TECNICO', NULL),
  (6, '2026-09-21 10:00:00', 'APROBADO',  'TECNICO', NULL),
  (7, '2026-09-11 10:00:00', 'APROBADO',  'TECNICO', NULL);

INSERT INTO item_presupuesto (id_presupuesto, descripcion, tipo, cantidad, precio_unitario) VALUES
  (1, 'Mano de obra plomería',          'MANO_OBRA', 1, 85000.00),
  (1, 'Caño termofusión 20 mm (metro)', 'MATERIAL',  2,  6500.00),
  (1, 'Codos y cuplas',                 'MATERIAL',  4,  1800.00),
  (2, 'Mano de obra albañilería',       'MANO_OBRA', 1, 70000.00),
  (2, 'Bolsa de revoque grueso',        'MATERIAL',  2,  9500.00),
  (3, 'Mano de obra electricidad',      'MANO_OBRA', 1, 60000.00),
  (4, 'Mano de obra carpintería',       'MANO_OBRA', 1, 30000.00),
  (4, 'Bisagra reforzada',              'MATERIAL',  4,  3200.00),
  (5, 'Mano de obra pintura',           'MANO_OBRA', 1, 45000.00),
  (5, 'Pintura para cielorraso 4 L',    'MATERIAL',  1, 28000.00),
  (6, 'Mano de obra pintura',           'MANO_OBRA', 1, 55000.00),
  (6, 'Látex interior 10 L',            'MATERIAL',  1, 52000.00);

INSERT INTO registro_llaves (id_orden, detalle_juego, fecha_entrega, fecha_devolucion) VALUES
  (1, 'Llave puerta principal 3B (1 unidad)', '2026-09-03 08:00:00', '2026-09-04 17:30:00'),
  (3, 'Llave tablero eléctrico del hall',      '2026-09-22 08:45:00', NULL),
  (7, 'Llaves puerta 2C (juego de 2)',         '2026-09-23 07:45:00', '2026-09-24 18:15:00');

INSERT INTO material_utilizado (id_orden, nombre, cantidad, costo_unitario) VALUES
  (1, 'Caño termofusión 20 mm (metro)', 2, 6500.00),
  (1, 'Codos y cuplas',                 4, 1800.00),
  (3, 'Equipo para luminaria LED',      3, 15000.00),
  (7, 'Látex interior 10 L',            1, 52000.00),
  (7, 'Enduido 4 kg',                   1, 11000.00);

INSERT INTO evidencia_foto (id_orden, ruta_archivo, etapa, fecha_subida) VALUES
  (1, 'fotos/OT-0001/inicio_1.jpg', 'INICIO', '2026-09-03 08:40:00'),
  (1, 'fotos/OT-0001/final_1.jpg',  'FINAL',  '2026-09-04 16:50:00'),
  (3, 'fotos/OT-0003/inicio_1.jpg', 'INICIO', '2026-09-22 09:10:00'),
  (7, 'fotos/OT-0007/inicio_1.jpg', 'INICIO', '2026-09-23 08:10:00'),
  (7, 'fotos/OT-0007/final_1.jpg',  'FINAL',  '2026-09-24 17:50:00');

-- OT-0002 (revoque) depende de OT-0001 (filtracion) y hay que esperar 72 h de secado.
-- OT-0006 (cielorraso) depende de OT-0005 (membrana) y hay que esperar 48 h.
INSERT INTO dependencia_orden (id_orden_bloqueada, id_orden_previa, tiempo_espera_horas) VALUES
  (2, 1, 72),
  (6, 5, 48);
