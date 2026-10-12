package com.tallerwebi.infraestructura.catalogo;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.RepositorioCatalogo;
import com.tallerwebi.dominio.catalogo.Universidad;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioCatalogo")
public class RepositorioCatalogoImpl implements RepositorioCatalogo {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioCatalogoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Universidad> obtenerUniversidades() {
        return sessionFactory
                .getCurrentSession()
                .createQuery("FROM Universidad u ORDER BY u.nombre", Universidad.class)
                .list();
    }

    @Override
    public List<Carrera> obtenerCarreras() {
        return sessionFactory
                .getCurrentSession()
                .createQuery("FROM Carrera c ORDER BY c.nombre", Carrera.class)
                .list();
    }

    @Override
    public List<Interes> obtenerIntereses() {
        return sessionFactory
                .getCurrentSession()
                .createQuery("FROM Interes i ORDER BY i.nombre", Interes.class)
                .list();
    }
}