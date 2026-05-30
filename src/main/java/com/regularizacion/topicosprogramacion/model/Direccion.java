package com.regularizacion.topicosprogramacion.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Direccion {
    private String calle = "";
    private String ciudad = "";
    private String estado = "";
    private String pais = "Mexico";
    private String codigoPostal = "";

    public static Direccion fromMap(Map<String, Object> map) {
        Direccion direccion = new Direccion();
        direccion.calle = text(map.get("calle"));
        direccion.ciudad = text(map.get("ciudad"));
        direccion.estado = text(map.get("estado"));
        direccion.pais = text(map.getOrDefault("pais", "Mexico"));
        direccion.codigoPostal = text(map.get("codigoPostal"));
        return direccion;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("calle", calle);
        map.put("ciudad", ciudad);
        map.put("estado", estado);
        map.put("pais", pais);
        map.put("codigoPostal", codigoPostal);
        return map;
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public String asSingleLine() {
        return String.join(", ", calle, ciudad, estado, pais, codigoPostal).replaceAll("(,\\s*)+", ", ").replaceAll("^, |, $", "");
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}

