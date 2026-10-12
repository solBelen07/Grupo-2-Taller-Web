package com.tallerwebi.dominio.registro;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface ServicioRegistro {
    void registrar(RegistroDTO registro) throws UsuarioExistente;
}