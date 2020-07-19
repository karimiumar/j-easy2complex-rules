package com.umar.apps.rule.infra.dao.api;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public interface GenericDao<T, ID extends Serializable> {
    Optional<T> findById(ID id);
    Collection<T> findAll();
    T save(T t);
    T merge(T t);
    void makeTransient(T entity);
    void closeEntityManagerFactory();
    EntityManagerFactory getEMF();
    void doInJPA(Consumer<EntityManager> consumer, GenericDao<T, ID> dao);
}
