package tn.esprit.utils;

import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private Utilisateur currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == Role.ADMIN;
    }

    public boolean isEmploye() {
        return isLoggedIn() && currentUser.getRole() == Role.EMPLOYE;
    }

    public boolean isCitoyen() {
        return isLoggedIn() && currentUser.getRole() == Role.CITOYEN;
    }
}