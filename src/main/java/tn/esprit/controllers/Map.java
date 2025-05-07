package tn.esprit.controllers;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Map {

    private TextField linkedAddressField;
    private Stage mapStage;

    /**
     * Associe le champ d'adresse au contrôleur de carte
     * @param addressField Champ texte où l'adresse sélectionnée sera affichée
     */
    public void setLinkedAddressField(TextField addressField) {
        this.linkedAddressField = addressField;
    }

    /**
     * Ouvre une fenêtre avec OpenStreetMap pour sélectionner une adresse
     */
    public void openMap() {
        mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("Sélectionner une adresse");

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Créer un pont entre JavaScript et Java
        JavaBridge bridge = new JavaBridge();

        // Charger le contenu HTML avec la carte OpenStreetMap interactive
        webEngine.loadContent(getMapHtml());

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            // Une fois la page chargée, exposer l'objet JavaBridge au JavaScript
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaBridge", bridge);
        });

        StackPane root = new StackPane(webView);
        Scene scene = new Scene(root, 800, 600);

        mapStage.setScene(scene);
        mapStage.show();
    }

    /**
     * Exemple de méthode pour rechercher une adresse avec Nominatim directement depuis Java
     * @param query Terme de recherche (ex: "Paris")
     * @return Résultat JSON de la recherche
     */
    public String searchAddress(String query) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "JavaFX Application") // Nominatim exige un User-Agent
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Classe interne pour permettre à JavaScript d'appeler des méthodes Java
     */
    public class JavaBridge {
        /**
         * Méthode appelée par JavaScript pour envoyer l'adresse sélectionnée
         * @param address L'adresse sélectionnée sur la carte
         */
        public void setAddress(String address) {
            System.out.println("JavaBridge.setAddress called with: " + address);
            if (linkedAddressField != null) {
                // Utiliser runLater pour s'assurer que la mise à jour est faite dans le thread de l'UI
                javafx.application.Platform.runLater(() -> {
                    try {
                        linkedAddressField.setText(address);
                        // Fermer la fenêtre de carte correctement (référencer mapStage au lieu de la fenêtre du TextField)
                        if (mapStage != null) {
                            mapStage.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error setting address: " + e.getMessage());
                    }
                });
            } else {
                System.err.println("linkedAddressField is null");
            }
        }
    }

    /**
     * Retourne le HTML pour afficher la carte avec la possibilité de sélectionner une adresse
     */
    private String getMapHtml() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Sélection d'adresse</title>\n" +
                "    <meta http-equiv=\"Content-Security-Policy\" content=\"default-src * 'self' 'unsafe-inline' 'unsafe-eval' data: blob:;\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.css\" />\n" +
                "    <style>\n" +
                "        body, html { height: 100%; margin: 0; padding: 0; }\n" +
                "        #map { height: 85%; width: 100%; }\n" +
                "        #address-panel { padding: 10px; background-color: #f8f8f8; }\n" +
                "        #address { width: 70%; padding: 8px; margin-right: 10px; }\n" +
                "        button { padding: 8px 15px; background-color: #4169E1; color: white; border: none; cursor: pointer; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <div id=\"address-panel\">\n" +
                "        <input type=\"text\" id=\"address\" placeholder=\"Adresse sélectionnée\" readonly />\n" +
                "        <button onclick=\"confirmSelection()\">Confirmer</button>\n" +
                "    </div>\n" +
                "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/leaflet.js\"></script>\n" +
                "    <script>\n" +
                "        let map, marker;\n" +
                "        let selectedAddress = '';\n" +
                "        let lat = 36.8065; // Coordonnées de Tunis\n" +
                "        let lng = 10.1815;\n" +
                "        \n" +
                "        // Attendre que la page soit complètement chargée\n" +
                "        window.onload = function() {\n" +
                "            try {\n" +
                "                // Initialisation de la carte centrée sur la Tunisie\n" +
                "                map = L.map('map').setView([lat, lng], 10);\n" +
                "                \n" +
                "                // Ajouter la couche OpenStreetMap avec l'URL directe\n" +
                "                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "                    attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n" +
                "                }).addTo(map);\n" +
                "                \n" +
                "                // Événement de clic sur la carte\n" +
                "                map.on('click', async function(e) {\n" +
                "                    if (marker) map.removeLayer(marker); // Supprimer l'ancien marqueur\n" +
                "                    \n" +
                "                    // Créer un nouveau marqueur\n" +
                "                    marker = L.marker(e.latlng, {draggable: true}).addTo(map);\n" +
                "                    \n" +
                "                    // Récupérer l'adresse pour ces coordonnées\n" +
                "                    selectedAddress = await reverseGeocode(e.latlng.lat, e.latlng.lng);\n" +
                "                    document.getElementById('address').value = selectedAddress;\n" +
                "                    \n" +
                "                    // Mettre à jour l'adresse si le marqueur est déplacé\n" +
                "                    marker.on('dragend', async function() {\n" +
                "                        const position = marker.getLatLng();\n" +
                "                        selectedAddress = await reverseGeocode(position.lat, position.lng);\n" +
                "                        document.getElementById('address').value = selectedAddress;\n" +
                "                    });\n" +
                "                });\n" +
                "                console.log('Map initialized successfully!');\n" +
                "            } catch (error) {\n" +
                "                console.error('Error initializing map:', error);\n" +
                "            }\n" +
                "        };\n" +
                "        \n" +
                "        // Fonction pour reverse géocoder (coordonnées -> adresse)\n" +
                "        async function reverseGeocode(lat, lng) {\n" +
                "            try {\n" +
                "                const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`, {\n" +
                "                    headers: {\n" +
                "                        'User-Agent': 'JavaFX Application' // Nominatim exige un User-Agent\n" +
                "                    }\n" +
                "                });\n" +
                "                const data = await response.json();\n" +
                "                return data.display_name;\n" +
                "            } catch (error) {\n" +
                "                console.error('Erreur de géocodage:', error);\n" +
                "                return 'Adresse non trouvée';\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        function confirmSelection() {\n" +
                "            if (selectedAddress) {\n" +
                "                try {\n" +
                "                    // Appeler la méthode Java pour transmettre l'adresse sélectionnée\n" +
                "                    if (window.javaBridge) {\n" +
                "                        window.javaBridge.setAddress(selectedAddress);\n" +
                "                    } else {\n" +
                "                        console.error('javaBridge not found');\n" +
                "                        alert('Erreur de communication avec l\\'application. Veuillez réessayer.');\n" +
                "                    }\n" +
                "                } catch (e) {\n" +
                "                    console.error('Error calling javaBridge:', e);\n" +
                "                    alert('Erreur lors de la confirmation de l\\'adresse: ' + e.message);\n" +
                "                }\n" +
                "            } else {\n" +
                "                alert('Veuillez sélectionner une adresse sur la carte');\n" +
                "            }\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}