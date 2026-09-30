package com.tallerwebi.dominio.sesionEstudio;

public interface RepositorioSesionEstudio {
    void guardar(SesionEstudio sesionEstudio);
    void modificar(SesionEstudio sesionEstudio);
    void eliminar(SesionEstudio sesionEstudio);
}