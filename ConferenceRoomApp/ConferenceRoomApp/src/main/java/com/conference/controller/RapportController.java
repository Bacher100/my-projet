package com.conference.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.opencsv.CSVWriter;
import com.conference.model.Reservation;
import com.conference.service.ReservationService;
import com.conference.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RapportController {

    @FXML private Button btnExportPDF;
    @FXML private Button btnExportCSV;
    @FXML private Label lblDureeMoyenne;
    @FXML private TableView<Reservation> tableRapport;
    @FXML private TableColumn<Reservation, String> colTitre;
    @FXML private TableColumn<Reservation, String> colSalle;
    @FXML private TableColumn<Reservation, String> colDebut;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private TableColumn<Reservation, Integer> colParticipants;

    private final ReservationService service = new ReservationService();
    private List<Reservation> reservations;

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        btnExportPDF.setOnAction(e -> exportPDF());
        btnExportCSV.setOnAction(e -> exportCSV());
    }

    private void setupTable() {
        colTitre.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getTitre()));
        colSalle.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getSalle() != null ? d.getValue().getSalle().getNom() : ""));
        colDebut.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getDateDebutFormatted()));
        colStatut.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getStatut()));
        colParticipants.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("nombreParticipants"));
    }

    private void loadData() {
        reservations = service.getToutesReservations();
        tableRapport.getItems().setAll(reservations);
        double avg = service.getAverageDuration();
        lblDureeMoyenne.setText(String.format("Durée moyenne : %.0f min (%.1f h)",
                avg, avg / 60));
    }

    private void exportPDF() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en PDF");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fc.setInitialFileName("rapport_reservations.pdf");
        File file = fc.showSaveDialog(btnExportPDF.getScene().getWindow());
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            doc.add(new Paragraph("Rapport des Réservations", titleFont));
            doc.add(new Paragraph("Généré le : " +
                    java.time.LocalDate.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")), cellFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 2f, 2f, 1.5f});

            String[] headers = {"Titre", "Salle", "Début", "Statut", "Participants"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(33, 150, 243));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Reservation r : reservations) {
                table.addCell(new PdfPCell(new Phrase(r.getTitre(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(
                        r.getSalle() != null ? r.getSalle().getNom() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(
                        r.getDateDebutFormatted(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(r.getStatut(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(
                        String.valueOf(r.getNombreParticipants()), cellFont)));
            }

            doc.add(table);
            doc.close();
            AlertUtil.showInfo("Export réussi",
                    "Le rapport PDF a été exporté : " + file.getAbsolutePath());
        } catch (Exception e) {
            AlertUtil.showError("Erreur export PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fc.setInitialFileName("rapport_reservations.csv");
        File file = fc.showSaveDialog(btnExportCSV.getScene().getWindow());
        if (file == null) return;

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeNext(new String[]{
                    "ID", "Titre", "Salle", "Utilisateur",
                    "Début", "Fin", "Participants", "Statut", "Disposition"});

            for (Reservation r : reservations) {
                writer.writeNext(new String[]{
                        String.valueOf(r.getId()),
                        r.getTitre(),
                        r.getSalle() != null ? r.getSalle().getNom() : "",
                        r.getUtilisateur() != null ? r.getUtilisateur().getNomComplet() : "",
                        r.getDateDebutFormatted(),
                        r.getDateFinFormatted(),
                        String.valueOf(r.getNombreParticipants()),
                        r.getStatut(),
                        r.getDisposition()
                });
            }
            AlertUtil.showInfo("Export réussi",
                    "Le rapport CSV a été exporté : " + file.getAbsolutePath());
        } catch (IOException e) {
            AlertUtil.showError("Erreur export CSV", e.getMessage());
            e.printStackTrace();
        }
    }
}
