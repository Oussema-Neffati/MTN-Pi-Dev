package tn.esprit.services;

import javafx.collections.ObservableList;

public interface IService<T> {
    void create(T t);
    T read(int id);
    void update(T t);
    void delete(int id);
    ObservableList<T> readAll();
    ObservableList<T> searchEvents(String query);


}