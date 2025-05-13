package tn.esprit.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Citoyen;
import tn.esprit.models.Evenement;
import tn.esprit.models.Participation;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.Evenementservice;
import tn.esprit.services.ParticipationService;
import tn.esprit.services.paiementStripe;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class inter_event_details {

    @FXML
    private Label eventNameLabel;

    @FXML
    private Label eventDateLabel;

    @FXML
    private Label eventLieuLabel;

    @FXML
    private Label eventOrganisateurLabel;

    @FXML
    private Label eventPrixLabel;

    @FXML
    private Label eventPlacesLabel;

    @FXML
    private Label eventTotalPlacesLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Button payButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button downloadTicketButton;

    @FXML
    private Label userNameLabel;

    private Evenement evenement;
    private ParticipationService participationService;
    private Evenementservice evenementService;
    private double totalAmount;
    private String paymentSessionId;
    private boolean paymentCompleted = false;
    private Participation currentParticipation; // Store the participation object

    public inter_event_details() {
        this.participationService = new ParticipationService();
        this.evenementService = new Evenementservice();
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        if (evenement != null) {
            eventNameLabel.setText(evenement.getNom());
            eventDateLabel.setText("Date: " + evenement.getDate());
            eventLieuLabel.setText("Lieu: " + evenement.getLieu());
            eventOrganisateurLabel.setText("Organisateur: " + evenement.getOrganisateur());
            eventPrixLabel.setText("Prix par ticket: " + evenement.getPrix() + " TND");
            eventPlacesLabel.setText("Places disponibles: " + evenement.getNombreplace());
            eventTotalPlacesLabel.setText("Places totales: " + evenement.getTotalPlaces());
            totalAmount = evenement.getPrix();
            totalAmountLabel.setText("Montant total : " + String.format("%.2f", totalAmount) + " TND");
        }
    }

    @FXML
    public void initialize() {
        downloadTicketButton.setVisible(false);
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            userNameLabel.setText("Non connecté");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) userNameLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement: " + e.getMessage());
            }
        }
    }

    @FXML
    void handlePayButton() {
        try {
            if (evenement == null) {
                showError("Erreur", "Aucun événement sélectionné.", "Veuillez sélectionner un événement.");
                return;
            }

            if (evenement.getNombreplace() <= 0) {
                showError("Erreur", "Aucune place disponible.", "Cet événement est complet.");
                return;
            }

            if (totalAmount <= 0) {
                showError("Erreur de montant", "Le montant total est invalide.", "Veuillez vérifier le prix du ticket.");
                return;
            }

            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                showError("Erreur", "Utilisateur non connecté.", "Veuillez vous connecter pour continuer.");
                return;
            }


            paiementStripe stripePaymentService = new paiementStripe();
            paymentSessionId = stripePaymentService.createCheckoutSession(totalAmount);

            WebView webView = new WebView();
            webView.getEngine().load(paymentSessionId);

            VBox webViewContainer = new VBox();
            webViewContainer.getChildren().add(webView);
            Scene scene = new Scene(webViewContainer, 800, 600);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Paiement Stripe");

            webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    String location = newValue.toString();
                    if (location.contains("success")) {
                        stage.close();
                        confirmPayment(currentUser);
                        downloadTicketButton.setVisible(true);
                        paymentCompleted = true;
                        showSuccess("Paiement réussi", "Votre paiement a été effectué avec succès.",
                                "Vous pouvez maintenant télécharger votre ticket en cliquant sur le bouton 'Télécharger Ticket'.");
                    } else if (location.contains("cancel")) {
                        stage.close();
                        handleCancelButton();
                        showError("Annulation", "Le paiement a été annulé.", "Veuillez réessayer.");
                    }
                }
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de paiement", "Une erreur s'est produite lors du paiement.", e.getMessage());
        }
    }

    private void confirmPayment(Utilisateur user) {
        evenement.setNombreplace(evenement.getNombreplace() - 1);
        evenementService.update(evenement);
        eventPlacesLabel.setText("Places disponibles: " + evenement.getNombreplace());

        Participation participation = new Participation();
        participation.setIdEvenement(evenement.getId());
        participation.setId_user(user.getId());
        participation.setStatut("Approuvé");
        participation.setNombreticket(1);
        participation.setDatepay(LocalDateTime.now());
        // Do not set datepay here; let the database default to CURRENT_TIMESTAMP
        boolean added = participationService.add(participation);

        if (added) {
            // Retrieve the participation from the database to get the database-generated datepay
            this.currentParticipation = participationService.getParticipationsByUser(user.getId())
                    .stream()
                    .filter(p -> p.getIdEvenement() == evenement.getId() && "Approuvé".equals(p.getStatut()))
                    .findFirst()
                    .orElse(null);

            if (this.currentParticipation != null) {
                System.out.println("Participation added with datepay: " + this.currentParticipation.getDatepay());
            } else {
                System.err.println("Failed to retrieve the newly added participation.");
            }
        } else {
            System.err.println("Failed to add participation.");
        }

        if (evenement.getNombreplace() <= 0) {
            payButton.setDisable(true);
        }
    }

    @FXML
    void handleCancelButton() {
        paymentSessionId = null;
        showSuccess("Annulation", "La sélection a été annulée.", "Veuillez choisir à nouveau si nécessaire.");
    }

    @FXML
    void downloadTicket() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Erreur", "Utilisateur non connecté.", "Veuillez vous connecter pour continuer.");
            return;
        }
        if (evenement == null) {
            showError("Erreur", "Aucun événement sélectionné.", "Veuillez sélectionner un événement.");
            return;
        }

        if (!paymentCompleted) {
            showError("Erreur", "Paiement non effectué", "Veuillez d'abord effectuer le paiement.");
            return;
        }

        // Retrieve the participation for the current user and event
        Participation participation = participationService.getParticipationsByUser(currentUser.getId())
                .stream()
                .filter(p -> p.getIdEvenement() == evenement.getId() && "Approuvé".equals(p.getStatut()))
                .findFirst()
                .orElse(null);

        if (participation == null) {
            showError("Erreur", "Participation non trouvée", "Aucune participation approuvée trouvée pour cet événement.");
            return;
        }

        System.out.println("Participation retrieved for ticket download with datepay: " + participation.getDatepay());

        // Open file chooser to select save location and filename
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le billet PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("ticket_E" + evenement.getId() + "_U" + currentUser.getId() + ".pdf");

        Stage stage = (Stage) downloadTicketButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // Generate and save the PDF to the chosen location
            generateTicketPDF(currentUser, file.getAbsolutePath(), participation);
            showSuccess("Succès", "Téléchargement terminé", "Le billet a été enregistré à : " + file.getAbsolutePath());
        } else {
            showError("Annulation", "Aucun emplacement sélectionné.", "Le téléchargement du ticket a été annulé.");
        }
    }

    private void generateTicketPDF(Utilisateur user, String pdfFilePath, Participation participation) {
        try {
            String ticketId = "E" + evenement.getId() + "U" + user.getId() + "T" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String datepay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            String qrData = String.format("EventID:%d,UserID:%d,TicketID:%s",
                    evenement.getId(), user.getId(), ticketId);
            String qrFilePath = generateQRCode(qrData, "ticket_qr_" + ticketId);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float titleFontSize = 24;
            float normalFontSize = 12;
            float smallFontSize = 10;
            float leading = 1.5f * normalFontSize;

            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, titleFontSize);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("BILLET D'ÉVÉNEMENT");
            contentStream.endText();

            // Event Name
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
            contentStream.newLineAtOffset(margin, yStart - 40);
            contentStream.showText(evenement.getNom());
            contentStream.endText();

            // Event Details
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 70);
            contentStream.showText("Date: " + evenement.getDate());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 90);
            contentStream.showText("Lieu: " + evenement.getLieu());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 110);
            contentStream.showText("Organisateur: " + evenement.getOrganisateur());
            contentStream.endText();

            // Ticket Information Section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 140);
            contentStream.showText("Informations Billet:");
            contentStream.endText();

            // Calculate number of tickets purchased
            int ticketsPurchased = evenement.getTotalPlaces() - (evenement.getNombreplace() - 1);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 160);
            contentStream.showText("Numéro de tickets: " + ticketsPurchased);
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 180);
            contentStream.showText("Prix: " + evenement.getPrix() + " TND");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 200);
            contentStream.showText("ID Ticket: " + ticketId);
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 220);
            contentStream.showText("Date de paiement: " + (datepay));
            contentStream.endText();

            // Participant Information Section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 250);
            contentStream.showText("Informations Participant:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 270);
            contentStream.showText("Nom: " + user.getPrenom() + " " + user.getNom());
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
            contentStream.newLineAtOffset(margin, yStart - 290);
            contentStream.showText("Email: " + (user.getEmail() != null ? user.getEmail() : "Non spécifié"));
            contentStream.endText();

            // Citoyen-specific fields
            if (user instanceof Citoyen) {
                Citoyen citoyen = (Citoyen) user;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
                contentStream.newLineAtOffset(margin, yStart - 310);
                contentStream.showText("CIN: " + (citoyen.getCin() != null ? citoyen.getCin() : "Non spécifié"));
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
                contentStream.newLineAtOffset(margin, yStart - 330);
                contentStream.showText("Adresse: " + (citoyen.getAdresse() != null ? citoyen.getAdresse() : "Non spécifiée"));
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
                contentStream.newLineAtOffset(margin, yStart - 350);
                contentStream.showText("Téléphone: " + (citoyen.getTelephone() != null ? citoyen.getTelephone() : "Non spécifié"));
                contentStream.endText();
            } else {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
                contentStream.newLineAtOffset(margin, yStart - 310);
                contentStream.showText("CIN: Non applicable");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
                contentStream.newLineAtOffset(margin, yStart - 330);
                contentStream.showText("Adresse: Non spécifiée");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN, normalFontSize);
                contentStream.newLineAtOffset(margin, yStart - 350);
                contentStream.showText("Téléphone: Non spécifié");
                contentStream.endText();
            }

            // QR Code
            PDImageXObject qrCodeImage = PDImageXObject.createFromFile(qrFilePath, document);
            float qrSize = 150f;
            contentStream.drawImage(qrCodeImage, margin, yStart - 500, qrSize, qrSize);

            // Footer Note
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, smallFontSize);
            contentStream.newLineAtOffset(margin, margin);
            contentStream.showText("Ce billet est personnel et doit être présenté à l'entrée de l'événement.");
            contentStream.endText();

            contentStream.close();

            document.save(pdfFilePath);
            document.close();

            showSuccess("Succès", "Ticket généré avec succès",
                    "Le fichier a été enregistré à l'emplacement: " + pdfFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la génération du ticket.", e.getMessage());
        }
    }

    private String generateQRCode(String data, String fileName) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);
        String qrFilePath = System.getProperty("user.dir") + File.separator + fileName + ".png";
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", Path.of(qrFilePath));
        return qrFilePath;
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/inter_event_usr.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Événements");
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de retourner à la liste des événements.", e.getMessage());
        }
    }

    @FXML
    void showProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/UserProfile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mon Profil");
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page de profil.", e.getMessage());
        }
    }

    @FXML
    void logout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger l'écran de connexion.", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}