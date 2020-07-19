import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleTest {
    @Test
    public void test() {
        assertNotEquals(new Name("Umar Ali"), new Name("Ali Umar"), "Both are different names");
    }
}

record Name(String name){}