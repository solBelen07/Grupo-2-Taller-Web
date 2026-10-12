package com.tallerwebi.presentacion.registro;

import com.tallerwebi.dominio.catalogo.ServicioCatalogo;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.registro.RegistroDTO;
import com.tallerwebi.dominio.registro.ServicioRegistro;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRegistro {

    private final ServicioRegistro servicioRegistro;
    private final ServicioCatalogo servicioCatalogo;

    @Autowired
    public ControladorRegistro(
            ServicioRegistro servicioRegistro,
            ServicioCatalogo servicioCatalogo
    ) {
        this.servicioRegistro = servicioRegistro;
        this.servicioCatalogo = servicioCatalogo;
    }

    @RequestMapping(path = "/registro", method = RequestMethod.GET)
    public ModelAndView mostrarFormulario() {
        return armarFormulario(new RegistroDTO(), null);
    }

    @RequestMapping(path = "/registro", method = RequestMethod.POST)
    public ModelAndView registrar(@ModelAttribute("registro") RegistroDTO registro) {
        try {
            servicioRegistro.registrar(registro);
        } catch (UsuarioExistente e) {
            return armarFormulario(registro, "Ese email ya está registrado");
        } catch (IllegalArgumentException e) {
            return armarFormulario(registro, e.getMessage());
        }
        return new ModelAndView("redirect:/login");
    }

    private ModelAndView armarFormulario(RegistroDTO registro, String error) {
        Map<String, Object> modelo = new ModelMap();
        modelo.put("registro", registro);
        modelo.put("universidades", servicioCatalogo.obtenerUniversidades());
        modelo.put("carreras", servicioCatalogo.obtenerCarreras());
        modelo.put("intereses", servicioCatalogo.obtenerIntereses());
        modelo.put("error", error);
        return new ModelAndView("registro", modelo);
    }
}