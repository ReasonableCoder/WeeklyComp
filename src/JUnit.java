import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JUnit {
    @Test
    public void test() {
        Record record1 = new Record(Event.CLOCK, RecordType.SINGLE, 1000, "Reasy");
        Record record2 = new Record(Event.CLOCK, RecordType.SINGLE, 2000, "Reasy");
        assertTrue(record1.compareTo(record2) > 0);
    }

    @Test
    public void test2() {

    }
}
