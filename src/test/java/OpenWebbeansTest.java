import com.umar.simply.jdbc.dml.operations.InsertOp;
import com.umar.simply.jdbc.dml.operations.SelectOp;
import com.umar.simply.jdbc.dml.operations.api.SqlFunctions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OpenWebbeansTest {

    private SeContainer container;

    @BeforeAll
    public void start() {
        // simulate another way than @Cdi to bootstrap the container,
        // can be another server (meecrowave, tomee, playx, ...) or just a custom preconfigured setup
        container = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addPackages(OpenWebbeansTest.class)
                .addBeanClasses(DefaultItemDao.class, ItemProcessor.class, AnotherItemDao.class, SqlFunctionProducer.class)
                .initialize();
    }

    @AfterAll
    public void stop() {
        container.close();
    }

    @Test
    public void givenContainerWhenItemProcessor$execute_ThenPrintItems() {
        ItemProcessor processor = container.select(ItemProcessor.class).get();
        processor.execute();
    }

}

record Item(int value, int limit) {}

@Qualifier @Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD
        ,ElementType.FIELD
        ,ElementType.PARAMETER
        ,ElementType.TYPE
})
@interface Demo {
}

interface ItemDao {
    List<Item> fetchItems();
}

@RequestScoped
@Named
class ItemProcessor {
    private ItemDao itemDao;
    private SqlFunctionProducer sqlFunctionProducer;

    //Required for CDI.
    protected ItemProcessor(){}

    @Inject
    ItemProcessor(ItemDao itemDao, SqlFunctionProducer sqlFunctionProducer) {
        this.itemDao = itemDao;
        this.sqlFunctionProducer = sqlFunctionProducer;
    }

    public void execute() {
        List<Item> items = itemDao.fetchItems();
        items.forEach(System.out::println);
        sqlFunctionProducer.select();
    }
}

@Demo
class DefaultItemDao implements ItemDao {
    @Override
    public List<Item> fetchItems() {
        return List.of(new Item(34,7), new Item(44,7), new Item(4,17), new Item(4,27));
    }
}

class AnotherItemDao implements ItemDao {

    @Override
    public List<Item> fetchItems() {
        return List.of(new Item(67,8), new Item(34,5));
    }
}

class SqlFunctionProducer {
    @Produces
    public SqlFunctions<SelectOp> select() {
        return SelectOp.create();
    }

    @Produces
    public SqlFunctions<InsertOp> insert() {
        return InsertOp.create();
    }
}