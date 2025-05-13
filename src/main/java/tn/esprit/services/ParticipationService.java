package tn.esprit.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.models.Participation;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class ParticipationService {

    private Connection connection;

    public ParticipationService() {
        this.connection = MyDataBase.getInstance().getCnx();
    }

    public boolean add(Participation participation) {
        String query = "INSERT INTO participation (id_evenement, id_user, Statut, nombreticket, datepay) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, participation.getIdEvenement());
            ps.setInt(2, participation.getId_user());
            ps.setString(3, participation.getStatut());
            ps.setInt(4, participation.getNombreticket());
            ps.setObject(5, participation.getDatepay() != null ? java.sql.Timestamp.valueOf(participation.getDatepay()) : null);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    participation.setIdParticipation(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une participation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean participationExists(int id_user, int idEvenement) {
        String query = "SELECT COUNT(*) FROM participation WHERE id_user = ? AND id_evenement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id_user);
            ps.setInt(2, idEvenement);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification d'une participation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatut(int id_user, int idEvenement, String nouveauStatut) {
        String query = "UPDATE participation SET Statut = ? WHERE id_user = ? AND id_evenement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, id_user);
            ps.setInt(3, idEvenement);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut d'une participation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateNombreticket(int id_user, int idEvenement, int nouveauNombreticket) {
        String query = "UPDATE participation SET nombreticket = ? WHERE id_user = ? AND id_evenement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, nouveauNombreticket);
            ps.setInt(2, id_user);
            ps.setInt(3, idEvenement);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du nombreticket d'une participation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDatepay(int id_user, int idEvenement, LocalDateTime nouveauDatepay) {
        String query = "UPDATE participation SET datepay = ? WHERE id_user = ? AND id_evenement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setObject(1, nouveauDatepay != null ? java.sql.Timestamp.valueOf(nouveauDatepay) : null);
            ps.setInt(2, id_user);
            ps.setInt(3, idEvenement);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la date de paiement d'une participation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public ObservableList<Participation> getParticipationsByUser(int id_user) {
        ObservableList<Participation> participations = FXCollections.observableArrayList();
        String query = "SELECT * FROM participation WHERE id_user = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id_user);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participation participation = new Participation();
                participation.setIdParticipation(rs.getInt("id_participation"));
                participation.setIdEvenement(rs.getInt("id_evenement"));
                participation.setId_user(rs.getInt("id_user"));
                participation.setStatut(rs.getString("Statut"));
                participation.setNombreticket(rs.getInt("nombreticket"));
                participation.setDatepay(rs.getTimestamp("datepay") != null ? rs.getTimestamp("datepay").toLocalDateTime() : null);
                participations.add(participation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des participations d'un utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return participations;
    }

    public ObservableList<Participation> getParticipationsByEvent(int idEvenement) {
        ObservableList<Participation> participations = FXCollections.observableArrayList();
        String query = "SELECT * FROM participation WHERE id_evenement = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participation participation = new Participation();
                participation.setIdParticipation(rs.getInt("id_participation"));
                participation.setIdEvenement(rs.getInt("id_evenement"));
                participation.setId_user(rs.getInt("id_user"));
                participation.setStatut(rs.getString("Statut"));
                participation.setNombreticket(rs.getInt("nombreticket"));
                participation.setDatepay(rs.getTimestamp("datepay") != null ? rs.getTimestamp("datepay").toLocalDateTime() : null);
                participations.add(participation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des participations à un événement: " + e.getMessage());
            e.printStackTrace();
        }
        return participations;
    }

    public boolean delete(int idParticipation) {
        String query = "DELETE FROM participation WHERE id_participation = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idParticipation);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression d'une participation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public ObservableList<Participation> getApprovedParticipationsByEvent(int eventId) {
        ObservableList<Participation> participations = FXCollections.observableArrayList();
        String query = "SELECT * FROM participation WHERE id_evenement = ? AND Statut = 'Approuvé'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Participation participation = new Participation();
                participation.setIdParticipation(rs.getInt("id_participation"));
                participation.setIdEvenement(rs.getInt("id_evenement"));
                participation.setId_user(rs.getInt("id_user"));
                participation.setStatut(rs.getString("Statut"));
                participation.setNombreticket(rs.getInt("nombreticket"));
                participation.setDatepay(rs.getTimestamp("datepay") != null ? rs.getTimestamp("datepay").toLocalDateTime() : null);
                participations.add(participation);
                System.out.println("Found approved participation: ID=" + participation.getIdParticipation() + ", User ID=" + participation.getId_user());
            }
            System.out.println("Total approved participations found: " + participations.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des participations approuvées pour event ID " + eventId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return participations;
    }
}