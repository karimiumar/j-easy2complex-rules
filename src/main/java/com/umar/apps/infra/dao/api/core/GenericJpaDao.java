package com.umar.apps.infra.dao.api.core;

import com.umar.apps.infra.dao.api.GenericDao;
import com.umar.apps.infra.dao.api.WorkflowItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import static com.umar.apps.infra.dao.api.core.AbstractTxExecutor.*;

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
    protected final EntityManagerFactory emf;

    public GenericJpaDao(final Class<MODEL> persistentClass, final String persistenceUnit) {
        this.persistentClass = persistentClass;
        this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
    }

    @Override
    public Optional<MODEL> findById(ID id) {
        log.debug("findById {} ", id);
        AtomicReference<MODEL> entity = new AtomicReference<>();
        doInJPA(() -> emf, entityManager -> {
            entity.set(entityManager.find(persistentClass, id));
        }, null);
        return Optional.ofNullable(entity.get());
    }

    @Override
    public MODEL save(MODEL model) {
        log.debug("Persisting {} ", model.getClass());
        doInJPA(()-> emf, entityManager -> {
            entityManager.persist(model);
            entityManager.flush();
        },null);
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
