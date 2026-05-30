package com.regularizacion.topicosprogramacion.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cliente extends Persona {
    public static Cliente fromMap(String id, Map<String, Object> map) {
        Cliente cliente = new Cliente();
        cliente.id = id;
        cliente.readCommon(map);
        return cliente;
    }

    public Map<String, Object> toMap() {
        return writeCommon(new LinkedHashMap<>());
    }

    @Override
    public String toString() {
        return "Cliente: " + nombreCompleto + " | Correo: " + mail;
    }
}

