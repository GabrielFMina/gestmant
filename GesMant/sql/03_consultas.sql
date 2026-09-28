-- =====================================================================
-- GesMant - Script 03: consultas SQL
-- =====================================================================
USE gesmant;

-- C1. Listado general de ordenes con su consorcio, unidad y tecnico asignado
SELECT o.codigo, c.nombre AS consorcio, u.numero AS unidad, o.prioridad, o.estado,
       t.nombre AS tecnico
FROM orden_trabajo o
JOIN unidad_funcional u ON u.id_unidad = o.id_unidad
JOIN consorcio c        ON c.id_consorcio = u.id_consorcio
LEFT JOIN tecnico t     ON t.id_tecnico = o.id_tecnico
ORDER BY o.codigo;

-- C2. Tareas abiertas de un tecnico (Gabriel Mina, id 1), las mas urgentes primero
SELECT o.codigo, u.numero AS unidad, o.descripcion_problema, o.prioridad, o.estado
FROM orden_trabajo o
JOIN unidad_funcional u ON u.id_unidad = o.id_unidad
WHERE o.id_tecnico = 1
  AND o.estado NOT IN ('FINALIZADA', 'PAGADA')
ORDER BY CASE o.prioridad WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END, o.codigo;

-- C3. Total de cada presupuesto separando mano de obra y materiales
SELECT o.codigo, p.id_presupuesto, p.estado,
       SUM(CASE WHEN i.tipo = 'MANO_OBRA' THEN i.cantidad * i.precio_unitario ELSE 0 END) AS mano_obra,
       SUM(CASE WHEN i.tipo = 'MATERIAL'  THEN i.cantidad * i.precio_unitario ELSE 0 END) AS materiales,
       SUM(i.cantidad * i.precio_unitario) AS total
FROM presupuesto p
JOIN orden_trabajo o     ON o.id_orden = p.id_orden
JOIN item_presupuesto i  ON i.id_presupuesto = p.id_presupuesto
GROUP BY o.codigo, p.id_presupuesto, p.estado
ORDER BY o.codigo;

-- C4. Historial de trabajos terminados de una unidad (3B del Edificio Los Alamos)
SELECT o.codigo, o.fecha_fin, t.nombre AS tecnico, o.diagnostico, o.trabajo_realizado
FROM orden_trabajo o
JOIN unidad_funcional u ON u.id_unidad = o.id_unidad
JOIN tecnico t          ON t.id_tecnico = o.id_tecnico
WHERE u.id_consorcio = 1 AND u.numero = '3B'
  AND o.estado IN ('FINALIZADA', 'PAGADA')
ORDER BY o.fecha_fin DESC;

-- C5. Llaves entregadas que todavia no se devolvieron
SELECT o.codigo, r.detalle_juego, r.fecha_entrega, t.nombre AS tecnico
FROM registro_llaves r
JOIN orden_trabajo o ON o.id_orden = r.id_orden
JOIN tecnico t       ON t.id_tecnico = o.id_tecnico
WHERE r.fecha_devolucion IS NULL;

-- C6. Ordenes que dependen de otra y en que situacion esta la orden previa
SELECT ob.codigo AS orden_bloqueada, op.codigo AS orden_previa, op.estado AS estado_previa,
       op.fecha_fin AS fin_previa, d.tiempo_espera_horas
FROM dependencia_orden d
JOIN orden_trabajo ob ON ob.id_orden = d.id_orden_bloqueada
JOIN orden_trabajo op ON op.id_orden = d.id_orden_previa;

-- C7. Cantidad de ordenes por estado
SELECT estado, COUNT(*) AS cantidad
FROM orden_trabajo
GROUP BY estado
ORDER BY cantidad DESC, estado;

-- C8. Costo de materiales utilizados por consorcio
SELECT c.nombre AS consorcio, SUM(m.cantidad * m.costo_unitario) AS costo_materiales
FROM material_utilizado m
JOIN orden_trabajo o    ON o.id_orden = m.id_orden
JOIN unidad_funcional u ON u.id_unidad = o.id_unidad
JOIN consorcio c        ON c.id_consorcio = u.id_consorcio
GROUP BY c.nombre;

-- C9. Presupuestos pendientes de aprobacion con su monto total
SELECT o.codigo, u.numero AS unidad, p.fecha_creacion,
       (SELECT SUM(i.cantidad * i.precio_unitario)
          FROM item_presupuesto i
         WHERE i.id_presupuesto = p.id_presupuesto) AS total
FROM presupuesto p
JOIN orden_trabajo o    ON o.id_orden = p.id_orden
JOIN unidad_funcional u ON u.id_unidad = o.id_unidad
WHERE p.estado = 'PENDIENTE';

-- C10. Tecnicos activos que no tienen ninguna orden abierta (disponibles)
SELECT t.nombre, t.especialidad
FROM tecnico t
WHERE t.activo = TRUE
  AND NOT EXISTS (SELECT 1 FROM orden_trabajo o
                   WHERE o.id_tecnico = t.id_tecnico
                     AND o.estado NOT IN ('FINALIZADA', 'PAGADA'));
