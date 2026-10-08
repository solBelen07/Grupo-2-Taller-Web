INSERT INTO Materia
(id, nombre, descripcion, docente, color, dias, horario, materialBibliografico)
VALUES
(
    null,
    'Taller Web I',
    'Desarrollo de aplicaciones web con Spring MVC, Hibernate y Thymeleaf.',
    'Spizzirri',
    '#8B5CF6',
    'Lunes y Miércoles',
    '18:00 - 22:00',
    'Documentación de Spring'
);

INSERT INTO Materia
(id, nombre, descripcion, docente, color, dias, horario, materialBibliografico)
VALUES
(
    null,
    'Base de Datos II',
    'Diseño y administración de bases de datos.',
    'Laura Ibáñez',
    '#3B82F6',
    'Martes y Jueves',
    '16:00 - 20:00',
    'Apuntes SQL'
);

INSERT INTO Materia
(id, nombre, descripcion, docente, color, dias, horario, materialBibliografico)
VALUES
(
    null,
    'Visualización e Interfaces',
    'Diseño de interfaces y experiencia de usuario.',
    'Mariana López',
    '#EC4899',
    'Viernes',
    '14:00 - 18:00',
    'Material de Figma'
);

INSERT INTO materia_usuario (materia_id, usuario_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 1);

INSERT INTO Parcial
(id, nombre, materia_id, fecha, horario, cantidadDiasEstudio, horasPorDia)
VALUES
    (null, 'Primer Parcial', (SELECT id FROM Materia WHERE nombre = 'Taller Web I'), '2026-10-15', '18:00:00', 4, 2);

INSERT INTO Parcial
(id, nombre, materia_id, fecha, horario, cantidadDiasEstudio, horasPorDia)
VALUES
    (null, 'Parcial SQL y Normalización', (SELECT id FROM Materia WHERE nombre = 'Base de Datos II'), '2026-10-22', '16:00:00', 5, 3);

INSERT INTO Parcial
(id, nombre, materia_id, fecha, horario, cantidadDiasEstudio, horasPorDia)
VALUES
    (null, 'Evaluación de Interfaces', (SELECT id FROM Materia WHERE nombre = 'Visualización e Interfaces'), '2026-10-30', '14:00:00', 3, 2);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Spring MVC', '2026-10-11', 2, true,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Primer Parcial'));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Hibernate y persistencia', '2026-10-12', 2, true,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Primer Parcial'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Controladores y servicios', '2026-10-13', 2, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Primer Parcial'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Thymeleaf', '2026-10-14', 2, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Primer Parcial'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Modelo entidad relación', '2026-10-17', 3, true,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Parcial SQL y Normalización'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Normalización', '2026-10-18', 3, true,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Parcial SQL y Normalización'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Consultas SQL', '2026-10-19', 3, true,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Parcial SQL y Normalización'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'JOIN y subconsultas', '2026-10-20', 3, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Parcial SQL y Normalización'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Procedimientos y triggers', '2026-10-21', 3, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Parcial SQL y Normalización'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Principios de diseño visual', '2026-10-27', 2, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Evaluación de Interfaces'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Wireframes y prototipos', '2026-10-28', 2, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Evaluación de Interfaces'
     ));

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
    (null, 'Usabilidad y experiencia de usuario', '2026-10-29', 2, false,
     (   SELECT id
         FROM Parcial
         WHERE nombre = 'Evaluación de Interfaces'
     ));

INSERT INTO Usuario(id, nombre, apellido, email, password, rol, activo, puntos)

VALUES
    (null, 'Juan', 'Mendez','test@unlam.edu.ar', 'test', 'ADMIN', true, 0),
    (null,'user','nose','user@test.com','1234','ADMIN', true, 0);

INSERT INTO Grupo (id, nombre)
VALUES
    (null, 'grupo1'),
    (null, 'grupo2'),
    (null, 'grupo3');

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

INSERT INTO Logro(id, nombre, descripcion)
VALUES (
           null,
           'Maratonista',
           'Estudiaste más de 50 horas'
       ),
    (
     null,
        'Invitador',
     'Fuiste invitado muchas veces a grupos'
    );