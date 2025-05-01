package tn.esprit.test;

import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.MyDataBase;

public class Main {
    public static void main(String[] args) {
        ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

        Utilisateur admin = new Utilisateur("neffati", "oussema", "admin@mairie.tn", "admin123", Role.ADMIN, true);
        serviceUtilisateur.add(admin);

    }
}
