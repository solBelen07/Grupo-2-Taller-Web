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
INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true), (null,'user@test.com','1234','ADMIN', true);
INSERT INTO Grupo(id, nombre) VALUES(null, 'grupo1'),(null, 'grupo2'),(null, 'grupo3');

INSERT INTO Parcial
(id, nombre, materia_id, fecha, horario, cantidadDiasEstudio, horasPorDia)
VALUES
(
    null,
    'Primer Parcial',
    (SELECT id FROM Materia WHERE nombre = 'Taller Web I'),
    '2026-10-15',
    '18:00:00',
    4,
    2
);

INSERT INTO Parcial
(id, nombre, materia_id, fecha, horario, cantidadDiasEstudio, horasPorDia)
VALUES
(
    null,
    'Parcial SQL y Normalización',
    (SELECT id FROM Materia WHERE nombre = 'Base de Datos II'),
    '2026-10-22',
    '16:00:00',
    5,
    3
);

INSERT INTO Parcial
(id, nombre, materia_id, fecha, horario, cantidadDiasEstudio, horasPorDia)
VALUES
(
    null,
    'Evaluación de Interfaces',
    (SELECT id FROM Materia WHERE nombre = 'Visualización e Interfaces'),
    '2026-10-30',
    '14:00:00',
    3,
    2
);

-- ============================================
-- SESIONES DE ESTUDIO - TALLER WEB I
-- Parcial: Primer Parcial
-- Fecha del parcial: 2026-10-15
-- ============================================

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Spring MVC',
    '2026-10-11',
    2,
    true,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Primer Parcial'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Hibernate y persistencia',
    '2026-10-12',
    2,
    true,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Primer Parcial'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Controladores y servicios',
    '2026-10-13',
    2,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Primer Parcial'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Thymeleaf',
    '2026-10-14',
    2,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Primer Parcial'
    )
);


-- ============================================
-- SESIONES DE ESTUDIO - BASE DE DATOS II
-- Parcial: Parcial SQL y Normalización
-- Fecha del parcial: 2026-10-22
-- ============================================

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Modelo entidad relación',
    '2026-10-17',
    3,
    true,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Parcial SQL y Normalización'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Normalización',
    '2026-10-18',
    3,
    true,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Parcial SQL y Normalización'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Consultas SQL',
    '2026-10-19',
    3,
    true,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Parcial SQL y Normalización'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'JOIN y subconsultas',
    '2026-10-20',
    3,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Parcial SQL y Normalización'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Procedimientos y triggers',
    '2026-10-21',
    3,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Parcial SQL y Normalización'
    )
);


-- ============================================
-- SESIONES DE ESTUDIO - VISUALIZACIÓN E INTERFACES
-- Parcial: Evaluación de Interfaces
-- Fecha del parcial: 2026-10-30
-- ============================================

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Principios de diseño visual',
    '2026-10-27',
    2,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Evaluación de Interfaces'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Wireframes y prototipos',
    '2026-10-28',
    2,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Evaluación de Interfaces'
    )
);

INSERT INTO SesionEstudio
(id, tema, fecha, cantidadHoras, completada, parcial_id)
VALUES
(
    null,
    'Usabilidad y experiencia de usuario',
    '2026-10-29',
    2,
    false,
    (
        SELECT id
        FROM Parcial
        WHERE nombre = 'Evaluación de Interfaces'
    )
);