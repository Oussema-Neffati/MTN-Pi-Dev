package tn.esprit.services;

import tn.esprit.models.Reservation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private Connection connection;

    public ReservationService() {
        connection = MyDataBase.getInstance().getCnx();
    }

    public void add(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservation (dateReservation, heureDebut, heureFin, status, nombreParticipants, motif, cin, idUtilisateur, idRessource) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDate(1, Date.valueOf(reservation.getDateReservation()));
            pst.setString(2, reservation.getHeureDebut());
            pst.setString(3, reservation.getHeureFin());
            pst.setString(4, reservation.getStatus());
            pst.setInt(5, reservation.getNombreParticipants());
            pst.setString(6, reservation.getMotif());
            pst.setString(7, reservation.getCin());
            pst.setInt(8, reservation.getIdUtilisateur());
            pst.setInt(9, reservation.getIdRessource());
            
            pst.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setIdRes(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(Reservation reservation) throws SQLException {
        String query = "UPDATE reservation SET dateReservation=?, heureDebut=?, heureFin=?, status=?, " +
                      "nombreParticipants=?, motif=?, cin=?, idUtilisateur=?, idRessource=? WHERE idRes=?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(reservation.getDateReservation()));
            pst.setString(2, reservation.getHeureDebut());
            pst.setString(3, reservation.getHeureFin());
            pst.setString(4, reservation.getStatus());
            pst.setInt(5, reservation.getNombreParticipants());
            pst.setString(6, reservation.getMotif());
            pst.setString(7, reservation.getCin());
            pst.setInt(8, reservation.getIdUtilisateur());
            pst.setInt(9, reservation.getIdRessource());
            pst.setInt(10, reservation.getIdRes());
            
            pst.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM reservation WHERE idRes=?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public List<Reservation> getAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setIdRes(rs.getInt("idRes"));
                reservation.setDateReservation(rs.getDate("dateReservation").toLocalDate());
                reservation.setHeureDebut(rs.getString("heureDebut"));
                reservation.setHeureFin(rs.getString("heureFin"));
                reservation.setStatus(rs.getString("status"));
                reservation.setNombreParticipants(rs.getInt("nombreParticipants"));
                reservation.setMotif(rs.getString("motif"));
                reservation.setCin(rs.getString("cin"));
                reservation.setIdUtilisateur(rs.getInt("idUtilisateur"));
                reservation.setIdRessource(rs.getInt("idRessource"));
                reservations.add(reservation);
            }
        }
        
        return reservations;
    }

    public boolean hasTimeConflict(LocalDate date, String startTime, String endTime, Integer excludeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation WHERE dateReservation = ? " +
                      "AND ((heureDebut <= ? AND heureFin > ?) OR (heureDebut < ? AND heureFin >= ?))";
        if (excludeId != null) {
            query += " AND idRes != ?";
        }
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(date));
            pst.setString(2, endTime);
            pst.setString(3, startTime);
            pst.setString(4, endTime);
            pst.setString(5, startTime);
            if (excludeId != null) {
                pst.setInt(6, excludeId);
            }
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
} 