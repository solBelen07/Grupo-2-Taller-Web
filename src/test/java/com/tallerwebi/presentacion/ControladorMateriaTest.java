package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.ServicioMateria;
import com.tallerwebi.dominio.excepcion.MateriaExistente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMateriaTest {

  private ServicioMateria servicioMateriaMock;
  private ControladorMateria controladorMateria;

  @BeforeEach
  public void init() {
    this.servicioMateriaMock = mock(ServicioMateria.class);
    this.controladorMateria = new ControladorMateria(this.servicioMateriaMock);
  }

  @Test
  public void queSePuedaCrearUnaMateriaCompletandoTodosLosDatos() throws MateriaExistente {
    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setNombre("Taller Web I");
    datosMateria.setDescripcion("Materia de desarrollo web");
    datosMateria.setDocente("Spizzirri");
    datosMateria.setColor("#8B5CF6");
    datosMateria.setDias("Lunes y Miércoles");
    datosMateria.setHorario("18:00 - 22:00");
    datosMateria.setMaterialBibliografico("Documentación Spring");

    ModelAndView mav = controladorMateria.guardarMateria(datosMateria);

    ArgumentCaptor<Materia> materiaCaptor = ArgumentCaptor.forClass(Materia.class);

    verify(servicioMateriaMock, times(1)).crearMateria(materiaCaptor.capture());

    Materia materiaCreada = materiaCaptor.getValue();

    assertThat(materiaCreada.getNombre(), equalTo("Taller Web I"));
    assertThat(materiaCreada.getDescripcion(), equalTo("Materia de desarrollo web"));
    assertThat(materiaCreada.getDocente(), equalTo("Spizzirri"));
    assertThat(materiaCreada.getColor(), equalTo("#8B5CF6"));
    assertThat(materiaCreada.getDias(), equalTo("Lunes y Miércoles"));
    assertThat(materiaCreada.getHorario(), equalTo("18:00 - 22:00"));
    assertThat(materiaCreada.getMaterialBibliografico(), equalTo("Documentación Spring"));
    assertThat(mav.getViewName(), equalTo("redirect:/materias"));
    
  }
}
