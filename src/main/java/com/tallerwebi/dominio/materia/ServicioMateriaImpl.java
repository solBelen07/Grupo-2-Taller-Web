package com.tallerwebi.dominio.materia;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ServicioMateriaImpl implements ServicioMateria {

    private RepositorioMateria repositorioMateria;

    public ServicioMateriaImpl(RepositorioMateria repositorioMateria) {
        this.repositorioMateria = repositorioMateria;
    }

    @Override
    public List<Materia> obtenerMaterias() {
        return repositorioMateria.obtenerTodas();
    }
}
