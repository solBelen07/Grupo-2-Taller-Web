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
INSERT INTO Usuario(id, nombre, apellido, email, password, rol, activo, puntos) VALUES(null, 'Juan', 'Mendez','test@unlam.edu.ar', 'test', 'ADMIN', true, 0), (null,'user','nose','user@test.com','1234','ADMIN', true, 0);
INSERT INTO Grupo(id, nombre) VALUES(null, 'grupo1'),(null, 'grupo2'),(null, 'grupo3');