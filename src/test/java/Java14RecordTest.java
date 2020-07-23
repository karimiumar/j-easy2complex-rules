import org.junit.jupiter.api.Test;

import javax.persistence.*;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class Java14RecordTest {
    @Test
    public void test() {
        assertNotEquals(new Name(12L,"Umar Ali"), new Name(12L, "Ali Umar"), "Both are different names");
        NamesDao namesDao = new NamesDao();
        Name umar = new Name(12L,"Mohammad Umar Ali Karimi");
        //record types are final so won't be recognised as valid entities by JPA.
        assertThrows(Exception.class, ()-> namesDao.save(umar));
        assertThrows(Exception.class, namesDao::findAll);
    }
}

@Table(name = "names")
@Entity
record Name(@Id Long id, @Column String name){}

class NamesDao {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test_rulesPU");

    void doInJPA(Consumer<EntityManager> consumer) {
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        if(!transaction.isActive()){
            transaction.begin();
        }
        entityManager.persist(consumer);
        transaction.commit();
        entityManager.close();
    }

    void save(Name name) {
        System.out.println(name);
        doInJPA(entityManager -> entityManager.persist(name));
    }

    void findAll() {
        String sql = "select names from Name names";
        doInJPA(entityManager -> {
            List<?> rows = entityManager.createQuery(sql).getResultList();
            rows.forEach(System.out::println);
        });
    }
}