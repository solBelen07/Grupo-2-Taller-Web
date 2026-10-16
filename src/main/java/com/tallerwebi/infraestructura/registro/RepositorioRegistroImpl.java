package com.tallerwebi.infraestructura.registro;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.registro.RepositorioRegistro;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioRegistro")
public class RepositorioRegistroImpl implements RepositorioRegistro {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioRegistroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Universidad buscarUniversidad(Long id) {
        return sessionFactory.getCurrentSession().get(Universidad.class, id);
    }

    @Override
    public Carrera buscarCarrera(Long id) {
        return sessionFactory.getCurrentSession().get(Carrera.class, id);
    }

    @Override
    public List<Interes> buscarIntereses(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return sessionFactory
                .getCurrentSession()
                .createQuery("FROM Interes i WHERE i.id IN (:ids)", Interes.class)
                .setParameterList("ids", ids)
                .list();
    }
}