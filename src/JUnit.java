import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JUnit {
    @Test
    public void RecordCompareToTest() {
        // Arrange
        Record record1 = new Record(Event.CLOCK, RecordType.SINGLE, 1000, "Reasy");
        Record record2 = new Record(Event.CLOCK, RecordType.SINGLE, 2000, "Reasy");
        // Act
        boolean result  = record1.compareTo(record2) > 0;
        // Assert
        assertTrue(result, "record1 has a better time than record2, so the value should be greater than 0");
    }

    @Test
    public void getMillisecondsTest() {
        // Arrange
        String testString1 = "12:01.3";
        String testString2 = "45.09812";
        String testString3 = "12/13";
        String testString4 = "0.58";
        // Act
        int ms1 = Main.getMilliseconds(testString1);
        int ms2 = Main.getMilliseconds(testString2);
        int ms3 = Main.getMilliseconds(testString3);
        int ms4 = Main.getMilliseconds(testString4);
        // Assert
        assertEquals(721300, ms1, "12:01.328 should be 721320 ms");
        assertEquals(45090, ms2, "45.09812 should be 45090 ms");
        assertEquals(0, ms3, "Invalid input should return 0");
        assertEquals(580, ms4, "0.58 should be 580 ms");
    }
}
