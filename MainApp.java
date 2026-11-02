package es.glaux;

import es.glaux.data.StudyRepository;
import es.glaux.model.StudySession;
import es.glaux.model.Subject;
import es.glaux.model.Topic;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

/** Main JavaFX window for the first EstudiaFP version. */
public class MainApp extends Application {
    private StudyRepository repository;

    private final TableView<Subject> subjectsTable = new TableView<>();
    private final TableView<Topic> topicsTable = new TableView<>();
    private final TableView<StudySession> sessionsTable = new TableView<>();
    private final ComboBox<Subject> topicSubjectCombo = new ComboBox<>();
    private final ComboBox<Topic> sessionTopicCombo = new ComboBox<>();

    @Override
    public void start(Stage stage) {
        try {
            repository = new StudyRepository();
        } catch (SQLException e) {
            showError("No se ha podido iniciar la base de datos", e.getMessage());
            return;
        }

        Label title = new Label("EstudiaFP");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label subtitle = new Label("Organiza tus asignaturas, estudia con seguimiento y registra tu progreso.");
        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(18, 20, 12, 20));

        TabPane tabs = new TabPane(createSubjectsTab(), createTopicsTab(), createSessionsTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        BorderPane root = new BorderPane(tabs);
        root.setTop(header);
        root.setStyle("-fx-font-family: 'Arial'; -fx-background-color: #f6f7fb;");

        refreshAll();
        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("EstudiaFP - Gestión del estudio");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();
    }

