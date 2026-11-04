package com.tallerwebi.infraestructura.materialBibliografico;

import static org.mockito.Mockito.mock;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
import com.tallerwebi.dominio.materialBibliografico.RepositorioMaterial;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioMaterialTest {

  private RepositorioMaterial repositorioMaterial;

  @Autowired
  private SessionFactory sessionFactory;

  private Materia materiaMock;
  private MaterialBibliografico materialBibliograficoMock;

  @BeforeEach
  public void inicializar() {
    sessionFactory = mock(SessionFactory.class);
    repositorioMaterial = new RepositorioMaterialImpl(sessionFactory);
    materiaMock = mock(Materia.class);
    materialBibliograficoMock = mock(MaterialBibliografico.class);
  }

  @Test
  @Transactional
  @Rollback
  public void guardarMaterialDeberiaPersistirElMaterial() {
    materiaMock.setNombre("grupo-existente");
    materialBibliograficoMock =
      new MaterialBibliografico("archivo.pdf", "pdf", 1024L, "clave-existente", materiaMock);
  }
}
