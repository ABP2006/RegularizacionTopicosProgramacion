package com.regularizacion.topicosprogramacion.service;

import com.regularizacion.topicosprogramacion.model.Asesor;
import com.regularizacion.topicosprogramacion.model.Automovil;
import com.regularizacion.topicosprogramacion.model.Cliente;
import com.regularizacion.topicosprogramacion.model.OrdenItem;
import com.regularizacion.topicosprogramacion.model.OrdenServicio;
import com.regularizacion.topicosprogramacion.model.Persona;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DemoDataSeeder {
    private static final String DEMO_PASSWORD = "123456";
    private static final String[][] ASESORES = {
            {"Ana Lopez Martinez", "Femenino", "Servicio", "14500", "ana.lopez"},
            {"Miguel Hernandez Soto", "Masculino", "Mantenimiento", "15200", "miguel.hernandez"},
            {"Laura Castillo Vega", "Femenino", "Garantias", "14800", "laura.castillo"},
            {"Jorge Medina Ruiz", "Masculino", "Hojalateria", "15800", "jorge.medina"},
            {"Patricia Gomez Luna", "Femenino", "Refacciones", "14200", "patricia.gomez"},
            {"Roberto Sanchez Diaz", "Masculino", "Diagnóstico", "16500", "roberto.sanchez"}
    };
    private static final String[][] CLIENTES = {
            {"Carlos Ramirez Torres", "Masculino", "carlos.ramirez"},
            {"Sofia Perez Aguilar", "Femenino", "sofia.perez"},
            {"Daniel Ortega Molina", "Masculino", "daniel.ortega"},
            {"Valeria Cruz Romero", "Femenino", "valeria.cruz"},
            {"Fernando Ibarra Solis", "Masculino", "fernando.ibarra"},
            {"Mariana Salazar Pena", "Femenino", "mariana.salazar"},
            {"Ricardo Navarro Leon", "Masculino", "ricardo.navarro"},
            {"Gabriela Flores Rivas", "Femenino", "gabriela.flores"},
            {"Alberto Vargas Nunez", "Masculino", "alberto.vargas"},
            {"Diana Morales Campos", "Femenino", "diana.morales"},
            {"Emilio Franco Arias", "Masculino", "emilio.franco"},
            {"Natalia Reyes Ponce", "Femenino", "natalia.reyes"},
            {"Hector Luna Silva", "Masculino", "hector.luna"},
            {"Andrea Fuentes Lara", "Femenino", "andrea.fuentes"},
            {"Oscar Rangel Prado", "Masculino", "oscar.rangel"}
    };
    private static final String[][] AUTOMOVILES = {
            {"Nissan", "Sentra", "Azul"}, {"Nissan", "Versa", "Gris"},
            {"Nissan", "March", "Blanco"}, {"Nissan", "Kicks", "Rojo"},
            {"Nissan", "Altima", "Negro"},
            {"Honda", "Civic", "Azul"}, {"Honda", "City", "Blanco"},
            {"Honda", "CR-V", "Plata"}, {"Honda", "HR-V", "Gris"},
            {"Honda", "Accord", "Negro"},
            {"Ford", "Focus", "Rojo"}, {"Ford", "Escape", "Gris"},
            {"Ford", "Fiesta", "Blanco"}, {"Ford", "Ranger", "Azul"},
            {"Ford", "Bronco Sport", "Verde"},
            {"Chevrolet", "Aveo", "Blanco"}, {"Chevrolet", "Onix", "Azul"},
            {"Chevrolet", "Tracker", "Plata"}, {"Chevrolet", "Captiva", "Gris"},
            {"Chevrolet", "Spark", "Rojo"},
            {"Suzuki", "Swift", "Rojo"}, {"Suzuki", "Vitara", "Blanco"},
            {"Suzuki", "Ertiga", "Gris"}, {"Suzuki", "S-Cross", "Azul"},
            {"Suzuki", "Ignis", "Plata"}
    };
    private static final String[][] OPERACIONES = {
            {"Operación", "Servicio preventivo", "950"},
            {"Operación", "Diagnóstico con escáner", "650"},
            {"Operación", "Alineación y balanceo", "780"},
            {"Operación", "Revisión de sistema eléctrico", "720"},
            {"Operación", "Cambio de balatas", "820"},
            {"Operación", "Lavado de inyectores", "1100"}
    };
    private static final String[][] REFACCIONES = {
            {"Refacción", "Filtro de aceite", "260"},
            {"Refacción", "Aceite sintético 5W30", "180"},
            {"Refacción", "Balatas delanteras", "1350"},
            {"Refacción", "Bujías", "620"},
            {"Refacción", "Filtro de aire", "310"},
            {"Refacción", "Anticongelante", "240"},
            {"Refacción", "Limpiaparabrisas", "390"}
    };
    private static final String[] OBSERVACIONES = {
            "Servicio programado por kilometraje",
            "Cliente solicita revisión general",
            "Unidad con ruido en tren delantero",
            "Mantenimiento preventivo y prueba de ruta",
            "Revisión previa a viaje en carretera"
    };

    private final FirestoreService service;

    public DemoDataSeeder(FirestoreService service) {
        this.service = service;
    }

    public DemoSeedResult seed() throws Exception {
        String batch = DateTimeFormatter.ofPattern("MMddHHmmss").format(LocalDateTime.now())
                + String.format("%03d", System.currentTimeMillis() % 1000);
        Random random = new Random(System.nanoTime());
        List<String> asesorLogins = new ArrayList<>();
        List<Asesor> asesores = new ArrayList<>();
        List<Cliente> clientes = new ArrayList<>();
        List<Automovil> automoviles = new ArrayList<>();

        for (int i = 0; i < ASESORES.length; i++) {
            Asesor asesor = asesor(ASESORES[i], batch, i);
            asesor.setId(service.guardarAsesor(asesor));
            asesores.add(asesor);
            asesorLogins.add(asesor.getMail());
        }

        for (int i = 0; i < CLIENTES.length; i++) {
            Cliente cliente = cliente(CLIENTES[i], batch, i);
            cliente.setId(service.guardarCliente(cliente));
            clientes.add(cliente);
        }

        for (int i = 0; i < AUTOMOVILES.length; i++) {
            Cliente cliente = clientes.get(i % clientes.size());
            Automovil automovil = auto(cliente.getId(), AUTOMOVILES[i], batch, i);
            automovil.setId(service.guardarAutomovil(automovil));
            automoviles.add(automovil);
        }

        int ordenes = 60;
        for (int i = 0; i < ordenes; i++) {
            Automovil automovil = automoviles.get(random.nextInt(automoviles.size()));
            Cliente cliente = clienteById(clientes, automovil.getClienteId());
            Asesor asesor = asesores.get(random.nextInt(asesores.size()));
            LocalDateTime fecha = LocalDateTime.now()
                    .minusDays(random.nextInt(365))
                    .withHour(8 + random.nextInt(9))
                    .withMinute(0);
            OrdenServicio orden = orden(cliente, automovil, asesor, "CITA-" + batch + "-" + String.format("%03d", i + 1),
                    15000 + random.nextInt(90000), OBSERVACIONES[random.nextInt(OBSERVACIONES.length)], fecha);
            agregarPartidas(orden, random);
            service.guardarOrden(orden);
        }

        service.marcarDatosDemo();
        return new DemoSeedResult(asesores.size(), clientes.size(), automoviles.size(), ordenes, asesorLogins);
    }

    private Asesor asesor(String[] data, String batch, int index) {
        Asesor asesor = new Asesor();
        String nombre = data[0];
        asesor.setNombreCompleto(nombre);
        direccion(asesor, index);
        asesor.setTelefono("722" + String.format("%07d", 1100000 + index * 137));
        asesor.setRfc(rfc(nombre, batch, index));
        asesor.setMail(data[4] + "." + batch + "@autoprime.mx");
        asesor.setGenero(data[1]);
        asesor.setFechaContratacion(LocalDateTime.now().minusMonths(4L + index * 2L).toLocalDate().toString());
        asesor.setSueldo(Double.parseDouble(data[3]));
        asesor.setDepartamento(data[2]);
        asesor.setPasswordHash(PasswordUtil.sha256(DEMO_PASSWORD));
        return asesor;
    }

    private Cliente cliente(String[] data, String batch, int index) {
        Cliente cliente = new Cliente();
        String nombre = data[0];
        cliente.setNombreCompleto(nombre);
        direccion(cliente, index + 10);
        cliente.setTelefono("722" + String.format("%07d", 2100000 + index * 173));
        cliente.setRfc(rfc(nombre, batch, index + 20));
        cliente.setMail(data[2] + "." + batch + "@example.com");
        cliente.setGenero(data[1]);
        return cliente;
    }

    private Automovil auto(String clienteId, String[] data, String batch, int index) {
        Automovil auto = new Automovil();
        auto.setClienteId(clienteId);
        auto.setMarca(data[0]);
        auto.setSubmarca(data[1]);
        auto.setColor(data[2]);
        auto.setAnioModelo(2018 + index % 7);
        auto.setNumeroSerie(("VIN" + batch + String.format("%06d", index)).toUpperCase(Locale.ROOT));
        auto.setPlacas(String.format("%03d", 100 + index) + "-ATP-" + (char) ('A' + index % 26));
        return auto;
    }

    private OrdenServicio orden(Cliente cliente, Automovil auto, Asesor asesor, String cita, int km, String observaciones, LocalDateTime fecha) {
        OrdenServicio orden = new OrdenServicio();
        orden.setClienteId(cliente.getId());
        orden.setClienteNombre(cliente.getNombreCompleto());
        orden.setAutomovilId(auto.getId());
        orden.setAutomovilDescripcion(auto.descripcion());
        orden.setAsesorId(asesor.getId());
        orden.setAsesorNombre(asesor.getNombreCompleto());
        orden.setFechaCaptura(fecha.toString());
        orden.setFechaIngreso(fecha.plusHours(1).toString());
        orden.setFechaEntrega(fecha.plusDays(1 + Math.abs(cita.hashCode()) % 3).toString());
        orden.setKilometraje(km);
        orden.setNumeroCita(cita);
        orden.setObservaciones(observaciones);
        orden.setItems(new ArrayList<>(List.of()));
        return orden;
    }

    private void agregarPartidas(OrdenServicio orden, Random random) {
        String[] operacion = OPERACIONES[random.nextInt(OPERACIONES.length)];
        orden.getItems().add(item(operacion[0], operacion[1], 1, Double.parseDouble(operacion[2])));
        int refacciones = 1 + random.nextInt(3);
        for (int i = 0; i < refacciones; i++) {
            String[] refaccion = REFACCIONES[random.nextInt(REFACCIONES.length)];
            orden.getItems().add(item(refaccion[0], refaccion[1], 1 + random.nextInt(3), Double.parseDouble(refaccion[2])));
        }
    }

    private Cliente clienteById(List<Cliente> clientes, String id) {
        return clientes.stream().filter(cliente -> cliente.getId().equals(id)).findFirst().orElse(clientes.get(0));
    }

    private void direccion(Persona persona, int index) {
        String[] ciudades = {"Toluca", "Metepec", "Lerma", "Zinacantepec", "San Mateo Atenco"};
        persona.getDireccion().setCalle("Calle Servicio " + (100 + index));
        persona.getDireccion().setCiudad(ciudades[index % ciudades.length]);
        persona.getDireccion().setEstado("Estado de Mexico");
        persona.getDireccion().setPais("Mexico");
        persona.getDireccion().setCodigoPostal(String.valueOf(50000 + index));
    }

    private String rfc(String nombre, String batch, int index) {
        String cleaned = nombre.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        String prefix = (cleaned + "XXXX").substring(0, 4);
        String suffix = batch.substring(Math.max(0, batch.length() - 6));
        return prefix + suffix + String.format("%02d", index);
    }

    private OrdenItem item(String tipo, String descripcion, int cantidad, double precio) {
        OrdenItem item = new OrdenItem();
        item.setTipo(tipo);
        item.setDescripcion(descripcion);
        item.setCantidad(cantidad);
        item.setPrecioUnitario(precio);
        return item;
    }

    public record DemoSeedResult(int asesores, int clientes, int automoviles, int ordenes, List<String> asesorLogins) {
    }
}

