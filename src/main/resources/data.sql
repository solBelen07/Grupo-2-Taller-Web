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

-- Eventos de ejemplo para el calendario (CAL-01), para el usuario test@unlam.edu.ar (id 1),
-- usando las materias ya sembradas arriba (1 = Taller Web I, 2 = Base de Datos II).
INSERT INTO Evento(id, usuarioId, titulo, tipo, inicio, fin, materia_id)
VALUES(null, 1, 'Clase de Taller Web I', 'CLASE', CONCAT(CURDATE(), ' 18:00:00'), CONCAT(CURDATE(), ' 22:00:00'), 1);
INSERT INTO Evento(id, usuarioId, titulo, tipo, inicio, fin, materia_id)
VALUES(null, 1, 'Práctica de Base de Datos II', 'CLASE', CONCAT(CURDATE(), ' 08:00:00'), CONCAT(CURDATE(), ' 10:00:00'), 2);

-- CAL-02: una Tarea (de /tareas) vinculada a un evento del calendario, para mostrar el badge de
-- estado (acá "Parcialmente completada": tiene algunas horas registradas y todavía le queda
-- tiempo antes de vencer).
INSERT INTO Tarea(id, titulo, materia, estado, horasRealizadas, horasPlanificadas, responsable, tipo, fechaVencimiento)
VALUES(null, 'Entrega TP de Taller Web I', 'Taller Web I', 'PENDIENTE', 4, 10, 'test@unlam.edu.ar', 'TP', DATE_ADD(CURDATE(), INTERVAL 10 DAY));
INSERT INTO Evento(id, usuarioId, titulo, tipo, inicio, fin, materia_id, tareaId)
VALUES(null, 1, 'Entrega TP de Taller Web I', 'TRABAJO_PRACTICO', CONCAT(CURDATE(), ' 20:00:00'), CONCAT(CURDATE(), ' 21:00:00'), 1, 1);

