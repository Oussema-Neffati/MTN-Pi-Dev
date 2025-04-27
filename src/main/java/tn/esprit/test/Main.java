package tn.esprit.test;

import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.MyDataBase;

public class Main {
    public static void main(String[] args) {
        ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

        // Ajout d'un nouvel utilisateur (admin)
        Utilisateur admin = new Utilisateur("neffati", "oussema", "admin@mairie.tn", "admin123", "ADMIN", true);
        serviceUtilisateur.add(admin);

    }
}
