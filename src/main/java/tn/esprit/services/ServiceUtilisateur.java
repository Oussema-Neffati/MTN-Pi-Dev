package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements IService<Utilisateur> {
    private Connection cnx  ;

    public ServiceUtilisateur(){
        cnx = MyDataBase.getInstance().getCnx();
    }

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

        String qry ="SELECT * FROM `utilisateur`";
        try {
            Statement stm = cnx.createStatement();
         ResultSet rs = stm.executeQuery(qry);

         while(rs.next()){
             Utilisateur u = new Utilisateur();
             u.setId(rs.getInt("id"));
             u.setNom(rs.getString("nom"));
             u.setPrenom(rs.getString("prenom"));
             u.setEmail(rs.getString("email"));
             u.setMotDePasse(rs.getString("mot_de_passe"));
             u.setRole(Role.valueOf(rs.getString("role")));
             u.setActif(rs.getBoolean("actif"));

             utilisateurs.add(u);         }


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
}
