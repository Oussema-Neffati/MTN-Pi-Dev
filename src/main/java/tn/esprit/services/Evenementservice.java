package tn.esprit.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.models.Evenement;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Evenementservice implements IService<Evenement> {
    private final Connection connection;

    public Evenementservice() {
        connection = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void create(Evenement evenement) {
        String req = "INSERT INTO evenement (nom, lieu, date, organisateur, prix, nombreplace, totalPlaces) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, evenement.getNom());
            ps.setString(2, evenement.getLieu());
            ps.setString(3, evenement.getDate());
            ps.setString(4, evenement.getOrganisateur());
            ps.setDouble(5, evenement.getPrix());
            ps.setInt(6, evenement.getNombreplace());
            ps.setInt(7, evenement.getTotalPlaces());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creating event: " + e.getMessage());
        }
    }

    @Override
    public Evenement read(int id) {
        String req = "SELECT * FROM evenement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setNom(rs.getString("nom"));
                evenement.setLieu(rs.getString("lieu"));
                evenement.setDate(rs.getString("date"));
                evenement.setOrganisateur(rs.getString("organisateur"));
                evenement.setPrix(rs.getInt("prix"));
                evenement.setNombreplace(rs.getInt("nombreplace"));
                evenement.setTotalPlaces(rs.getInt("totalPlaces"));
                return evenement;
            }
        } catch (SQLException e) {
            System.out.println("Error reading event: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void update(Evenement evenement) {
        String req = "UPDATE evenement SET nom = ?, lieu = ?, date = ?, organisateur = ?, prix = ?, nombreplace = ?, totalPlaces = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, evenement.getNom());
            ps.setString(2, evenement.getLieu());
            ps.setString(3, evenement.getDate());
            ps.setString(4,evenement.getOrganisateur());
            ps.setDouble(5, evenement.getPrix());
            ps.setInt(6, evenement.getNombreplace());
            ps.setInt(7, evenement.getTotalPlaces());
            ps.setInt(8, evenement.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating event: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM evenement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting event: " + e.getMessage());
        }
    }

    @Override
    public ObservableList<Evenement> readAll() {
        String req = "SELECT * FROM evenement";
        ObservableList<Evenement> evenements = FXCollections.observableArrayList();
        try (PreparedStatement ps = connection.prepareStatement(req); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setNom(rs.getString("nom"));
                evenement.setLieu(rs.getString("lieu"));
                evenement.setDate(rs.getString("date"));
                evenement.setOrganisateur(rs.getString("organisateur"));
                evenement.setPrix(rs.getInt("prix"));
                evenement.setNombreplace(rs.getInt("nombreplace"));
                evenement.setTotalPlaces(rs.getInt("totalPlaces"));
                evenements.add(evenement);
            }
        } catch (SQLException e) {
            System.out.println("Error reading all events: " + e.getMessage());
        }
        return evenements;
    }

    @Override
    public ObservableList<Evenement> searchEvents(String query) {
        String req = "SELECT * FROM evenement WHERE nom LIKE ? OR lieu LIKE ? OR organisateur LIKE ?";
        ObservableList<Evenement> evenements = FXCollections.observableArrayList();
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setNom(rs.getString("nom"));
                evenement.setLieu(rs.getString("lieu"));
                evenement.setDate(rs.getString("date"));
                evenement.setOrganisateur(rs.getString("organisateur"));
                evenement.setPrix(rs.getInt("prix"));
                evenement.setNombreplace(rs.getInt("nombreplace"));
                evenement.setTotalPlaces(rs.getInt("totalPlaces"));
                evenements.add(evenement);
            }
        } catch (SQLException e) {
            System.out.println("Error searching events: " + e.getMessage());
        }
        return evenements;
    }

    public List<Evenement> getTodayEvents() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedToday = today.format(formatter);

        String req = "SELECT * FROM evenement WHERE date = ?";
        List<Evenement> evenements = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, formattedToday);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Evenement evenement = new Evenement();
                evenement.setId(rs.getInt("id"));
                evenement.setNom(rs.getString("nom"));
                evenement.setLieu(rs.getString("lieu"));
                evenement.setDate(rs.getString("date"));
                evenement.setOrganisateur(rs.getString("organisateur"));
                evenement.setPrix(rs.getInt("prix"));
                evenement.setNombreplace(rs.getInt("nombreplace"));
                evenement.setTotalPlaces(rs.getInt("totalPlaces"));
                evenements.add(evenement);
            }
        } catch (SQLException e) {
            System.out.println("Error getting today's events: " + e.getMessage());
        }
        return evenements;
    }
}