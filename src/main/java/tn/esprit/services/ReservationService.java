package tn.esprit.services;

import tn.esprit.controllers.ReservationController.Reservation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private Connection connection;

    public ReservationService() {
        connection = MyDataBase.getInstance().getCnx();
    }

    public void add(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservation (date_reservation, heure_debut, heure_fin, status, nombre_participants, motif, cin) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDate(1, Date.valueOf(reservation.getDateReservation()));
            pst.setTime(2, Time.valueOf(reservation.getHeureDebut()));
            pst.setTime(3, Time.valueOf(reservation.getHeureFin()));
            pst.setString(4, reservation.getStatus());
            pst.setInt(5, reservation.getNombreParticipants());
            pst.setString(6, reservation.getMotif());
            pst.setString(7, reservation.getCin());
            
            pst.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void update(Reservation reservation) throws SQLException {
        String query = "UPDATE reservation SET date_reservation=?, heure_debut=?, heure_fin=?, status=?, " +
                      "nombre_participants=?, motif=?, cin=? WHERE id=?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(reservation.getDateReservation()));
            pst.setTime(2, Time.valueOf(reservation.getHeureDebut()));
            pst.setTime(3, Time.valueOf(reservation.getHeureFin()));
            pst.setString(4, reservation.getStatus());
            pst.setInt(5, reservation.getNombreParticipants());
            pst.setString(6, reservation.getMotif());
            pst.setString(7, reservation.getCin());
            pst.setInt(8, reservation.getId());
            
            pst.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM reservation WHERE id=?";
        
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
                Reservation reservation = new Reservation(
                    rs.getInt("id"),
                    rs.getDate("date_reservation").toLocalDate(),
                    rs.getTime("heure_debut").toLocalTime(),
                    rs.getTime("heure_fin").toLocalTime(),
                    rs.getString("status"),
                    rs.getInt("nombre_participants"),
                    rs.getString("motif"),
                    rs.getString("cin")
                );
                reservations.add(reservation);
            }
        }
        
        return reservations;
    }

    public boolean hasTimeConflict(LocalDate date, LocalTime startTime, LocalTime endTime, Integer excludeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation WHERE date_reservation = ? " +
                      "AND ((heure_debut <= ? AND heure_fin > ?) OR (heure_debut < ? AND heure_fin >= ?))";
        if (excludeId != null) {
            query += " AND id != ?";
        }
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(date));
            pst.setTime(2, Time.valueOf(endTime));
            pst.setTime(3, Time.valueOf(startTime));
            pst.setTime(4, Time.valueOf(endTime));
            pst.setTime(5, Time.valueOf(startTime));
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