package com.tallerwebi.infraestructura.SesionEstudio;

import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioSesionEstudioImpl
        implements RepositorioSesionEstudio {

    private final SessionFactory sessionFactory;

    public RepositorioSesionEstudioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(SesionEstudio sesionEstudio) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(sesionEstudio);
    }

    @Override
    public void modificar(SesionEstudio sesionEstudio) {
        Session session = sessionFactory.getCurrentSession();
        session.merge(sesionEstudio);
    }

    @Override
    public void eliminar(SesionEstudio sesionEstudio) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(sesionEstudio);
    }
}