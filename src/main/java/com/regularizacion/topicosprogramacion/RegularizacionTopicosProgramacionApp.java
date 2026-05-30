package com.regularizacion.topicosprogramacion;

import com.regularizacion.topicosprogramacion.model.Asesor;
import com.regularizacion.topicosprogramacion.model.Automovil;
import com.regularizacion.topicosprogramacion.model.Cliente;
import com.regularizacion.topicosprogramacion.model.Direccion;
import com.regularizacion.topicosprogramacion.model.OrdenItem;
import com.regularizacion.topicosprogramacion.model.OrdenServicio;
import com.regularizacion.topicosprogramacion.service.DemoDataSeeder;
import com.regularizacion.topicosprogramacion.service.FirestoreService;
import com.regularizacion.topicosprogramacion.service.PasswordUtil;
import com.regularizacion.topicosprogramacion.service.PdfReportService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class RegularizacionTopicosProgramacionApp extends Application {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("es", "MX"));
    private static final List<String> ALLOWED_CAR_BRANDS = List.of("Chevrolet", "Nissan", "Suzuki", "Ford", "Honda");

    private Stage stage;
    private FirestoreService service;
    private Asesor asesorSesion;
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private final ObservableList<Automovil> automoviles = FXCollections.observableArrayList();
    private final ObservableList<Asesor> asesores = FXCollections.observableArrayList();
    private final ObservableList<OrdenServicio> ordenes = FXCollections.observableArrayList();

    @FXML
    private TextField loginMail;
    @FXML
    private PasswordField loginPassword;
    @FXML
    private Button loginButton;
    @FXML
    private Button seedButton;
    @FXML
    private Label sessionLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private Tab clientesMainTab;
    @FXML
    private Tab automovilesMainTab;
    @FXML
    private Tab asesoresMainTab;
    @FXML
    private Tab ordenesMainTab;
    @FXML
    private Tab reportesMainTab;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Sistema de Órdenes de Reparación - AutoPrime");
        showLogin();
    }

    private void showLogin() {
        StackPane root = loadFxml("/fxml/login-view.fxml");
        Scene scene = scene(root, 720, 420);
        stage.setScene(scene);
        stage.show();

        loginButton.setOnAction(event -> loginAsync(loginMail.getText(), loginPassword.getText(), loginButton, seedButton));
        seedButton.setOnAction(event -> seedAsync(loginButton, seedButton));
    }

    private void loginAsync(String mail, String password, Button login, Button seed) {
        if (mail == null || mail.isBlank() || password == null || password.isBlank()) {
            warn("Datos incompletos", "Ingresa correo y contraseña.");
            return;
        }

        setLoginBusy(login, seed, true, "Ingresando...");
        Thread worker = new Thread(() -> {
            try {
                ensureService();
                Optional<Asesor> asesor = service.loginAsesor(mail, password);
                if (asesor.isEmpty()) {
                    Platform.runLater(() -> {
                        setLoginBusy(login, seed, false, null);
                        warn("Login incorrecto", "Verifica correo y contraseña. Puedes generar datos demo si aún no alimentaste Firestore.");
                    });
                    return;
                }

                Platform.runLater(() -> {
                    asesorSesion = asesor.get();
                    showMain();
                    actualizarDatos(null);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoginBusy(login, seed, false, null);
                    warn("No se pudo iniciar sesión", ex.getMessage());
                });
            }
        }, "firebase-login");
        worker.setDaemon(true);
        worker.start();
    }

    private void seedAsync(Button login, Button seed) {
        setLoginBusy(login, seed, true, "Creando datos...");
        Thread worker = new Thread(() -> {
            try {
                ensureService();
                DemoDataSeeder.DemoSeedResult result = new DemoDataSeeder(service).seed();
                Platform.runLater(() -> {
                    setLoginBusy(login, seed, false, null);
                    info("Datos demo generados", """
                            Se agregaron:
                            Asesores: %d
                            Clientes: %d
                            Automóviles: %d
                            Órdenes: %d

                            Contraseña para los asesores demo: 123456

                            Correos generados:
                            %s
                            """.formatted(result.asesores(), result.clientes(), result.automoviles(), result.ordenes(), String.join("\n", result.asesorLogins())));
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setLoginBusy(login, seed, false, null);
                    warn("No se pudieron crear datos demo", ex.getMessage());
                });
            }
        }, "firebase-seed");
        worker.setDaemon(true);
        worker.start();
    }

    private void setLoginBusy(Button login, Button seed, boolean busy, String busyText) {
        login.setDisable(busy);
        seed.setDisable(busy);
        login.setText(busy ? busyText : "Ingresar");
        seed.setText("Crear más datos demo");
    }

    private void showMain() {
        BorderPane root = loadFxml("/fxml/main-view.fxml");
        sessionLabel.setText("Sesión: " + asesorSesion.getNombreCompleto());
        refreshButton.setOnAction(e -> actualizarDatos(refreshButton));
        clientesMainTab.setContent(clientesTab());
        automovilesMainTab.setContent(automovilesTab());
        asesoresMainTab.setContent(asesoresTab());
        ordenesMainTab.setContent(ordenesTab());
        reportesMainTab.setContent(reportesTab());
        stage.setScene(scene(root, 1220, 760));
        stage.setMaximized(true);
    }

    private <T> T loadFxml(String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            loader.setController(this);
            return loader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo cargar la vista FXML: " + resource, ex);
        }
    }

    private HBox header() {
        Label title = new Label("Sistema de Órdenes de Reparación");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Sesión: " + asesorSesion.getNombreCompleto());
        subtitle.getStyleClass().add("header-subtitle");
        VBox text = new VBox(2, title, subtitle);
        Button refresh = new Button("Actualizar");
        refresh.setOnAction(e -> actualizarDatos(refresh));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, text, spacer, refresh);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private Tab tab(String text, Region content) {
        Tab tab = new Tab(text, content);
        tab.setClosable(false);
        return tab;
    }

    private Region clientesTab() {
        TableView<Cliente> table = new TableView<>(clientes);
        table.getColumns().add(col("Nombre", c -> c.getNombreCompleto(), 210));
        table.getColumns().add(col("RFC", Cliente::getRfc, 120));
        table.getColumns().add(col("Teléfono", Cliente::getTelefono, 120));
        table.getColumns().add(col("Mail", Cliente::getMail, 190));

        TextField nombre = input();
        TextField calle = input();
        TextField ciudad = input();
        TextField estado = input();
        TextField pais = input("Mexico");
        TextField cp = input();
        TextField telefono = input();
        TextField rfc = input();
        TextField mail = input();
        TextField genero = input();
        StackPane imagen = imagePreview();
        genero.textProperty().addListener((obs, old, value) -> updatePersonaPreview(imagen, value));
        updatePersonaPreview(imagen, genero.getText());

        GridPane form = form();
        addRows(form,
                row("Nombre completo", nombre), row("Calle", calle), row("Ciudad", ciudad),
                row("Estado", estado), row("País", pais), row("C.P.", cp), row("Teléfono", telefono),
                row("RFC", rfc), row("Mail", mail), row("Género", genero));

        Button nuevo = new Button("Nuevo");
        Button guardar = new Button("Guardar");
        guardar.getStyleClass().add("success-button");
        Button eliminar = new Button("Eliminar");
        eliminar.getStyleClass().add("danger-button");
        Button detalles = new Button("Detalles");

        final Cliente[] actual = {null};
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            actual[0] = value;
            if (value == null) {
                return;
            }
            fillPersona(value, nombre, calle, ciudad, estado, pais, cp, telefono, rfc, mail, genero);
        });

        nuevo.setOnAction(e -> {
            actual[0] = null;
            clear(nombre, calle, ciudad, estado, cp, telefono, rfc, mail, genero);
            pais.setText("Mexico");
        });
        guardar.setOnAction(e -> run(() -> {
            Cliente c = actual[0] == null ? new Cliente() : actual[0];
            readPersona(c, nombre, calle, ciudad, estado, pais, cp, telefono, rfc, mail, genero);
            c.setId(service.guardarCliente(c));
            cargarTodo();
            table.setItems(clientes);
        }));
        eliminar.setOnAction(e -> confirm("Eliminar cliente", () -> run(() -> {
            if (actual[0] != null) {
                service.eliminarCliente(actual[0].getId());
                cargarTodo();
            }
        })));
        detalles.setOnAction(e -> {
            if (actual[0] != null) {
                showPersonaDetails("Cliente", actual[0]);
            }
        });

        return split(table, titled("Datos del cliente", formWithPreview(form, imagen), new HBox(8, nuevo, guardar, eliminar, detalles)));
    }

    private Region asesoresTab() {
        TableView<Asesor> table = new TableView<>(asesores);
        table.getColumns().add(col("Nombre", Asesor::getNombreCompleto, 210));
        table.getColumns().add(col("Departamento", Asesor::getDepartamento, 140));
        table.getColumns().add(col("Mail", Asesor::getMail, 190));
        table.getColumns().add(col("Sueldo", a -> MONEY.format(a.getSueldo()), 110));

        TextField nombre = input();
        TextField calle = input();
        TextField ciudad = input();
        TextField estado = input();
        TextField pais = input("Mexico");
        TextField cp = input();
        TextField telefono = input();
        TextField rfc = input();
        TextField mail = input();
        TextField genero = input();
        TextField fecha = input(LocalDate.now().toString());
        TextField sueldo = input();
        TextField departamento = input("Servicio");
        PasswordField password = new PasswordField();
        password.setPromptText("Dejar vacío para conservar contraseña");
        StackPane imagen = imagePreview();
        genero.textProperty().addListener((obs, old, value) -> updatePersonaPreview(imagen, value));
        updatePersonaPreview(imagen, genero.getText());

        GridPane form = form();
        addRows(form,
                row("Nombre completo", nombre), row("Calle", calle), row("Ciudad", ciudad),
                row("Estado", estado), row("País", pais), row("C.P.", cp), row("Teléfono", telefono),
                row("RFC", rfc), row("Mail", mail), row("Género", genero),
                row("Fecha contratación", fecha), row("Sueldo", sueldo), row("Departamento", departamento),
                row("Contraseña", password));

        Button nuevo = new Button("Nuevo");
        Button guardar = new Button("Guardar");
        guardar.getStyleClass().add("success-button");
        Button eliminar = new Button("Eliminar");
        eliminar.getStyleClass().add("danger-button");
        Button detalles = new Button("Detalles");

        final Asesor[] actual = {null};
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            actual[0] = value;
            if (value == null) {
                return;
            }
            fillPersona(value, nombre, calle, ciudad, estado, pais, cp, telefono, rfc, mail, genero);
            fecha.setText(value.getFechaContratacion());
            sueldo.setText(String.valueOf(value.getSueldo()));
            departamento.setText(value.getDepartamento());
            password.clear();
        });

        nuevo.setOnAction(e -> {
            actual[0] = null;
            clear(nombre, calle, ciudad, estado, cp, telefono, rfc, mail, genero, fecha, sueldo, password);
            pais.setText("Mexico");
            departamento.setText("Servicio");
            fecha.setText(LocalDate.now().toString());
        });
        guardar.setOnAction(e -> run(() -> {
            Asesor a = actual[0] == null ? new Asesor() : actual[0];
            readPersona(a, nombre, calle, ciudad, estado, pais, cp, telefono, rfc, mail, genero);
            a.setFechaContratacion(fecha.getText());
            a.setSueldo(decimal(sueldo.getText()));
            a.setDepartamento(departamento.getText());
            if (!password.getText().isBlank()) {
                a.setPasswordHash(PasswordUtil.sha256(password.getText()));
            } else if (a.getPasswordHash().isBlank()) {
                a.setPasswordHash(PasswordUtil.sha256("123456"));
            }
            a.setId(service.guardarAsesor(a));
            cargarTodo();
            table.setItems(asesores);
        }));
        eliminar.setOnAction(e -> confirm("Eliminar asesor", () -> run(() -> {
            if (actual[0] != null) {
                service.eliminarAsesor(actual[0].getId());
                cargarTodo();
            }
        })));
        detalles.setOnAction(e -> {
            if (actual[0] != null) {
                showPersonaDetails("Asesor", actual[0]);
            }
        });

        return split(table, titled("Datos del asesor", formWithPreview(form, imagen), new HBox(8, nuevo, guardar, eliminar, detalles)));
    }

    private Region automovilesTab() {
        TableView<Automovil> table = new TableView<>(automoviles);
        table.getColumns().add(col("Marca", Automovil::getMarca, 120));
        table.getColumns().add(col("Submarca", Automovil::getSubmarca, 140));
        table.getColumns().add(col("Año", a -> String.valueOf(a.getAnioModelo()), 70));
        table.getColumns().add(col("Placas", Automovil::getPlacas, 110));
        table.getColumns().add(col("Cliente", a -> clienteNombre(a.getClienteId()), 210));

        ComboBox<Cliente> cliente = new ComboBox<>(clientes);
        cliente.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> marca = new ComboBox<>(FXCollections.observableArrayList(ALLOWED_CAR_BRANDS));
        marca.setMaxWidth(Double.MAX_VALUE);
        TextField submarca = input();
        TextField color = input();
        TextField anio = input();
        TextField serie = input();
        TextField placas = input();
        StackPane imagen = imagePreview();
        marca.valueProperty().addListener((obs, old, value) -> updateAutoPreview(imagen, value));
        updateAutoPreview(imagen, marca.getValue());

        GridPane form = form();
        addRows(form,
                row("Cliente", cliente), row("Marca", marca), row("Submarca", submarca), row("Color", color),
                row("Año-modelo", anio), row("Número de serie", serie), row("Placas", placas));

        Button nuevo = new Button("Nuevo");
        Button guardar = new Button("Guardar");
        guardar.getStyleClass().add("success-button");
        Button eliminar = new Button("Eliminar");
        eliminar.getStyleClass().add("danger-button");
        Button detalles = new Button("Detalles");

        final Automovil[] actual = {null};
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> {
            actual[0] = value;
            if (value == null) {
                return;
            }
            cliente.setValue(clienteById(value.getClienteId()));
            marca.setValue(value.getMarca());
            submarca.setText(value.getSubmarca());
            color.setText(value.getColor());
            anio.setText(String.valueOf(value.getAnioModelo()));
            serie.setText(value.getNumeroSerie());
            placas.setText(value.getPlacas());
        });

        nuevo.setOnAction(e -> {
            actual[0] = null;
            cliente.setValue(null);
            marca.setValue(null);
            clear(submarca, color, anio, serie, placas);
        });
        guardar.setOnAction(e -> run(() -> {
            if (cliente.getValue() == null) {
                warn("Falta cliente", "Selecciona el cliente propietario del automóvil.");
                return;
            }
            if (marca.getValue() == null || marca.getValue().isBlank()) {
                warn("Falta marca", "Selecciona una marca: Chevrolet, Nissan, Suzuki, Ford u Honda.");
                return;
            }
            Automovil a = actual[0] == null ? new Automovil() : actual[0];
            a.setClienteId(cliente.getValue().getId());
            a.setMarca(marca.getValue());
            a.setSubmarca(submarca.getText());
            a.setColor(color.getText());
            a.setAnioModelo(entero(anio.getText()));
            a.setNumeroSerie(serie.getText());
            a.setPlacas(placas.getText());
            a.setId(service.guardarAutomovil(a));
            cargarTodo();
            table.setItems(automoviles);
        }));
        eliminar.setOnAction(e -> confirm("Eliminar automóvil", () -> run(() -> {
            if (actual[0] != null) {
                service.eliminarAutomovil(actual[0].getId());
                cargarTodo();
            }
        })));
        detalles.setOnAction(e -> {
            if (actual[0] != null) {
                showAutoDetails(actual[0]);
            }
        });

        return split(table, titled("Datos del automóvil", formWithPreview(form, imagen), new HBox(8, nuevo, guardar, eliminar, detalles)));
    }

    private Region ordenesTab() {
        TableView<OrdenServicio> table = new TableView<>(ordenes);
        table.getColumns().add(col("Cita", OrdenServicio::getNumeroCita, 120));
        table.getColumns().add(col("Cliente", OrdenServicio::getClienteNombre, 190));
        table.getColumns().add(col("Automóvil", OrdenServicio::getAutomovilDescripcion, 230));
        table.getColumns().add(col("Asesor", OrdenServicio::getAsesorNombre, 180));
        table.getColumns().add(col("Total", o -> MONEY.format(o.getCostoTotal()), 110));

        ComboBox<Cliente> cliente = new ComboBox<>(clientes);
        ComboBox<Automovil> auto = new ComboBox<>(automoviles);
        ComboBox<Asesor> asesor = new ComboBox<>(asesores);
        asesor.setValue(asesorSesion);
        TextField captura = input(LocalDateTime.now().toString());
        TextField ingreso = input(LocalDateTime.now().plusHours(1).toString());
        TextField entrega = input(LocalDateTime.now().plusDays(1).toString());
        TextField km = input();
        TextField cita = input("CITA-" + System.currentTimeMillis() % 100000);
        TextArea obs = new TextArea();
        obs.setPrefRowCount(3);

        ObservableList<OrdenItem> items = FXCollections.observableArrayList();
        TableView<OrdenItem> itemTable = new TableView<>(items);
        itemTable.setPrefHeight(190);
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        itemTable.getColumns().add(col("Tipo", OrdenItem::getTipo, 95));
        itemTable.getColumns().add(col("Descripción", OrdenItem::getDescripcion, 240));
        itemTable.getColumns().add(col("Cantidad", i -> String.valueOf(i.getCantidad()), 80));
        itemTable.getColumns().add(col("Precio", i -> MONEY.format(i.getPrecioUnitario()), 90));
        itemTable.getColumns().add(col("Total", i -> MONEY.format(i.getTotal()), 90));

        ComboBox<String> tipoItem = new ComboBox<>(FXCollections.observableArrayList("Operación", "Refacción"));
        tipoItem.setValue("Operación");
        TextField descItem = input();
        TextField cantItem = input("1");
        TextField precioItem = input();
        Button addItem = new Button("Agregar partida");
        Button removeItem = new Button("Quitar partida");
        HBox itemControls = new HBox(8, tipoItem, descItem, cantItem, precioItem, addItem, removeItem);
        HBox.setHgrow(descItem, Priority.ALWAYS);

        Label total = new Label("Total: $0.00");
        total.getStyleClass().add("section-title");
        Runnable updateTotal = () -> total.setText("Total: " + MONEY.format(items.stream().mapToDouble(OrdenItem::getTotal).sum()));

        addItem.setOnAction(e -> {
            OrdenItem item = new OrdenItem();
            item.setTipo(tipoItem.getValue());
            item.setDescripcion(descItem.getText());
            item.setCantidad(entero(cantItem.getText()));
            item.setPrecioUnitario(decimal(precioItem.getText()));
            items.add(item);
            itemTable.refresh();
            updateTotal.run();
            clear(descItem, precioItem);
            cantItem.setText("1");
        });
        removeItem.setOnAction(e -> {
            OrdenItem selected = itemTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                items.remove(selected);
                updateTotal.run();
            }
        });

        GridPane form = form();
        addRows(form,
                row("Cliente", cliente), row("Automóvil", auto), row("Asesor", asesor),
                row("Fecha captura", captura), row("Fecha ingreso", ingreso), row("Fecha entrega", entrega),
                row("Kilometraje", km), row("Número cita", cita), row("Observaciones", obs));
        addRows(form, row("Partidas", new VBox(8, itemTable, itemControls, total)));

        Button nuevo = new Button("Nueva");
        Button guardar = new Button("Guardar");
        guardar.getStyleClass().add("success-button");
        Button eliminar = new Button("Eliminar");
        eliminar.getStyleClass().add("danger-button");
        Button pdf = new Button("Generar PDF");
        pdf.getStyleClass().add("primary-button");

        final OrdenServicio[] actual = {null};
        table.getSelectionModel().selectedItemProperty().addListener((obsValue, old, value) -> {
            actual[0] = value;
            if (value == null) {
                return;
            }
            cliente.setValue(clienteById(value.getClienteId()));
            auto.setValue(autoById(value.getAutomovilId()));
            asesor.setValue(asesorById(value.getAsesorId()));
            captura.setText(value.getFechaCaptura());
            ingreso.setText(value.getFechaIngreso());
            entrega.setText(value.getFechaEntrega());
            km.setText(String.valueOf(value.getKilometraje()));
            cita.setText(value.getNumeroCita());
            obs.setText(value.getObservaciones());
            items.setAll(value.getItems());
            updateTotal.run();
        });

        nuevo.setOnAction(e -> {
            actual[0] = null;
            cliente.setValue(null);
            auto.setValue(null);
            asesor.setValue(asesorSesion);
            captura.setText(LocalDateTime.now().toString());
            ingreso.setText(LocalDateTime.now().plusHours(1).toString());
            entrega.setText(LocalDateTime.now().plusDays(1).toString());
            km.clear();
            cita.setText("CITA-" + System.currentTimeMillis() % 100000);
            obs.clear();
            items.clear();
            updateTotal.run();
        });
        guardar.setOnAction(e -> run(() -> {
            if (cliente.getValue() == null || auto.getValue() == null || asesor.getValue() == null || items.isEmpty()) {
                warn("Orden incompleta", "Selecciona cliente, automóvil, asesor y agrega al menos una partida.");
                return;
            }
            OrdenServicio orden = actual[0] == null ? new OrdenServicio() : actual[0];
            orden.setClienteId(cliente.getValue().getId());
            orden.setClienteNombre(cliente.getValue().getNombreCompleto());
            orden.setAutomovilId(auto.getValue().getId());
            orden.setAutomovilDescripcion(auto.getValue().descripcion());
            orden.setAsesorId(asesor.getValue().getId());
            orden.setAsesorNombre(asesor.getValue().getNombreCompleto());
            orden.setFechaCaptura(captura.getText());
            orden.setFechaIngreso(ingreso.getText());
            orden.setFechaEntrega(entrega.getText());
            orden.setKilometraje(entero(km.getText()));
            orden.setNumeroCita(cita.getText());
            orden.setObservaciones(obs.getText());
            orden.setItems(List.copyOf(items));
            orden.setId(service.guardarOrden(orden));
            cargarTodo();
            table.setItems(ordenes);
        }));
        eliminar.setOnAction(e -> confirm("Eliminar orden", () -> run(() -> {
            if (actual[0] != null) {
                service.eliminarOrden(actual[0].getId());
                cargarTodo();
            }
        })));
        pdf.setOnAction(e -> {
            OrdenServicio selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                selected = actual[0];
            }
            if (selected == null) {
                warn("Selecciona una orden", "Elige una orden para generar el PDF.");
                return;
            }
            OrdenServicio finalSelected = selected;
            run(() -> {
                Path output = Path.of("ordenes_pdf");
                Path pdfFile = new PdfReportService().generarOrdenPdf(
                        finalSelected,
                        clienteById(finalSelected.getClienteId()),
                        autoById(finalSelected.getAutomovilId()),
                        asesorById(finalSelected.getAsesorId()),
                        output
                );
                info("PDF generado", pdfFile.toAbsolutePath().toString());
            });
        });

        return split(table, titled("Orden de reparación / mantenimiento", form, new HBox(8, nuevo, guardar, eliminar, pdf)));
    }

    private Region reportesTab() {
        DatePicker inicio = new DatePicker(LocalDate.now().minusMonths(2));
        DatePicker fin = new DatePicker(LocalDate.now());
        Spinner<Integer> anio = new Spinner<>(2020, 2035, LocalDate.now().getYear());
        Button porAsesor = new Button("Órdenes por asesor");
        Button ingresos = new Button("Ingresos mensuales");
        StackPane chartBox = new StackPane();
        chartBox.setPadding(new Insets(15));

        String[] reporteActivo = {"asesor"};
        Runnable actualizarReporte = () -> {
            if ("ingresos".equals(reporteActivo[0])) {
                chartBox.getChildren().setAll(chartIngresosMensuales(anio.getValue()));
            } else {
                chartBox.getChildren().setAll(chartOrdenesPorAsesor(inicio.getValue(), fin.getValue()));
            }
        };

        porAsesor.setOnAction(e -> {
            reporteActivo[0] = "asesor";
            actualizarReporte.run();
        });
        ingresos.setOnAction(e -> {
            reporteActivo[0] = "ingresos";
            actualizarReporte.run();
        });
        inicio.valueProperty().addListener((obs, old, value) -> actualizarReporte.run());
        fin.valueProperty().addListener((obs, old, value) -> actualizarReporte.run());
        anio.valueProperty().addListener((obs, old, value) -> actualizarReporte.run());
        actualizarReporte.run();

        HBox controls = new HBox(10, new Label("Inicio"), inicio, new Label("Fin"), fin, porAsesor, new Label("Año"), anio, ingresos);
        controls.getStyleClass().add("toolbar");
        BorderPane pane = new BorderPane(chartBox);
        pane.setTop(controls);
        return pane;
    }

    private BarChart<String, Number> chartOrdenesPorAsesor(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            LocalDate temp = inicio;
            inicio = fin;
            fin = temp;
        }
        final LocalDate filtroInicio = inicio;
        final LocalDate filtroFin = fin;

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Totales de órdenes atendidas por asesor");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(filtroInicio + " a " + filtroFin);
        Map<String, Long> data = ordenes.stream()
                .filter(o -> between(dateOf(o.getFechaCaptura()), filtroInicio, filtroFin))
                .collect(Collectors.groupingBy(this::asesorReportKey, LinkedHashMap::new, Collectors.counting()));
        data.forEach((asesorKey, total) -> series.getData().add(new XYChart.Data<>(asesorReportLabel(asesorKey), total)));
        chart.getData().add(series);
        return chart;
    }

    private BarChart<String, Number> chartIngresosMensuales(int anio) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Ingresos mensuales por reparaciones y mantenimientos");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(String.valueOf(anio));
        Map<Month, Double> data = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            data.put(month, 0.0);
        }
        for (OrdenServicio orden : ordenes) {
            LocalDate date = dateOf(orden.getFechaCaptura());
            if (date != null && date.getYear() == anio) {
                data.put(date.getMonth(), data.get(date.getMonth()) + orden.getCostoTotal());
            }
        }
        data.forEach((month, total) -> series.getData().add(new XYChart.Data<>(month.name().substring(0, 3), total)));
        chart.getData().add(series);
        return chart;
    }

    private void cargarTodo() throws Exception {
        clientes.setAll(service.listarClientes());
        automoviles.setAll(service.listarAutomoviles());
        asesores.setAll(service.listarAsesores());
        ordenes.setAll(service.listarOrdenes());
    }

    private DataSnapshot cargarSnapshot() throws Exception {
        CompletableFuture<List<Cliente>> clientesFuture = CompletableFuture.supplyAsync(() -> fetch(() -> service.listarClientes()));
        CompletableFuture<List<Automovil>> automovilesFuture = CompletableFuture.supplyAsync(() -> fetch(() -> service.listarAutomoviles()));
        CompletableFuture<List<Asesor>> asesoresFuture = CompletableFuture.supplyAsync(() -> fetch(() -> service.listarAsesores()));
        CompletableFuture<List<OrdenServicio>> ordenesFuture = CompletableFuture.supplyAsync(() -> fetch(() -> service.listarOrdenes()));

        try {
            return new DataSnapshot(
                    clientesFuture.join(),
                    automovilesFuture.join(),
                    asesoresFuture.join(),
                    ordenesFuture.join()
            );
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw ex;
        }
    }

    private void aplicarSnapshot(DataSnapshot snapshot) {
        clientes.setAll(snapshot.clientes());
        automoviles.setAll(snapshot.automoviles());
        asesores.setAll(snapshot.asesores());
        ordenes.setAll(snapshot.ordenes());
    }

    private void actualizarDatos(Button refresh) {
        if (refresh != null) {
            refresh.setDisable(true);
            refresh.setText("Actualizando...");
        }
        Thread updater = new Thread(() -> {
            try {
                DataSnapshot snapshot = cargarSnapshot();
                Platform.runLater(() -> {
                    aplicarSnapshot(snapshot);
                    showMain();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    if (refresh != null) {
                        refresh.setDisable(false);
                        refresh.setText("Actualizar");
                    }
                    warn("No se pudo actualizar", ex.getMessage());
                });
            }
        }, "firestore-refresh");
        updater.setDaemon(true);
        updater.start();
    }

    private <T> T fetch(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    private void ensureService() {
        if (service == null) {
            service = new FirestoreService();
        }
    }

    private SplitPane split(TableView<?> table, VBox form) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.55);
        split.setPadding(new Insets(12));
        return split;
    }

    private VBox titled(String title, Region form, HBox buttons) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        buttons.setAlignment(Pos.CENTER_RIGHT);
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox box = new VBox(12, label, scroll, buttons);
        box.getStyleClass().add("panel");
        box.setMinWidth(420);
        return box;
    }

    private VBox formWithPreview(Region form, StackPane preview) {
        VBox box = new VBox(8, form, preview);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    private StackPane imagePreview() {
        StackPane preview = new StackPane();
        preview.setPrefSize(260, 180);
        preview.setMaxWidth(260);
        return preview;
    }

    private void updatePersonaPreview(StackPane preview, String genero) {
        preview.getChildren().setAll(personaImage(genero));
    }

    private void updateAutoPreview(StackPane preview, String marca) {
        preview.getChildren().setAll(autoImage(marca));
    }

    private GridPane form() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(4));
        return grid;
    }

    private Object[] row(String label, Region input) {
        input.setMaxWidth(Double.MAX_VALUE);
        setFieldPrompt(label, input);
        return new Object[]{new Label(label), input};
    }

    private void addRows(GridPane grid, Object[]... rows) {
        int startRow = grid.getRowCount();
        for (int i = 0; i < rows.length; i++) {
            Label label = (Label) rows[i][0];
            Region input = (Region) rows[i][1];
            label.getStyleClass().add("field-label");
            label.setMinWidth(155);
            label.setPrefWidth(155);
            label.setMaxWidth(155);
            label.setWrapText(true);
            label.setAlignment(Pos.CENTER_LEFT);

            HBox row = new HBox(12, label, input);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(input, Priority.ALWAYS);
            GridPane.setHgrow(row, Priority.ALWAYS);
            grid.add(row, 0, startRow + i, 2, 1);
        }
    }

    private void setFieldPrompt(String label, Region input) {
        if (input instanceof TextInputControl textInput && textInput.getPromptText() == null) {
            textInput.setPromptText(label);
        } else if (input instanceof ComboBox<?> comboBox && comboBox.getPromptText() == null) {
            comboBox.setPromptText(label);
        }
    }

    private TextField input() {
        return input("");
    }

    private TextField input(String value) {
        TextField field = new TextField(value);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> getter, int width) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private VBox title(String heading, String subtitle) {
        Label h = new Label(heading);
        h.getStyleClass().add("section-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("muted");
        return new VBox(4, h, s);
    }

    private Scene scene(Region root, int width, int height) {
        Scene scene = new Scene(root, width, height);
        String css = getClass().getResource("/styles/app.css").toExternalForm();
        scene.getStylesheets().add(css);
        return scene;
    }

    private void fillPersona(com.regularizacion.topicosprogramacion.model.Persona p, TextField nombre, TextField calle, TextField ciudad,
                             TextField estado, TextField pais, TextField cp, TextField telefono, TextField rfc,
                             TextField mail, TextField genero) {
        nombre.setText(p.getNombreCompleto());
        calle.setText(p.getDireccion().getCalle());
        ciudad.setText(p.getDireccion().getCiudad());
        estado.setText(p.getDireccion().getEstado());
        pais.setText(p.getDireccion().getPais());
        cp.setText(p.getDireccion().getCodigoPostal());
        telefono.setText(p.getTelefono());
        rfc.setText(p.getRfc());
        mail.setText(p.getMail());
        genero.setText(p.getGenero());
    }

    private void readPersona(com.regularizacion.topicosprogramacion.model.Persona p, TextField nombre, TextField calle, TextField ciudad,
                             TextField estado, TextField pais, TextField cp, TextField telefono, TextField rfc,
                             TextField mail, TextField genero) {
        p.setNombreCompleto(nombre.getText());
        Direccion direccion = p.getDireccion();
        direccion.setCalle(calle.getText());
        direccion.setCiudad(ciudad.getText());
        direccion.setEstado(estado.getText());
        direccion.setPais(pais.getText());
        direccion.setCodigoPostal(cp.getText());
        p.setTelefono(telefono.getText());
        p.setRfc(rfc.getText());
        p.setMail(mail.getText());
        p.setGenero(genero.getText());
    }

    private void showPersonaDetails(String title, com.regularizacion.topicosprogramacion.model.Persona p) {
        String details = """
                Nombre: %s
                Dirección: %s
                Teléfono: %s
                RFC: %s
                Mail: %s
                Género: %s
                """.formatted(p.getNombreCompleto(), p.getDireccion().asSingleLine(), p.getTelefono(), p.getRfc(), p.getMail(), p.getGenero());
        showDetails(title, details, personaImage(p.getGenero()));
    }

    private void showAutoDetails(Automovil a) {
        String details = """
                Cliente: %s
                Marca: %s
                Submarca: %s
                Color: %s
                Año-modelo: %d
                Número de serie: %s
                Placas: %s
                """.formatted(clienteNombre(a.getClienteId()), a.getMarca(), a.getSubmarca(), a.getColor(), a.getAnioModelo(), a.getNumeroSerie(), a.getPlacas());
        showDetails("Automóvil", details, autoImage(a.getMarca()));
    }

    private void showDetails(String title, String text, Node visual) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setPrefColumnCount(48);
        area.setPrefRowCount(8);
        VBox content = new VBox(10, area, visual);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private Node personaImage(String genero) {
        String normalized = normalize(genero);
        if (normalized.contains("femenino") || normalized.equals("f") || normalized.contains("mujer")) {
            return resourceImage("Mujer");
        }
        if (normalized.contains("masculino") || normalized.equals("m") || normalized.contains("hombre")) {
            return resourceImage("Hombre");
        }
        return generatedImage("Perfil general", "P", "Género sin clasificar", "#f3f4f6", "#4b5563");
    }

    private Node autoImage(String marca) {
        String normalized = normalize(marca);
        String brand = marca == null || marca.isBlank() ? "Marca no definida" : marca.trim();
        String initials = brand.equals("Marca no definida") ? "?" : brand.substring(0, Math.min(2, brand.length())).toUpperCase(Locale.ROOT);
        if (normalized.contains("nissan")) {
            return resourceImage("Nissan");
        }
        if (normalized.contains("honda")) {
            return resourceImage("Honda");
        }
        if (normalized.contains("ford")) {
            return resourceImage("Ford");
        }
        if (normalized.contains("chevrolet")) {
            return resourceImage("Chevrolet");
        }
        if (normalized.contains("suzuki")) {
            return resourceImage("Suzuki");
        }
        return generatedImage(brand, initials, "Imagen asignada por marca", "#ecfdf5", "#047857");
    }

    private Node resourceImage(String imageName) {
        var resource = getClass().getResource("/images/" + imageName + ".png");
        if (resource == null) {
            return generatedImage(imageName, "?", "Imagen no encontrada", "#f3f4f6", "#4b5563");
        }
        ImageView imageView = new ImageView(new Image(resource.toExternalForm()));
        imageView.setFitWidth(260);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        StackPane frame = new StackPane(imageView);
        frame.setPrefSize(260, 180);
        frame.setMaxWidth(260);
        frame.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #d1d5db; -fx-border-width: 1; -fx-padding: 10;");
        return frame;
    }

    private Node generatedImage(String title, String initials, String subtitle, String background, String foreground) {
        Label icon = new Label(initials);
        icon.setMinSize(86, 86);
        icon.setMaxSize(86, 86);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color: " + foreground + "; -fx-background-radius: 43; -fx-text-fill: white; -fx-font-size: 34px; -fx-font-weight: bold;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + foreground + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted");

        VBox content = new VBox(8, icon, titleLabel, subtitleLabel);
        content.setAlignment(Pos.CENTER);

        StackPane image = new StackPane(content);
        image.setPrefSize(260, 180);
        image.setMaxWidth(260);
        image.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: " + foreground + "; -fx-border-width: 1.5; -fx-padding: 14;");
        return image;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private Cliente clienteById(String id) {
        return clientes.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    private Automovil autoById(String id) {
        return automoviles.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    private Asesor asesorById(String id) {
        return asesores.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(asesorSesion);
    }

    private String clienteNombre(String id) {
        Cliente cliente = clienteById(id);
        return cliente == null ? "Sin cliente" : cliente.getNombreCompleto();
    }

    private LocalDate dateOf(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private boolean between(LocalDate value, LocalDate start, LocalDate end) {
        if (value == null) {
            return false;
        }
        boolean afterStart = start == null || value.isEqual(start) || value.isAfter(start);
        boolean beforeEnd = end == null || value.isEqual(end) || value.isBefore(end);
        return afterStart && beforeEnd;
    }

    private String asesorReportKey(OrdenServicio orden) {
        if (orden.getAsesorId() != null && !orden.getAsesorId().isBlank()) {
            return orden.getAsesorId();
        }
        return "nombre:" + value(orden.getAsesorNombre(), "Sin asesor");
    }

    private String asesorReportLabel(String key) {
        if (key == null || key.isBlank()) {
            return "Sin asesor";
        }
        if (key.startsWith("nombre:")) {
            return key.substring("nombre:".length()) + " (sin ID)";
        }
        Asesor asesor = asesorById(key);
        if (asesor == null) {
            return "Asesor eliminado (" + shortId(key) + ")";
        }
        String correo = asesor.getMail() == null || asesor.getMail().isBlank() ? shortId(key) : asesor.getMail();
        return asesor.getNombreCompleto() + " | " + correo;
    }

    private String shortId(String id) {
        return id == null || id.length() <= 6 ? String.valueOf(id) : id.substring(0, 6);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int entero(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private double decimal(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void clear(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private void run(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            warn("Ocurrió un error", ex.getMessage());
        }
    }

    private void confirm(String title, Runnable action) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Esta acción no se puede deshacer.", ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait().filter(ButtonType.OK::equals).ifPresent(e -> action.run());
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private void warn(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message == null ? "Sin detalle" : message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    private record DataSnapshot(
            List<Cliente> clientes,
            List<Automovil> automoviles,
            List<Asesor> asesores,
            List<OrdenServicio> ordenes
    ) {
    }
}

