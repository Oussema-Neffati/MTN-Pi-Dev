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
        // Mettre à jour la requête avec les bons noms de colonnes
        String qry = "INSERT INTO `utilisateur`(`id_user`, `nom_user`, `prenom_user`, `email`, `motDePasse`, `role`, `actif`, " +
                "`cin`, `adresse`, `telephone`, `poste`, `date_embauche`, `departement`) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, utilisateur.getId()); // id_user
            pstm.setString(2, utilisateur.getNom()); // nom_user
            pstm.setString(3, utilisateur.getPrenom()); // prenom_user
            pstm.setString(4, utilisateur.getEmail());
            pstm.setString(5, utilisateur.getMotDePasse());
            pstm.setString(6, utilisateur.getRole().name());
            pstm.setBoolean(7, utilisateur.isActif());

            // Champs spécifiques selon le rôle
            if (utilisateur.getRole() == Role.CITOYEN) {
                Citoyen citoyen = (Citoyen) utilisateur;
                pstm.setString(8, citoyen.getCin());
                pstm.setString(9, citoyen.getAdresse());
                pstm.setString(10, citoyen.getTelephone());
                pstm.setNull(11, java.sql.Types.VARCHAR);
                pstm.setNull(12, java.sql.Types.DATE);
                pstm.setNull(13, java.sql.Types.VARCHAR);
            } else if (utilisateur.getRole() == Role.EMPLOYE) {
                Employe employe = (Employe) utilisateur;
                pstm.setNull(8, java.sql.Types.VARCHAR);
                pstm.setNull(9, java.sql.Types.VARCHAR);
                pstm.setNull(10, java.sql.Types.VARCHAR);
                pstm.setString(11, employe.getPoste());
                if (employe.getDateEmbauche() != null) {
                    pstm.setDate(12, java.sql.Date.valueOf(employe.getDateEmbauche()));
                } else {
                    pstm.setNull(12, java.sql.Types.DATE);
                }
                pstm.setString(13, employe.getDepartement());
            } else {
                // Pour ADMIN
                pstm.setNull(8, java.sql.Types.VARCHAR);
                pstm.setNull(9, java.sql.Types.VARCHAR);
                pstm.setNull(10, java.sql.Types.VARCHAR);
                pstm.setNull(11, java.sql.Types.VARCHAR);
                pstm.setNull(12, java.sql.Types.DATE);
                pstm.setNull(13, java.sql.Types.VARCHAR);
            }

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
                Utilisateur u;
                Role role = Role.valueOf(rs.getString("role"));

                if (role == Role.CITOYEN) {
                    Citoyen citoyen = new Citoyen();
                    citoyen.setCin(rs.getString("cin"));
                    citoyen.setAdresse(rs.getString("adresse"));
                    citoyen.setTelephone(rs.getString("telephone"));
                    u = citoyen;
                } else if (role == Role.EMPLOYE) {
                    Employe employe = new Employe();
                    employe.setPoste(rs.getString("poste"));
                    Date dateEmbauche = rs.getDate("date_embauche");
                    if (dateEmbauche != null) {
                        employe.setDateEmbauche(dateEmbauche.toLocalDate());
                    }
                    employe.setDepartement(rs.getString("departement"));
                    u = employe;
                } else {
                    u = new Utilisateur();
                }

                // Utiliser les bons noms de colonnes
                u.setId(rs.getInt("id_user"));
                u.setNom(rs.getString("nom_user"));
                u.setPrenom(rs.getString("prenom_user"));
                u.setEmail(rs.getString("email"));
                u.setMotDePasse(rs.getString("motDePasse"));
                u.setRole(role);
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
        // Mettre à jour la requête avec les bons noms de colonnes
        String qry = "UPDATE `utilisateur` SET `nom_user`=?, `prenom_user`=?, `email`=?, `motDePasse`=?, `role`=?, `actif`=?, " +
                "`cin`=?, `adresse`=?, `telephone`=?, `poste`=?, `date_embauche`=?, `departement`=? WHERE `id_user`=?";

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, utilisateur.getNom());
            pstm.setString(2, utilisateur.getPrenom());
            pstm.setString(3, utilisateur.getEmail());
            pstm.setString(4, utilisateur.getMotDePasse());
            pstm.setString(5, utilisateur.getRole().name());
            pstm.setBoolean(6, utilisateur.isActif());

            // Champs spécifiques selon le rôle
            if (utilisateur.getRole() == Role.CITOYEN) {
                Citoyen citoyen = (Citoyen) utilisateur;
                pstm.setString(7, citoyen.getCin());
                pstm.setString(8, citoyen.getAdresse());
                pstm.setString(9, citoyen.getTelephone());
                pstm.setNull(10, java.sql.Types.VARCHAR);
                pstm.setNull(11, java.sql.Types.DATE);
                pstm.setNull(12, java.sql.Types.VARCHAR);
            } else if (utilisateur.getRole() == Role.EMPLOYE) {
                Employe employe = (Employe) utilisateur;
                pstm.setNull(7, java.sql.Types.VARCHAR);
                pstm.setNull(8, java.sql.Types.VARCHAR);
                pstm.setNull(9, java.sql.Types.VARCHAR);
                pstm.setString(10, employe.getPoste());
                if (employe.getDateEmbauche() != null) {
                    pstm.setDate(11, java.sql.Date.valueOf(employe.getDateEmbauche()));
                } else {
                    pstm.setNull(11, java.sql.Types.DATE);
                }
                pstm.setString(12, employe.getDepartement());
            } else {
                // Pour ADMIN
                pstm.setNull(7, java.sql.Types.VARCHAR);
                pstm.setNull(8, java.sql.Types.VARCHAR);
                pstm.setNull(9, java.sql.Types.VARCHAR);
                pstm.setNull(10, java.sql.Types.VARCHAR);
                pstm.setNull(11, java.sql.Types.DATE);
                pstm.setNull(12, java.sql.Types.VARCHAR);
            }

            pstm.setInt(13, utilisateur.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Utilisateur utilisateur) {
        // Mettre à jour la requête avec le bon nom de colonne pour l'id
        String qry = "DELETE FROM `utilisateur` WHERE `id_user`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, utilisateur.getId());

            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Méthode findByEmail modifiée
    public Utilisateur findByEmail(String email) {
        String qry = "SELECT * FROM `utilisateur` WHERE `email` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Utilisateur u;
                Role role = Role.valueOf(rs.getString("role"));

                if (role == Role.CITOYEN) {
                    Citoyen citoyen = new Citoyen();
                    citoyen.setCin(rs.getString("cin"));
                    citoyen.setAdresse(rs.getString("adresse"));
                    citoyen.setTelephone(rs.getString("telephone"));
                    u = citoyen;
                } else if (role == Role.EMPLOYE) {
                    Employe employe = new Employe();
                    employe.setPoste(rs.getString("poste"));
                    Date dateEmbauche = rs.getDate("date_embauche");
                    if (dateEmbauche != null) {
                        employe.setDateEmbauche(dateEmbauche.toLocalDate());
                    }
                    employe.setDepartement(rs.getString("departement"));
                    u = employe;
                } else {
                    u = new Utilisateur();
                }

                // Utiliser les bons noms de colonnes
                u.setId(rs.getInt("id_user"));
                u.setNom(rs.getString("nom_user"));
                u.setPrenom(rs.getString("prenom_user"));
                u.setEmail(rs.getString("email"));
                u.setMotDePasse(rs.getString("motDePasse"));
                u.setRole(role);
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

    // Méthode toggleActive modifiée
    public boolean toggleActive(int userId) {
        String qry = "UPDATE `utilisateur` SET `actif` = NOT `actif` WHERE `id_user` = ?";
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

    // Méthode getCitoyenById modifiée
    public Citoyen getCitoyenById(int id) {
        String qry = "SELECT * FROM `utilisateur` WHERE `id_user` = ? AND `role` = 'CITOYEN'";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Citoyen citoyen = new Citoyen();
                citoyen.setId(rs.getInt("id_user"));
                citoyen.setNom(rs.getString("nom_user"));
                citoyen.setPrenom(rs.getString("prenom_user"));
                citoyen.setEmail(rs.getString("email"));
                citoyen.setMotDePasse(rs.getString("motDePasse"));
                citoyen.setRole(Role.CITOYEN);
                citoyen.setActif(rs.getBoolean("actif"));
                citoyen.setCin(rs.getString("cin"));
                citoyen.setAdresse(rs.getString("adresse"));
                citoyen.setTelephone(rs.getString("telephone"));
                return citoyen;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Méthode getEmployeById modifiée
    public Employe getEmployeById(int id) {
        String qry = "SELECT * FROM `utilisateur` WHERE `id_user` = ? AND `role` = 'EMPLOYE'";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                Employe employe = new Employe();
                employe.setId(rs.getInt("id_user"));
                employe.setNom(rs.getString("nom_user"));
                employe.setPrenom(rs.getString("prenom_user"));
                employe.setEmail(rs.getString("email"));
                employe.setMotDePasse(rs.getString("motDePasse"));
                employe.setRole(Role.EMPLOYE);
                employe.setActif(rs.getBoolean("actif"));
                employe.setPoste(rs.getString("poste"));
                Date dateEmbauche = rs.getDate("date_embauche");
                if (dateEmbauche != null) {
                    employe.setDateEmbauche(dateEmbauche.toLocalDate());
                }
                employe.setDepartement(rs.getString("departement"));
                return employe;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
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