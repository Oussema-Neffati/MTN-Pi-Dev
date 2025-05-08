package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Citoyen;
import tn.esprit.models.Employe;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ServiceUtilisateur implements IService<Utilisateur> {
    private Connection cnx;
    public Connection getCnx() {
        return cnx;
    }

    public ServiceUtilisateur() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Opérations CRUD de base
     */
    @Override
    public void add(Utilisateur utilisateur) {
        String qry = "INSERT INTO `utilisateur`(`nom`, `prenom`, `email`, `mot_de_passe`, `role`, `actif`) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, utilisateur.getNom());
            pstm.setString(2, utilisateur.getPrenom());
            pstm.setString(3, utilisateur.getEmail());
            pstm.setString(4, utilisateur.getMotDePasse());
            pstm.setString(5, utilisateur.getRole().name());
            pstm.setBoolean(6, utilisateur.isActif());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Utilisateur> getAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();

        String qry = "SELECT * FROM `utilisateur`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setMotDePasse(rs.getString("mot_de_passe"));
                u.setRole(Role.valueOf(rs.getString("role")));
                u.setActif(rs.getBoolean("actif"));

                utilisateurs.add(u);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return utilisateurs;
    }

    @Override
    public void update(Utilisateur utilisateur) {
        String qry = "UPDATE `utilisateur` SET `nom`=?, `prenom`=?, `email`=?, `mot_de_passe`=?, `role`=?, `actif`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, utilisateur.getNom());
            pstm.setString(2, utilisateur.getPrenom());
            pstm.setString(3, utilisateur.getEmail());
            pstm.setString(4, utilisateur.getMotDePasse());
            pstm.setString(5, utilisateur.getRole().name());
            pstm.setBoolean(6, utilisateur.isActif());
            pstm.setInt(7, utilisateur.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Utilisateur utilisateur) {
        String qry = "DELETE FROM `utilisateur` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, utilisateur.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Dans ServiceUtilisateur.java
    public Utilisateur findByEmail(String email) {
        String qry = "SELECT * FROM `utilisateur` WHERE `email` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setMotDePasse(rs.getString("mot_de_passe"));
                u.setRole(Role.valueOf(rs.getString("role")));
                u.setActif(rs.getBoolean("actif"));
                return u;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Méthode pour vérifier les identifiants
    public Utilisateur authenticate(String email, String password) {
        Utilisateur user = findByEmail(email);
        if (user != null && user.getMotDePasse().equals(password) && user.isActif()) {
            return user;
        }
        return null;
    }

    // Méthode pour changer le statut actif d'un utilisateur
    public boolean toggleActive(int userId) {
        String qry = "UPDATE `utilisateur` SET `actif` = NOT `actif` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            int result = pstm.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Dans ServiceUtilisateur.java

    // Méthode pour ajouter un citoyen
    public void addCitoyen(Citoyen citoyen) {
        // D'abord ajouter l'utilisateur de base
        add(citoyen);

        // Récupérer l'ID généré
        Utilisateur user = findByEmail(citoyen.getEmail());
        if (user != null) {
            int userId = user.getId();

            // Insérer les données spécifiques au citoyen
            String qry = "INSERT INTO `citoyen`(`id_user`, `cin`, `adresse`, `telephone`) VALUES (?,?,?,?)";
            try {
                PreparedStatement pstm = cnx.prepareStatement(qry);
                pstm.setInt(1, userId);
                pstm.setString(2, citoyen.getCin());
                pstm.setString(3, citoyen.getAdresse());
                pstm.setString(4, citoyen.getTelephone());

                pstm.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Méthode pour ajouter un employé
    public void addEmploye(Employe employe) {
        // D'abord ajouter l'utilisateur de base
        add(employe);

        // Récupérer l'ID généré
        Utilisateur user = findByEmail(employe.getEmail());
        if (user != null) {
            int userId = user.getId();

            // Insérer les données spécifiques à l'employé
            String qry = "INSERT INTO `employe`(`id_user`, `poste`, `date_embauche`, `departement`) VALUES (?,?,?,?)";
            try {
                PreparedStatement pstm = cnx.prepareStatement(qry);
                pstm.setInt(1, userId);
                pstm.setString(2, employe.getPoste());
                pstm.setDate(3, java.sql.Date.valueOf(employe.getDateEmbauche()));
                pstm.setString(4, employe.getDepartement());

                pstm.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Méthode pour récupérer un citoyen par ID
    public Citoyen getCitoyenById(int id) {
        Utilisateur user = null;
        Citoyen citoyen = null;

        // D'abord récupérer l'utilisateur de base
        String qry1 = "SELECT * FROM `utilisateur` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry1);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("mot_de_passe"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setActif(rs.getBoolean("actif"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Si l'utilisateur existe et est un citoyen, récupérer les données spécifiques
        if (user != null && user.getRole() == Role.CITOYEN) {
            String qry2 = "SELECT * FROM `citoyen` WHERE `id_user` = ?";
            try {
                PreparedStatement pstm = cnx.prepareStatement(qry2);
                pstm.setInt(1, id);
                ResultSet rs = pstm.executeQuery();

                if (rs.next()) {
                    citoyen = new Citoyen();
                    citoyen.setId(user.getId());
                    citoyen.setNom(user.getNom());
                    citoyen.setPrenom(user.getPrenom());
                    citoyen.setEmail(user.getEmail());
                    citoyen.setMotDePasse(user.getMotDePasse());
                    citoyen.setRole(user.getRole());
                    citoyen.setActif(user.isActif());

                    citoyen.setCin(rs.getString("cin"));
                    citoyen.setAdresse(rs.getString("adresse"));
                    citoyen.setTelephone(rs.getString("telephone"));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return citoyen;
    }

    // Méthode pour récupérer un employé par ID
    public Employe getEmployeById(int id) {
        Utilisateur user = null;
        Employe employe = null;

        // D'abord récupérer l'utilisateur de base
        String qry1 = "SELECT * FROM `utilisateur` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry1);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("mot_de_passe"));
                user.setRole(Role.valueOf(rs.getString("role")));
                user.setActif(rs.getBoolean("actif"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Si l'utilisateur existe et est un employé, récupérer les données spécifiques
        if (user != null && user.getRole() == Role.EMPLOYE) {
            String qry2 = "SELECT * FROM `employe` WHERE `id_user` = ?";
            try {
                PreparedStatement pstm = cnx.prepareStatement(qry2);
                pstm.setInt(1, id);
                ResultSet rs = pstm.executeQuery();

                if (rs.next()) {
                    employe = new Employe();
                    employe.setId(user.getId());
                    employe.setNom(user.getNom());
                    employe.setPrenom(user.getPrenom());
                    employe.setEmail(user.getEmail());
                    employe.setMotDePasse(user.getMotDePasse());
                    employe.setRole(user.getRole());
                    employe.setActif(user.isActif());

                    employe.setPoste(rs.getString("poste"));
                    employe.setDateEmbauche(rs.getDate("date_embauche").toLocalDate());
                    employe.setDepartement(rs.getString("departement"));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        return employe;
    }

    // Méthode pour récupérer tous les citoyens
    public List<Citoyen> getAllCitoyens() {
        List<Citoyen> citoyens = new ArrayList<>();

        // Récupérer tous les utilisateurs qui sont des citoyens
        String qry = "SELECT u.*, c.cin, c.adresse, c.telephone FROM `utilisateur` u " +
                "JOIN `citoyen` c ON u.id = c.id_user " +
                "WHERE u.role = 'CITOYEN'";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Citoyen c = new Citoyen();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                c.setEmail(rs.getString("email"));
                c.setMotDePasse(rs.getString("mot_de_passe"));
                c.setRole(Role.CITOYEN);
                c.setActif(rs.getBoolean("actif"));

                c.setCin(rs.getString("cin"));
                c.setAdresse(rs.getString("adresse"));
                c.setTelephone(rs.getString("telephone"));

                citoyens.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return citoyens;
    }

    // Méthode pour récupérer tous les employés
    public List<Employe> getAllEmployes() {
        List<Employe> employes = new ArrayList<>();

        // Récupérer tous les utilisateurs qui sont des employés
        String qry = "SELECT u.*, e.poste, e.date_embauche, e.departement FROM `utilisateur` u " +
                "JOIN `employe` e ON u.id = e.id_user " +
                "WHERE u.role = 'EMPLOYE'";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Employe e = new Employe();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setPrenom(rs.getString("prenom"));
                e.setEmail(rs.getString("email"));
                e.setMotDePasse(rs.getString("mot_de_passe"));
                e.setRole(Role.EMPLOYE);
                e.setActif(rs.getBoolean("actif"));

                e.setPoste(rs.getString("poste"));
                e.setDateEmbauche(rs.getDate("date_embauche").toLocalDate());
                e.setDepartement(rs.getString("departement"));

                employes.add(e);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return employes;
    }
}