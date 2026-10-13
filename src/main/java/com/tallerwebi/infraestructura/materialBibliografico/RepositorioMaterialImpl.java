
package com.tallerwebi.infraestructura.materialBibliografico;

import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
import com.tallerwebi.dominio.materialBibliografico.RepositorioMaterial;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioMaterialImpl implements RepositorioMaterial {

    private final SessionFactory sessionFactory;

    public RepositorioMaterialImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(MaterialBibliografico material) {
        sessionFactory.getCurrentSession().persist(material);
    }

    @Override
    public MaterialBibliografico buscarPorId(Integer id) {
        return sessionFactory.getCurrentSession()
            .get(MaterialBibliografico.class, id);
    }

    @Override
    public List<MaterialBibliografico> buscarPorMateria(Integer materiaId) {
        return sessionFactory.getCurrentSession()
            .createQuery(
                "FROM MaterialBibliografico m "
                + "WHERE m.materia.id = :materiaId "
                + "ORDER BY m.fechaSubida DESC",
                MaterialBibliografico.class
            )
            .setParameter("materiaId", materiaId)
            .getResultList();
    }

    @Override
    public void eliminar(MaterialBibliografico material) {
        sessionFactory.getCurrentSession().remove(material);
    }
}
