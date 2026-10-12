package com.tallerwebi.dominio.registro;

import java.util.ArrayList;
import java.util.List;

public class RegistroDTO {

    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private Long universidadId;
    private Long carreraId;
    private List<Long> interesesIds = new ArrayList<>();

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getUniversidadId() {
        return universidadId;
    }

    public void setUniversidadId(Long universidadId) {
        this.universidadId = universidadId;
    }

    public Long getCarreraId() {
        return carreraId;
    }

    public void setCarreraId(Long carreraId) {
        this.carreraId = carreraId;
    }

    public List<Long> getInteresesIds() {
        return interesesIds;
    }

    public void setInteresesIds(List<Long> interesesIds) {
        this.interesesIds = interesesIds;
    }
}