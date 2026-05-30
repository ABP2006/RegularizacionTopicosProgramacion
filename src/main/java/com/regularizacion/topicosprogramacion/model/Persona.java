package com.regularizacion.topicosprogramacion.model;

import java.util.Map;

public class Persona {
    protected String id = "";
    protected String nombreCompleto = "";
    protected Direccion direccion = new Direccion();
    protected String telefono = "";
    protected String rfc = "";
    protected String mail = "";
    protected String genero = "";
    protected String fotografia = "";

    protected void readCommon(Map<String, Object> map) {
        nombreCompleto = text(map.get("nombreCompleto"));
        direccion = Direccion.fromMap(map);
        telefono = text(map.get("telefono"));
        rfc = text(map.get("rfc"));
        mail = text(map.get("mail"));
        genero = text(map.get("genero"));
        fotografia = text(map.get("fotografia"));
    }

    protected Map<String, Object> writeCommon(Map<String, Object> map) {
        map.put("nombreCompleto", nombreCompleto);
        map.putAll(direccion.toMap());
        map.put("telefono", telefono);
        map.put("rfc", rfc);
        map.put("mail", mail);
        map.put("genero", genero);
        map.put("fotografia", fotografia);
        return map;
    }

    protected static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    protected static double number(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(text(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }
}

