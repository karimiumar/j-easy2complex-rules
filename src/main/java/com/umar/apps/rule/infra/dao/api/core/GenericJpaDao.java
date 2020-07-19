package com.umar.apps.rule.infra.dao.api.core;

import com.umar.apps.rule.engine.WorkflowItem;
import com.umar.apps.rule.infra.dao.api.GenericDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class GenericJpaDao<MODEL extends WorkflowItem<ID>, ID extends Serializable> implements GenericDao<MODEL,ID> {

    private static final Logger log = LogManager.getLogger(GenericJpaDao.class);
    private final Class<MODEL> persistentClass;
    protected final EntityManagerFactory emf;

    public GenericJpaDao(final Class<MODEL> persistentClass, final String persistenceUnit) {
        this.persistentClass = persistentClass;
        this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
    }

    public void executeInTransaction(Consumer<EntityManager> consumer) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        if(!transaction.isActive()) {
            transaction.begin();
        }
        consumer.accept(em);
        transaction.commit();
        em.close();
    }

    @Override
    public Optional<MODEL> findById(ID id) {
        log.debug("findById {} ", id);
        AtomicReference<MODEL> entity = new AtomicReference<>();
        executeInTransaction(entityManager -> {
            entity.set(entityManager.find(persistentClass, id));
        });
        return Optional.ofNullable(entity.get());
    }

    @Override
    public MODEL save(MODEL model) {
        log.debug("Persisting {} ", model.getClass());
        executeInTransaction(entityManager -> {
            entityManager.persist(model);
            entityManager.flush();
        });
        return model;
    }

    @Override
    public MODEL merge(MODEL model) {
        log.debug("Merging {} ", model.getClass());
        executeInTransaction( entityManager -> {
            entityManager.merge(model);
            entityManager.flush();
        });
        return model;
    }

    @Override
    public void makeTransient(MODEL entity) {
        log.debug("Making Transient {} ", entity.getClass());
        emf.createEntityManager().detach(entity);
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

    public void doInJPA(Consumer<EntityManager> consumer, GenericDao<MODEL, ID> dao){
        EntityManager entityManager = dao.getEMF().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        if(!transaction.isActive()) {
            transaction.begin();
        }
        consumer.accept(entityManager);
        transaction.commit();
        entityManager.close();
    }
}
