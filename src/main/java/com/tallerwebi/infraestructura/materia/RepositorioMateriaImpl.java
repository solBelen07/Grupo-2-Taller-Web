package com.tallerwebi.infraestructura.materia;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioMateria")
public class RepositorioMateriaImpl implements RepositorioMateria {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioMateriaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Materia> obtenerTodas() {
        return sessionFactory
                .getCurrentSession()
                .createQuery("from Materia", Materia.class)
                .getResultList();
    }

}
