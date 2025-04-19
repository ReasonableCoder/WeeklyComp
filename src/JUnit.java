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
        String testString1 = " 12:01.3";
        String testString2 = "45.09812";
        String testString3 = "12/13 in 12:24.19";
        String testString4 = "0,58";
        String testString5 = "123:ab.23";
        // Act
        int ms1 = Main.getMilliseconds(testString1);
        int ms2 = Main.getMilliseconds(testString2);
        int ms3 = Main.getMilliseconds(testString3);
        int ms4 = Main.getMilliseconds(testString4);
        int ms5 = Main.getMilliseconds(testString5);
        // Assert
        assertEquals(721300, ms1, "12:01.328 should be 721320 ms");
        assertEquals(45090, ms2, "45.09812 should be 45090 ms");
        assertEquals(744190, ms3, "Should disregard first words and parse 12:23.19, which is 744190 ms");
        assertEquals(580, ms4, "0,58 should be 580 ms");
        assertEquals(0, ms5, "Letters should render this response invalid and return 0");
    }
}
