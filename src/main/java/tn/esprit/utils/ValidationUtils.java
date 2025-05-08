package tn.esprit.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    // Regex pour valider un email
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    // Regex pour valider un numéro de téléphone tunisien (8 chiffres commençant par 2, 5, 9, ou 4)
    private static final String PHONE_REGEX = "^[2459]\\d{7}$";

    // Regex pour valider un CIN tunisien (8 chiffres)
    private static final String CIN_REGEX = "^\\d{8}$";

    // Méthode pour valider un email
    public static boolean isValidEmail(String email) {
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    // Méthode pour valider un numéro de téléphone
    public static boolean isValidPhone(String phone) {
        return Pattern.compile(PHONE_REGEX).matcher(phone).matches();
    }

    // Méthode pour valider un CIN
    public static boolean isValidCIN(String cin) {
        return Pattern.compile(CIN_REGEX).matcher(cin).matches();
    }

    // Méthode pour valider un mot de passe (au moins 8 caractères, une majuscule, une minuscule et un chiffre)
    public static boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[a-z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find();
    }

    // Méthode pour vérifier si une chaîne est vide ou null
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
