package com.github.marcelomrwin.javafx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import jakarta.inject.Singleton;

import com.github.marcelomrwin.model.Task;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionSemantics;
import io.quarkus.panache.common.Sort;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FXController {

    @FXML
    TextField name;
    @FXML
    TextField description;
    @FXML
    DatePicker deadline;
    @FXML
    Button saveButton;
    @FXML
    Button cancelButton;

    @FXML
    TableView table;
    Task selectedTask = null;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @FXML
    void initialize() {
        logger.info("Application is initializing");
        configureTable();
        populateTable();
        configureActions();
    }

    private void configureActions() {
        TableView.TableViewSelectionModel selectionModel = table.getSelectionModel();
        ObservableList<Task> selectedItems =
                selectionModel.getSelectedItems();

        selectedItems.addListener(
                (ListChangeListener<Task>) change -> {
                    ObservableList<? extends Task> changeList = change.getList();
                    if (changeList.isEmpty()) {
                        clearForm();
                    } else {
                        selectedTask = changeList.stream().findFirst().get();
                        populateTaskForm(selectedTask);
                    }
                });

        saveButton.setOnAction(actionEvent -> {
            try {
                if (selectedTask != null) {
                    //update
                    QuarkusTransaction.requiringNew().run(() -> Task.update("name = ?1, description = ?2, deadLine = ?3 where id = ?4", name.getText(), description.getText(), deadline.getValue().atStartOfDay(), selectedTask.getId()));
                } else {
                    //create
                    Task task = createTaskFromForm();
                    QuarkusTransaction.requiringNew().run(task::persistAndFlush);
                }
                populateTable();
                clearForm();
            } catch (Exception e) {
                showExceptionDialog(e);
            }
        });

        cancelButton.setOnAction(actionEvent -> {
            table.getSelectionModel().clearSelection();
        });

        TableColumn<Task, Void> deleteColumn = new TableColumn<>();
        Callback<TableColumn<Task, Void>, TableCell<Task, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Task, Void> call(TableColumn<Task, Void> taskVoidTableColumn) {
                final TableCell<Task, Void> cell = new TableCell<Task, Void>() {

                    private final Button btn = new Button("Delete");

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            Task task = getTableView().getItems().get(getIndex());
                            QuarkusTransaction.requiringNew().run(task::delete);
                            populateTable();
                        });
                    }
                };
                return cell;
            }
        };
        deleteColumn.setCellFactory(cellFactory);
        table.getColumns().add(deleteColumn);
    }

    private Task createTaskFromForm() {
        return Task.builder()
                .name(name.getText())
                .description(description.getText())
                .deadLine(deadline.getValue().atStartOfDay())
                .build();
    }

    private void populateTaskForm(Task task) {
        name.setText(task.getName());
        description.setText(task.getDescription());
        deadline.setValue(task.getDeadLine().toLocalDate());
    }

    private void clearForm() {
        selectedTask = null;
        name.setText(null);
        description.setText(null);
        deadline.setValue(null);
    }

    private void populateTable() {
        List<Task> tasks = QuarkusTransaction.runner(TransactionSemantics.DISALLOW_EXISTING).call(() -> Task.listAll(Sort.ascending("id")));
        table.getItems().clear();
        for (Task task : tasks) {
            table.getItems().add(task);
        }
    }

    private void configureTable() {
        logger.info("configuring table");
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getColumns().clear();

        TableColumn<Task, Integer> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(25);
        idColumn.setMaxWidth(25);
        table.getColumns().add(idColumn);

        TableColumn<Task, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);
        nameColumn.setMaxWidth(150);
        table.getColumns().add(nameColumn);

        TableColumn<Task, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(250);
        descriptionColumn.setMaxWidth(300);
        table.getColumns().add(descriptionColumn);

        TableColumn<Task, String> deadLineColumn = new TableColumn<>("Deadline");
        deadLineColumn.setCellValueFactory(new PropertyValueFactory<>("deadLine"));
        table.getColumns().add(deadLineColumn);

    }

    private void showExceptionDialog(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("An error occurred while trying to perform the operation");
        alert.setContentText(ex.getMessage());

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

}
