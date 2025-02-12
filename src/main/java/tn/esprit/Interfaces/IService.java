package tn.esprit.Interfaces;

import java.util.List;

public interface IService<T> {

    void add(T t);

    List<T> getAll();

    void update(T t);

    void delete(T t);

    List<T> getByRole(String role);

    List<T> getByEmail(String email);

    boolean emailExists(String email);

    public T getOne(int id) ;


}
