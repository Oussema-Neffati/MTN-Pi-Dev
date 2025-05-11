package tn.esprit.utils;

import java.util.prefs.Preferences;

public class PreferencesUtils {
    private static final String PREFS_NODE = "tn/esprit/mte";
    private static final String EMAIL_KEY = "saved_email";
    private static final String PASSWORD_KEY = "saved_password";
    private static final String REMEMBER_ME_KEY = "remember_me";

    private static Preferences getPreferences() {
        return Preferences.userRoot().node(PREFS_NODE);
    }

    // Sauvegarder les identifiants
    public static void saveCredentials(String email, String password, boolean rememberMe) {
        Preferences prefs = getPreferences();

        if (rememberMe) {
            prefs.put(EMAIL_KEY, email);
            prefs.put(PASSWORD_KEY, password);
            prefs.putBoolean(REMEMBER_ME_KEY, true);
        } else {
            // Si "Se souvenir de moi" n'est pas coché, supprimer les données sauvegardées
            clearSavedCredentials();
        }
    }

    // Récupérer l'email sauvegardé
    public static String getSavedEmail() {
        Preferences prefs = getPreferences();
        return prefs.get(EMAIL_KEY, "");
    }

    // Récupérer le mot de passe sauvegardé
    public static String getSavedPassword() {
        Preferences prefs = getPreferences();
        return prefs.get(PASSWORD_KEY, "");
    }

    // Vérifier si "Se souvenir de moi" était coché
    public static boolean isRememberMeChecked() {
        Preferences prefs = getPreferences();
        return prefs.getBoolean(REMEMBER_ME_KEY, false);
    }

    // Supprimer les identifiants sauvegardés
    public static void clearSavedCredentials() {
        Preferences prefs = getPreferences();
        prefs.remove(EMAIL_KEY);
        prefs.remove(PASSWORD_KEY);
        prefs.putBoolean(REMEMBER_ME_KEY, false);
    }
}