    private Tab createSubjectsTab() {
        TextField nameField = new TextField();
        nameField.setPromptText("Ej. Programación");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Descripción (opcional)");
        Button addButton = new Button("Añadir asignatura");
        Button deleteButton = new Button("Eliminar seleccionada");

        TableColumn<Subject, String> nameCol = new TableColumn<>("Asignatura");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getName()));
        nameCol.setPrefWidth(250);
        TableColumn<Subject, String> descriptionCol = new TableColumn<>("Descripción");
        descriptionCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getDescription()));
        descriptionCol.setPrefWidth(500);
        subjectsTable.getColumns().setAll(nameCol, descriptionCol);
        subjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        subjectsTable.setPlaceholder(new Label("Todavía no hay asignaturas. Añade la primera arriba."));

        addButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showWarning("Falta el nombre", "Escribe el nombre de la asignatura."); return; }
            try {
                repository.addSubject(name, descriptionField.getText());
                nameField.clear(); descriptionField.clear(); refreshAll();
            } catch (SQLException e) { showError("No se ha podido guardar la asignatura", explainSqlError(e)); }
        });
        deleteButton.setOnAction(event -> {
            Subject selected = subjectsTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showWarning("No hay selección", "Selecciona una asignatura de la tabla."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Se eliminarán también sus temas y sesiones. ¿Quieres continuar?", ButtonType.CANCEL, ButtonType.OK);
            confirm.setHeaderText("Eliminar " + selected.getName());
            confirm.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button -> {
                try { repository.deleteSubject(selected.getId()); refreshAll(); }
                catch (SQLException e) { showError("No se ha podido eliminar", e.getMessage()); }
            });
        });

        HBox fields = new HBox(10, nameField, descriptionField, addButton);
        HBox.setHgrow(nameField, Priority.SOMETIMES); HBox.setHgrow(descriptionField, Priority.ALWAYS);
        VBox content = new VBox(12, new Label("Crea y administra las asignaturas que estás estudiando."), fields, subjectsTable, deleteButton);
        content.setPadding(new Insets(18)); VBox.setVgrow(subjectsTable, Priority.ALWAYS);
        return new Tab("Asignaturas", content);
    }

    private Tab createTopicsTab() {
        TextField nameField = new TextField(); nameField.setPromptText("Nombre del tema");
        TextField descriptionField = new TextField(); descriptionField.setPromptText("Descripción (opcional)");
        topicSubjectCombo.setPromptText("Selecciona asignatura");
        Button addButton = new Button("Añadir tema");
        Button deleteButton = new Button("Eliminar tema seleccionado");

        TableColumn<Topic, String> subjectCol = new TableColumn<>("Asignatura");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getSubjectName()));
        TableColumn<Topic, String> nameCol = new TableColumn<>("Tema");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getName()));
        TableColumn<Topic, String> descriptionCol = new TableColumn<>("Descripción");
        descriptionCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getDescription()));
        topicsTable.getColumns().setAll(subjectCol, nameCol, descriptionCol);
        topicsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        topicsTable.setPlaceholder(new Label("Crea primero una asignatura y después añade sus temas."));

        addButton.setOnAction(event -> {
            Subject subject = topicSubjectCombo.getValue();
            String name = nameField.getText().trim();
            if (subject == null) { showWarning("Falta la asignatura", "Crea y selecciona una asignatura primero."); return; }
            if (name.isEmpty()) { showWarning("Falta el nombre", "Escribe el nombre del tema."); return; }
            try { repository.addTopic(subject.getId(), name, descriptionField.getText()); nameField.clear(); descriptionField.clear(); refreshAll(); }
            catch (SQLException e) { showError("No se ha podido guardar el tema", explainSqlError(e)); }
        });
        deleteButton.setOnAction(event -> {
            Topic selected = topicsTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showWarning("No hay selección", "Selecciona un tema de la tabla."); return; }
            try { repository.deleteTopic(selected.getId()); refreshAll(); }
            catch (SQLException e) { showError("No se ha podido eliminar el tema", e.getMessage()); }
        });

        HBox fields = new HBox(10, topicSubjectCombo, nameField, descriptionField, addButton);
        HBox.setHgrow(nameField, Priority.SOMETIMES); HBox.setHgrow(descriptionField, Priority.ALWAYS);
        VBox content = new VBox(12, new Label("Cada tema pertenece a una asignatura."), fields, topicsTable, deleteButton);
        content.setPadding(new Insets(18)); VBox.setVgrow(topicsTable, Priority.ALWAYS);
        return new Tab("Temas", content);
    }

    private Tab createSessionsTab() {
        sessionTopicCombo.setPromptText("Selecciona tema");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField minutesField = new TextField(); minutesField.setPromptText("Minutos"); minutesField.setPrefWidth(100);
        TextArea notesField = new TextArea(); notesField.setPromptText("Qué has estudiado, resultado o errores detectados...");
        notesField.setPrefRowCount(2);
        Button addButton = new Button("Registrar sesión");
        Button deleteButton = new Button("Eliminar sesión seleccionada");

        TableColumn<StudySession, String> dateCol = new TableColumn<>("Fecha");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getDate()));
        TableColumn<StudySession, String> subjectCol = new TableColumn<>("Asignatura");
        subjectCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getSubjectName()));
        TableColumn<StudySession, String> topicCol = new TableColumn<>("Tema");
        topicCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getTopicName()));
        TableColumn<StudySession, String> minutesCol = new TableColumn<>("Minutos");
        minutesCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(String.valueOf(data.getValue().getMinutes())));
        TableColumn<StudySession, String> notesCol = new TableColumn<>("Notas");
        notesCol.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(data.getValue().getNotes()));
        sessionsTable.getColumns().setAll(dateCol, subjectCol, topicCol, minutesCol, notesCol);
        sessionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        sessionsTable.setPlaceholder(new Label("Cuando termines de estudiar, registra aquí tu sesión."));

        addButton.setOnAction(event -> {
            Topic topic = sessionTopicCombo.getValue();
            if (topic == null) { showWarning("Falta el tema", "Crea y selecciona un tema primero."); return; }
            if (datePicker.getValue() == null) { showWarning("Falta la fecha", "Selecciona la fecha de estudio."); return; }
            int minutes;
            try { minutes = Integer.parseInt(minutesField.getText().trim()); }
            catch (NumberFormatException e) { showWarning("Duración incorrecta", "Introduce los minutos como un número entero, por ejemplo 45."); return; }
            if (minutes <= 0) { showWarning("Duración incorrecta", "La sesión debe durar al menos un minuto."); return; }
            try {
                repository.addSession(topic.getId(), datePicker.getValue().toString(), minutes, notesField.getText());
                minutesField.clear(); notesField.clear(); refreshAll();
            } catch (SQLException e) { showError("No se ha podido registrar la sesión", e.getMessage()); }
        });
        deleteButton.setOnAction(event -> {
            StudySession selected = sessionsTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showWarning("No hay selección", "Selecciona una sesión de la tabla."); return; }
            try { repository.deleteSession(selected.getId()); refreshAll(); }
            catch (SQLException e) { showError("No se ha podido eliminar la sesión", e.getMessage()); }
        });

        HBox fields = new HBox(10, sessionTopicCombo, datePicker, minutesField, addButton);
        VBox content = new VBox(12, new Label("Registra cuánto tiempo has estudiado y anota resultados o dificultades."), fields, notesField, sessionsTable, deleteButton);
        content.setPadding(new Insets(18)); VBox.setVgrow(sessionsTable, Priority.ALWAYS);
        return new Tab("Sesiones de estudio", content);
    }

    private void refreshAll() {
        try {
            subjectsTable.setItems(FXCollections.observableArrayList(repository.getSubjects()));
            topicsTable.setItems(FXCollections.observableArrayList(repository.getTopics()));
            sessionsTable.setItems(FXCollections.observableArrayList(repository.getSessions()));
            topicSubjectCombo.setItems(FXCollections.observableArrayList(repository.getSubjects()));
            sessionTopicCombo.setItems(FXCollections.observableArrayList(repository.getTopics()));
        } catch (SQLException e) { showError("No se han podido cargar los datos", e.getMessage()); }
    }

    private String explainSqlError(SQLException e) {
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
            return "Ya existe una asignatura o tema con ese nombre. Usa un nombre diferente.";
        }
        return e.getMessage();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
