package com.umar.apps.infra.dao.api.core;

import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractTxExecutor {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractTxExecutor.class.getName());

    private final EntityManagerFactory entityManagerFactory;

    public AbstractTxExecutor(EntityManagerFactory emf) {
        entityManagerFactory = emf;
    }

    public EntityManagerFactory getEMF() {
        return entityManagerFactory;
    }

    public interface JPATransactionVoidFunction extends Consumer<EntityManager> {
        /**
         * Before transaction completion function
         */
        default void beforeTx() {
            LOG.info(JPATransactionVoidFunction.class.getName() + ".beforeTx()...");
        }

        /**
         * After transaction completion function
         */
        default void afterTx() {
            LOG.info(JPATransactionVoidFunction.class.getName() + ".afterTx()...");
        }
    }

    public interface JPATxFunction<T> extends Function<EntityManager, T> {
        default void beforeTx() {
            LOG.info(JPATxFunction.class.getName() + ".beforeTx()...");
        }
        default void afterTx() {
            LOG.info(JPATxFunction.class.getName() + ".afterTx()...");
        }
    }

    public static <T> T doInJPA(Supplier<EntityManagerFactory> factorySupplier, JPATxFunction<T> function, Map properties) {
        T result;
        EntityManager em = null;
        EntityTransaction txn = null;
        try{
            em = properties == null ? factorySupplier.get().createEntityManager() : factorySupplier.get().createEntityManager(properties);
            function.beforeTx();
            txn = em.getTransaction();
            txn.begin();
            LOG.info("Transaction began...");
            result = function.apply(em);
            if ( !txn.getRollbackOnly() ) {
                txn.commit();
            }
        }catch (Throwable t) {
            if(null != txn && txn.isActive()) {
                try {
                    txn.rollback();
                }catch (Exception e) {
                    LOG.error( "Rollback failure", t );
                }
            }
            throw t;
        } finally {
            function.afterTx();
            if(null != em) {
                em.close();
            }
        }
        return result;
    }

    public static void doInJPA(
            Supplier<EntityManagerFactory> factorySupplier,
            JPATransactionVoidFunction function,
            Map properties) {
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try {
            entityManager = properties == null ?
                    factorySupplier.get().createEntityManager():
                    factorySupplier.get().createEntityManager(properties);
            function.beforeTx();
            txn = entityManager.getTransaction();
            txn.begin();
            function.accept( entityManager );
            if ( !txn.getRollbackOnly() ) {
                txn.commit();
                LOG.debug("Transaction committed successfully.");
            }
            else {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOG.error( "Rollback failure", e );
                }
            }
        }
        catch ( Throwable t ) {
            if ( txn != null && txn.isActive() ) {
                try {
                    txn.rollback();
                }
                catch (Exception e) {
                    LOG.error( "Rollback failure", e );
                }
            }
            throw t;
        }
        finally {
            function.afterTx();
            if ( entityManager != null ) {
                entityManager.close();
            }
        }
    }
}
