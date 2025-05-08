package tn.esprit.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class logUtils {
    private static final String LOG_DIRECTORY = "logs";
    private static final String AUTH_LOG_FILE = "auth.log";
    private static final String USER_LOG_FILE = "user_actions.log";

    static {
        // Créer le répertoire de logs s'il n'existe pas
        File directory = new File(LOG_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // Méthode pour journaliser les événements d'authentification
    public static void logAuthEvent(String email, boolean success, String message) {
        String logEntry = String.format("[%s] %s - Login %s: %s",
                getCurrentDateTime(),
                email,
                success ? "SUCCESS" : "FAILURE",
                message);

        writeToLog(AUTH_LOG_FILE, logEntry);
    }

    // Méthode pour journaliser les actions des utilisateurs
    public static void logUserAction(String email, String action, String details) {
        String logEntry = String.format("[%s] %s - %s: %s",
                getCurrentDateTime(),
                email,
                action,
                details);

        writeToLog(USER_LOG_FILE, logEntry);
    }

    // Méthode utilitaire pour écrire dans un fichier de log
    private static void writeToLog(String filename, String logEntry) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_DIRECTORY + File.separator + filename, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier de log: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour obtenir la date et l'heure actuelles formatées
    private static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}