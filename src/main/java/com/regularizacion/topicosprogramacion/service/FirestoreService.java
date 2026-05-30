package com.regularizacion.topicosprogramacion.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.regularizacion.topicosprogramacion.model.Asesor;
import com.regularizacion.topicosprogramacion.model.Automovil;
import com.regularizacion.topicosprogramacion.model.Cliente;
import com.regularizacion.topicosprogramacion.model.OrdenServicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class FirestoreService {
    private final Firestore db;

    public FirestoreService() {
        this.db = FirestoreProvider.getDb();
    }

    public Optional<Asesor> loginAsesor(String mail, String password) throws ExecutionException, InterruptedException {
        String normalizedMail = mail == null ? "" : mail.trim().toLowerCase();
        if (normalizedMail.isBlank()) {
            return Optional.empty();
        }
        String hash = PasswordUtil.sha256(password);
        QuerySnapshot snapshot = db.collection("asesores")
                .whereEqualTo("mail", normalizedMail)
                .limit(1)
                .get()
                .get();
        if (snapshot.isEmpty()) {
            return Optional.empty();
        }

        QueryDocumentSnapshot doc = snapshot.getDocuments().get(0);
        Asesor asesor = Asesor.fromMap(doc.getId(), doc.getData());
        return asesor.getPasswordHash().equals(hash) ? Optional.of(asesor) : Optional.empty();
    }

    public List<Cliente> listarClientes() throws ExecutionException, InterruptedException {
        List<Cliente> clientes = new ArrayList<>();
        QuerySnapshot snapshot = db.collection("clientes").get().get();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            clientes.add(Cliente.fromMap(doc.getId(), doc.getData()));
        }
        clientes.sort(Comparator.comparing(Cliente::getNombreCompleto, String.CASE_INSENSITIVE_ORDER));
        return clientes;
    }

    public List<Automovil> listarAutomoviles() throws ExecutionException, InterruptedException {
        List<Automovil> automoviles = new ArrayList<>();
        QuerySnapshot snapshot = db.collection("automoviles").get().get();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            automoviles.add(Automovil.fromMap(doc.getId(), doc.getData()));
        }
        automoviles.sort(Comparator.comparing(Automovil::descripcion, String.CASE_INSENSITIVE_ORDER));
        return automoviles;
    }

    public List<Asesor> listarAsesores() throws ExecutionException, InterruptedException {
        List<Asesor> asesores = new ArrayList<>();
        QuerySnapshot snapshot = db.collection("asesores").get().get();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            asesores.add(Asesor.fromMap(doc.getId(), doc.getData()));
        }
        asesores.sort(Comparator.comparing(Asesor::getNombreCompleto, String.CASE_INSENSITIVE_ORDER));
        return asesores;
    }

    public List<OrdenServicio> listarOrdenes() throws ExecutionException, InterruptedException {
        List<OrdenServicio> ordenes = new ArrayList<>();
        QuerySnapshot snapshot = db.collection("ordenes").get().get();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            ordenes.add(OrdenServicio.fromMap(doc.getId(), doc.getData()));
        }
        ordenes.sort(Comparator.comparing(OrdenServicio::getFechaCaptura).reversed());
        return ordenes;
    }

    public String guardarCliente(Cliente cliente) throws ExecutionException, InterruptedException {
        return guardar("clientes", cliente.getId(), cliente.toMap());
    }

    public String guardarAutomovil(Automovil automovil) throws ExecutionException, InterruptedException {
        return guardar("automoviles", automovil.getId(), automovil.toMap());
    }

    public String guardarAsesor(Asesor asesor) throws ExecutionException, InterruptedException {
        return guardar("asesores", asesor.getId(), asesor.toMap());
    }

    public String guardarOrden(OrdenServicio orden) throws ExecutionException, InterruptedException {
        return guardar("ordenes", orden.getId(), orden.toMap());
    }

    public void eliminarCliente(String id) throws ExecutionException, InterruptedException {
        eliminar("clientes", id);
    }

    public void eliminarAutomovil(String id) throws ExecutionException, InterruptedException {
        eliminar("automoviles", id);
    }

    public void eliminarAsesor(String id) throws ExecutionException, InterruptedException {
        eliminar("asesores", id);
    }

    public void eliminarOrden(String id) throws ExecutionException, InterruptedException {
        eliminar("ordenes", id);
    }

    private String guardar(String collection, String id, Object data) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection(collection);
        DocumentReference doc = (id == null || id.isBlank()) ? ref.document() : ref.document(id);
        ApiFuture<?> future = doc.set(data);
        future.get();
        return doc.getId();
    }

    private void eliminar(String collection, String id) throws ExecutionException, InterruptedException {
        if (id == null || id.isBlank()) {
            return;
        }
        db.collection(collection).document(id).delete().get();
    }

    public boolean tieneDatosDemo() throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("_meta").document("demo").get().get();
        return doc.exists();
    }

    public void marcarDatosDemo() throws ExecutionException, InterruptedException {
        db.collection("_meta").document("demo").set(java.util.Map.of("creado", java.time.LocalDateTime.now().toString())).get();
    }
}

