package com.tallerwebi.dominio.sesionEstudio;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioSesionEstudioImpl implements ServicioSesionEstudio {

    private final RepositorioSesionEstudio repositorioSesionEstudio;

    public ServicioSesionEstudioImpl(RepositorioSesionEstudio repositorioSesionEstudio) {
        this.repositorioSesionEstudio = repositorioSesionEstudio;
    }

    @Override
    public void crearSesion(SesionEstudio sesionEstudio) {
        repositorioSesionEstudio.guardar(sesionEstudio);
    }

    @Override
    public void editarSesion(SesionEstudio sesionEstudio) {
        repositorioSesionEstudio.modificar(sesionEstudio);
    }

    @Override
    public void eliminarSesion(SesionEstudio sesionEstudio) {
        repositorioSesionEstudio.eliminar(sesionEstudio);
    }
}