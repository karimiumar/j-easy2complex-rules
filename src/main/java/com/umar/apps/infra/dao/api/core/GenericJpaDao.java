package com.umar.apps.infra.dao.api.core;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.infra.dao.api.WorkflowItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.doInJPA;

/**
 * An implementation of {@link GenericDao} of JPA
 *
 * @param <MODEL>
 * @param <ID>
 */
public abstract class GenericJpaDao<MODEL extends WorkflowItem<ID>, ID 
        extends Serializable> implements GenericDao<MODEL, ID> {

    private static final Logger log = LogManager.getLogger(GenericJpaDao.class);

    private final Class<MODEL> persistentClass;

    protected EntityManagerFactory emf;

    public GenericJpaDao(final Class<MODEL> persistentClass, final EntityManagerFactory emf) {
        this.persistentClass = persistentClass;
        this.emf = emf;
    }

    @Override
    public Optional<MODEL> findById(ID id) {
        log.debug("findById {} ", id);
        return doInJPA(() -> emf, entityManager -> {
             return Optional.of(entityManager.find(persistentClass, id));
        }, null);
    }

    @Override
    public Optional<MODEL> findById(ID id, String queryHint, String graphName) {
        Objects.requireNonNull(queryHint, "queryHint is required");
        Objects.requireNonNull(graphName, "graphName is required");
        log.debug("findById {}, queryHint {}, graphName ", queryHint, graphName);
        return doInJPA(() -> emf, entityManager -> {
            return Optional.of(entityManager.find(persistentClass, id, Collections.singletonMap(
                queryHint, entityManager.getEntityGraph(graphName)
            )));
        }, null);
    }

    @Override
    public MODEL save(MODEL model) {
        log.debug("Persisting {} ", model);
        doInJPA(()-> emf, entityManager -> {
            entityManager.persist(model);
            entityManager.flush();
        },null);
        log.debug("Persisted {} ", model);
        return model;
    }

    @Override
    public MODEL merge(MODEL model) {
        log.debug("Merging {} ", model.getClass());
        doInJPA(() -> emf, entityManager -> {
            entityManager.merge(model);
            entityManager.flush();
        }, null);
        return model;
    }

    @Override
    public void makeTransient(MODEL entity) {
        log.debug("Making Transient {} ", entity.getClass());
        doInJPA(() -> emf, entityManager -> {
            entityManager.detach(entity);
        }, null);
    }

    @Override
    public void delete(MODEL entity) {
        log.debug("Deleting entity {} ", entity.getClass());
        doInJPA(() -> emf, entityManager -> {
            var session = entityManager.unwrap(Session.class);
            var dbEntity = session.find(persistentClass, entity.getId());
            session.delete(dbEntity);
            session.flush();
        }, null);
    }

    @Override
    public void closeEntityManagerFactory() {
        log.debug("Closing Entity Manager Factory of PersistenceUnit");
        if(null != emf && emf.isOpen()) {
            emf.close();
        }
    }

    @Override
    public EntityManagerFactory getEMF() {
        return emf;
    }
}
