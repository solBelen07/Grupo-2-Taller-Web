package com.tallerwebi.dominio.registro;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import java.util.List;

public interface RepositorioRegistro {
    Universidad buscarUniversidad(Long id);
    Carrera buscarCarrera(Long id);
    List<Interes> buscarIntereses(List<Long> ids);
}