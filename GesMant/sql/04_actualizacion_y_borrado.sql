-- =====================================================================
-- GesMant - Script 04: actualizacion y borrado de registros
-- Ejecutar despues de 01 y 02.
-- =====================================================================
USE gesmant;

-- A1. Asignar la orden OT-0008 (canilla) al plomero Luis Pereyra
UPDATE orden_trabajo SET id_tecnico = 2, estado = 'ASIGNADA'
WHERE codigo = 'OT-0008';

-- A2. Aprobar el presupuesto pendiente de OT-0004 y habilitar la orden
UPDATE presupuesto SET estado = 'APROBADO' WHERE id_presupuesto = 4;
UPDATE orden_trabajo SET estado = 'APROBADA' WHERE codigo = 'OT-0004';

-- A3. Registrar la devolucion de la llave del tablero del hall (OT-0003)
UPDATE registro_llaves SET fecha_devolucion = '2026-09-28 18:00:00'
WHERE id_registro = 2;

-- B1. Borrar una foto cargada por error en una orden que sigue en ejecucion
INSERT INTO evidencia_foto (id_orden, ruta_archivo, etapa, fecha_subida)
VALUES (3, 'fotos/OT-0003/foto_equivocada.jpg', 'AVANCE', '2026-09-28 10:00:00');
DELETE FROM evidencia_foto
WHERE id_orden = 3 AND ruta_archivo = 'fotos/OT-0003/foto_equivocada.jpg';

-- B2. Borrar una orden registrada por error. Gracias a ON DELETE CASCADE
--     tambien se borran su presupuesto y los items del presupuesto.
INSERT INTO orden_trabajo (codigo, id_unidad, descripcion_problema, prioridad, estado, fecha_creacion)
VALUES ('OT-0009', 3, 'Reclamo duplicado por error', 'BAJA', 'REGISTRADA', '2026-09-28 10:05:00');
INSERT INTO presupuesto (id_orden, fecha_creacion, estado)
VALUES ((SELECT id_orden FROM orden_trabajo WHERE codigo = 'OT-0009'), '2026-09-28 10:10:00', 'PENDIENTE');
DELETE FROM orden_trabajo WHERE codigo = 'OT-0009' AND estado = 'REGISTRADA';

-- B3. Dar de baja a un tecnico que nunca tuvo ordenes asignadas
DELETE FROM tecnico
WHERE id_tecnico = 3
  AND NOT EXISTS (SELECT 1 FROM orden_trabajo o WHERE o.id_tecnico = 3);

-- B4. Ejemplo de integridad referencial: esta sentencia FALLA a proposito,
--     porque el consorcio tiene unidades y ordenes asociadas.
--     (Se deja comentada para que el script pueda ejecutarse completo.)
-- DELETE FROM consorcio WHERE id_consorcio = 1;
-- Error esperado: Cannot delete or update a parent row: a foreign key constraint fails

-- Verificacion de los cambios
SELECT codigo, id_tecnico, estado FROM orden_trabajo WHERE codigo IN ('OT-0004', 'OT-0008', 'OT-0009');
SELECT id_registro, detalle_juego, fecha_devolucion FROM registro_llaves;
SELECT id_tecnico, nombre FROM tecnico;
