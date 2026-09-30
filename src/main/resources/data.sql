
INSERT INTO Materia (id, nombre, descripcion, docente, color, dias, horario, materialBibliografico)
VALUES (
           null,
           'Taller Web I',
           'Desarrollo de aplicaciones web con Spring MVC, Hibernate y Thymeleaf.',
           'Spizzirri',
           '#8B5CF6',
           'Lunes y Miércoles',
           '18:00 - 22:00',
           'Documentación de Spring'
       );

INSERT INTO Materia (id, nombre, descripcion, docente, color, dias, horario, materialBibliografico)
VALUES (
           null,
           'Base de Datos II',
           'Diseño y administración de bases de datos.',
           'Laura Ibáñez',
           '#3B82F6',
           'Martes y Jueves',
           '16:00 - 20:00',
           'Apuntes SQL'
       );

INSERT INTO Materia (id, nombre, descripcion, docente, color, dias, horario, materialBibliografico)
VALUES (
           null,
           'Visualización e Interfaces',
           'Diseño de interfaces y experiencia de usuario.',
           'Mariana López',
           '#EC4899',
           'Viernes',
           '14:00 - 18:00',
           'Material de Figma'
       );

-- =====================================================================
-- Usuarios y grupos
-- =====================================================================

INSERT INTO Usuario (id, email, password, rol, activo)
VALUES
    (null, 'test@unlam.edu.ar', 'test', 'ADMIN', true),
    (null, 'user@test.com', '1234', 'ADMIN', true);

INSERT INTO Grupo (id, nombre)
VALUES
    (null, 'grupo1'),
    (null, 'grupo2'),
    (null, 'grupo3');

-- =====================================================================
-- CAL-01: eventos de ejemplo para el calendario, del usuario
-- test@unlam.edu.ar (id 1), usando las materias sembradas arriba
-- (1 = Taller Web I, 2 = Base de Datos II).
-- =====================================================================

INSERT INTO Evento (id, usuarioId, titulo, tipo, inicio, fin, materia_id)
VALUES (
           null,
           1,
           'Clase de Taller Web I',
           'CLASE',
           CONCAT(CURDATE(), ' 18:00:00'),
           CONCAT(CURDATE(), ' 22:00:00'),
           1
       );

INSERT INTO Evento (id, usuarioId, titulo, tipo, inicio, fin, materia_id)
VALUES (
           null,
           1,
           'Práctica de Base de Datos II',
           'CLASE',
           CONCAT(CURDATE(), ' 08:00:00'),
           CONCAT(CURDATE(), ' 10:00:00'),
           2
       );

-- =====================================================================
-- CAL-02: Tareas de /tareas vinculadas a eventos del calendario, para
-- mostrar el badge de estado de seguimiento.
-- =====================================================================

-- Ejemplo 1, hoy: un TP con 40% de avance y 10 días para la entrega.
-- Menos del 50% -> "En tiempo".
INSERT INTO Tarea (id, titulo, materia, estado, horasRealizadas, horasPlanificadas, responsable, tipo, fechaVencimiento)
VALUES (
           null,
           'Entrega TP de Taller Web I',
           'Taller Web I',
           'PENDIENTE',
           4,
           10,
           'test@unlam.edu.ar',
           'TP',
           DATE_ADD(CURDATE(), INTERVAL 10 DAY)
       );

INSERT INTO Evento (id, usuarioId, titulo, tipo, inicio, fin, materia_id, tareaId)
VALUES (
           null,
           1,
           'Entrega TP de Taller Web I',
           'TRABAJO_PRACTICO',
           CONCAT(CURDATE(), ' 20:00:00'),
           CONCAT(CURDATE(), ' 21:00:00'),
           1,
           1
       );

-- Ejemplo 2, mañana: un Parcial con 70% de avance pero que vence en 2
-- días. La urgencia por fecha le gana al progreso -> "Próxima a vencer".
INSERT INTO Tarea (id, titulo, materia, estado, horasRealizadas, horasPlanificadas, responsable, tipo, fechaVencimiento)
VALUES (
           null,
           'Parcial de Base de Datos II',
           'Base de Datos II',
           'PENDIENTE',
           7,
           10,
           'test@unlam.edu.ar',
           'PARCIAL',
           DATE_ADD(CURDATE(), INTERVAL 2 DAY)
       );

INSERT INTO Evento (id, usuarioId, titulo, tipo, inicio, fin, materia_id, tareaId)
VALUES (
           null,
           1,
           'Parcial de Base de Datos II',
           'PARCIAL',
           CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 09:00:00'),
           CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 11:00:00'),
           2,
           2
       );

-- Ejemplo 3, pasado mañana: un Parcial con 60% de avance y 15 días
-- todavía. Acá gana el progreso -> "Parcialmente completada".
INSERT INTO Tarea (id, titulo, materia, estado, horasRealizadas, horasPlanificadas, responsable, tipo, fechaVencimiento)
VALUES (
           null,
           'Parcial de Visualización e Interfaces',
           'Visualización e Interfaces',
           'PENDIENTE',
           6,
           10,
           'test@unlam.edu.ar',
           'PARCIAL',
           DATE_ADD(CURDATE(), INTERVAL 15 DAY)
       );

INSERT INTO Evento (id, usuarioId, titulo, tipo, inicio, fin, materia_id, tareaId)
VALUES (
           null,
           1,
           'Parcial de Visualización e Interfaces',
           'PARCIAL',
           CONCAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY), ' 14:00:00'),
           CONCAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY), ' 16:00:00'),
           3,
           3
       );