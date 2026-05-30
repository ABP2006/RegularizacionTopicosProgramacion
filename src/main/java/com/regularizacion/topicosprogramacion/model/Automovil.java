package com.regularizacion.topicosprogramacion.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Automovil {
    private String id = "";
    private String clienteId = "";
    private String marca = "";
    private String submarca = "";
    private String color = "";
    private int anioModelo;
    private String numeroSerie = "";
    private String placas = "";
    private String fotografia = "";

    public static Automovil fromMap(String id, Map<String, Object> map) {
        Automovil automovil = new Automovil();
        automovil.id = id;
        automovil.clienteId = text(map.get("clienteId"));
        automovil.marca = text(map.get("marca"));
        automovil.submarca = text(map.get("submarca"));
        automovil.color = text(map.get("color"));
        automovil.anioModelo = integer(map.get("anioModelo"));
        automovil.numeroSerie = text(map.get("numeroSerie"));
        automovil.placas = text(map.get("placas"));
        automovil.fotografia = text(map.get("fotografia"));
        return automovil;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("clienteId", clienteId);
        map.put("marca", marca);
        map.put("submarca", submarca);
        map.put("color", color);
        map.put("anioModelo", anioModelo);
        map.put("numeroSerie", numeroSerie);
        map.put("placas", placas);
        map.put("fotografia", fotografia);
        return map;
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int integer(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(text(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public String descripcion() {
        return String.format("Marca: %s | Submarca: %s | Año: %d | Placas: %s", marca, submarca, anioModelo, placas).trim();
    }

    @Override
    public String toString() {
        return descripcion();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getSubmarca() {
        return submarca;
    }

    public void setSubmarca(String submarca) {
        this.submarca = submarca;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getAnioModelo() {
        return anioModelo;
    }

    public void setAnioModelo(int anioModelo) {
        this.anioModelo = anioModelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getPlacas() {
        return placas;
    }

    public void setPlacas(String placas) {
        this.placas = placas;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }
}

