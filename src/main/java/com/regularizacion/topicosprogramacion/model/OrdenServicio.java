package com.regularizacion.topicosprogramacion.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrdenServicio {
    private String id = "";
    private String clienteId = "";
    private String clienteNombre = "";
    private String automovilId = "";
    private String automovilDescripcion = "";
    private String asesorId = "";
    private String asesorNombre = "";
    private String fechaCaptura = "";
    private String fechaIngreso = "";
    private String fechaEntrega = "";
    private int kilometraje;
    private String numeroCita = "";
    private String observaciones = "";
    private List<OrdenItem> items = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static OrdenServicio fromMap(String id, Map<String, Object> map) {
        OrdenServicio orden = new OrdenServicio();
        orden.id = id;
        orden.clienteId = text(map.get("clienteId"));
        orden.clienteNombre = text(map.get("clienteNombre"));
        orden.automovilId = text(map.get("automovilId"));
        orden.automovilDescripcion = text(map.get("automovilDescripcion"));
        orden.asesorId = text(map.get("asesorId"));
        orden.asesorNombre = text(map.get("asesorNombre"));
        orden.fechaCaptura = text(map.get("fechaCaptura"));
        orden.fechaIngreso = text(map.get("fechaIngreso"));
        orden.fechaEntrega = text(map.get("fechaEntrega"));
        orden.kilometraje = integer(map.get("kilometraje"));
        orden.numeroCita = text(map.get("numeroCita"));
        orden.observaciones = text(map.get("observaciones"));
        Object rawItems = map.get("items");
        if (rawItems instanceof List<?> list) {
            for (Object raw : list) {
                if (raw instanceof Map<?, ?> rawMap) {
                    orden.items.add(OrdenItem.fromMap((Map<String, Object>) rawMap));
                }
            }
        }
        return orden;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("clienteId", clienteId);
        map.put("clienteNombre", clienteNombre);
        map.put("automovilId", automovilId);
        map.put("automovilDescripcion", automovilDescripcion);
        map.put("asesorId", asesorId);
        map.put("asesorNombre", asesorNombre);
        map.put("fechaCaptura", fechaCaptura);
        map.put("fechaIngreso", fechaIngreso);
        map.put("fechaEntrega", fechaEntrega);
        map.put("kilometraje", kilometraje);
        map.put("numeroCita", numeroCita);
        map.put("observaciones", observaciones);
        map.put("items", items.stream().map(OrdenItem::toMap).toList());
        map.put("costoTotal", getCostoTotal());
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

    public double getCostoTotal() {
        return items.stream().mapToDouble(OrdenItem::getTotal).sum();
    }

    public String resumen() {
        return numeroCita + " - " + clienteNombre + " - $" + String.format("%.2f", getCostoTotal());
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

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getAutomovilId() {
        return automovilId;
    }

    public void setAutomovilId(String automovilId) {
        this.automovilId = automovilId;
    }

    public String getAutomovilDescripcion() {
        return automovilDescripcion;
    }

    public void setAutomovilDescripcion(String automovilDescripcion) {
        this.automovilDescripcion = automovilDescripcion;
    }

    public String getAsesorId() {
        return asesorId;
    }

    public void setAsesorId(String asesorId) {
        this.asesorId = asesorId;
    }

    public String getAsesorNombre() {
        return asesorNombre;
    }

    public void setAsesorNombre(String asesorNombre) {
        this.asesorNombre = asesorNombre;
    }

    public String getFechaCaptura() {
        return fechaCaptura;
    }

    public void setFechaCaptura(String fechaCaptura) {
        this.fechaCaptura = fechaCaptura;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public int getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(int kilometraje) {
        this.kilometraje = kilometraje;
    }

    public String getNumeroCita() {
        return numeroCita;
    }

    public void setNumeroCita(String numeroCita) {
        this.numeroCita = numeroCita;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<OrdenItem> getItems() {
        return items;
    }

    public void setItems(List<OrdenItem> items) {
        this.items = items;
    }
}

