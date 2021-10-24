package com.umar.apps.infra.dao.api;

import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * A GenericDao interface for DAO operations
 *
 * @param <T> The transient or persistent type.
 * @param <ID> The Id type to use. The <strong>ID</strong> type must implement {@link Serializable}
 */
public interface GenericDao <T, ID extends Serializable> {
    /**
     * Finds a given JPA entity by ID
     *
     * @param id The id to lookup
     * @return Returns an {@link Optional}
     */
    Optional<T> findById(ID id);

    /**
     * Finds a given JPA entity by ID for the given query hint and graph name.
     *
     * @param id The id to lookup
     * @param queryHint The query hint
     * @param graphName The graph name
     * @return Returns an {@link Optional}
     */
    Optional<T> findById(ID id, String queryHint, String graphName);

    /**
     * Finds All the entities of a given type <strong>T</strong>.
     *
     * @return Returns a {@link Collection}
     */
    Collection<T> findAll();

    /**
     * Makes a non persistent entity persistent.
     *
     * @param t The <strong>T</strong> type to persist.
     *
     * @return Returns the persistent entity.
     */
    T save(T t);

    /**
     * Merges a detached JPA entity with the {@link org.hibernate.Session}
     *
     * @param t The <strong>T</strong> type to persist.
     *
     * @return Returns the persistent entity.
     */
    T merge(T t);

    /**
     * Makes a persistent JPA entity transient.
     *
     * @param entity The JPA entity to make transient.
     */
    void makeTransient(T entity);

    /**
     * Closes the {@link EntityManagerFactory}
     */
    void closeEntityManagerFactory();

    /**
     * Gets the instance of {@link EntityManagerFactory}
     * @return Returns the {@link EntityManagerFactory}
     */
    EntityManagerFactory getEMF();

    /**
     * Deletes a given persistent entity
     *
     * @param entity The entity to delete
     */
    void delete(T entity);

}
