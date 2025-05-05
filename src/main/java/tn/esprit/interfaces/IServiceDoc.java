package tn.esprit.interfaces;

import javafx.collections.ObservableList;

import java.sql.SQLException;

public interface IServiceDoc<T>{
    void ajouter(T t) throws SQLException;
    void modifier(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    ObservableList<T> afficher() throws SQLException; //T t

}