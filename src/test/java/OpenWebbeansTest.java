import org.apache.openwebbeans.junit5.Cdi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Cdi(disableDiscovery = true, classes = MyBean.class)
public class OpenWebbeansTest {

    @Inject MyBean myBean;
    /*private static SeContainer container;

    @BeforeAll
    static void start() {
        // simulate another way than @Cdi to bootstrap the container,
        // can be another server (meecrowave, tomee, playx, ...) or just a custom preconfigured setup
        container = SeContainerInitializer.newInstance()
                .disableDiscovery()
                .addBeanClasses(MyBean.class)
                .initialize();
    }

    @AfterAll
    static void stop() {
        container.close();
    }*/

    @Test
    public void greetingsTest(){
        String greetings = myBean.greetings("Umar");
        assertEquals("Greetings, Umar the OpenWebBeans started successfully.", greetings);
    }
}

@ApplicationScoped
class MyBean{

    public String greetings(String name) {
        return String.format("Greetings, %s the OpenWebBeans started successfully.", name);
    }
}
