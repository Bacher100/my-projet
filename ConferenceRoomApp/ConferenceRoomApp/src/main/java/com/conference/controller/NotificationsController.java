package com.conference.controller;

import com.conference.dao.NotificationDAO;
import com.conference.model.Notification;
import com.conference.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

public class NotificationsController {

    @FXML private TableView<Notification> tableNotifications;
    @FXML private TableColumn<Notification, String> colTitre;
    @FXML private TableColumn<Notification, String> colMessage;
    @FXML private TableColumn<Notification, String> colType;
    @FXML private TableColumn<Notification, String> colDate;
    @FXML private Button btnMarquerLu;

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ObservableList<Notification> notificationList = FXCollections.observableArrayList();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitre()));
        colMessage.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMessage()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateCreation() != null
                        ? d.getValue().getDateCreation().format(FORMATTER) : ""));

        tableNotifications.setItems(notificationList);
        loadNotifications();

        btnMarquerLu.setOnAction(e -> handleMarquerLu());
    }

    public void loadNotifications() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        notificationList.setAll(notificationDAO.findByUser(userId));
    }

    @FXML
    public void handleMarquerLu() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        notificationDAO.markAllAsRead(userId);
        loadNotifications();
    }
}
