package com.regularizacion.topicosprogramacion.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class OrdenItem {
    private String tipo = "Operacion";
    private String descripcion = "";
    private int cantidad = 1;
    private double precioUnitario;

    public static OrdenItem fromMap(Map<String, Object> map) {
        OrdenItem item = new OrdenItem();
        item.tipo = text(map.get("tipo"));
        item.descripcion = text(map.get("descripcion"));
        item.cantidad = integer(map.get("cantidad"));
        item.precioUnitario = number(map.get("precioUnitario"));
        return item;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tipo", tipo);
        map.put("descripcion", descripcion);
        map.put("cantidad", cantidad);
        map.put("precioUnitario", precioUnitario);
        map.put("total", getTotal());
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

    private static double number(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(text(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public double getTotal() {
        return cantidad * precioUnitario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}

