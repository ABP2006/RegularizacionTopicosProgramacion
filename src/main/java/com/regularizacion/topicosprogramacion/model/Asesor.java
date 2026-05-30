package com.regularizacion.topicosprogramacion.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Asesor extends Persona {
    private String fechaContratacion = "";
    private double sueldo;
    private String departamento = "";
    private String passwordHash = "";

    public static Asesor fromMap(String id, Map<String, Object> map) {
        Asesor asesor = new Asesor();
        asesor.id = id;
        asesor.readCommon(map);
        asesor.fechaContratacion = text(map.get("fechaContratacion"));
        asesor.sueldo = number(map.get("sueldo"));
        asesor.departamento = text(map.get("departamento"));
        asesor.passwordHash = text(map.get("passwordHash"));
        return asesor;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = writeCommon(new LinkedHashMap<>());
        map.put("fechaContratacion", fechaContratacion);
        map.put("sueldo", sueldo);
        map.put("departamento", departamento);
        map.put("passwordHash", passwordHash);
        return map;
    }

    @Override
    public String toString() {
        return "Asesor: " + nombreCompleto + " | Departamento: " + departamento + " | Correo: " + mail;
    }

    public String getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(String fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}

