import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class Utils {
    /**
     * Converts a String of time into an int of the number of milliseconds.
     * @param response the String to be converted to ms
     * @return the int ms of the response
     */
    public static int getMilliseconds(String response) {
        response = response.replace("\"", "").replace(",", ".").strip();
        String time = "0"; // Placeholder value to indicate no response

        // In case someone wrote the time with a message next to it
        String[] words = response.split(" ");
        for (String word : words) {
            if (word.matches("^[0-9:.]+$")) {
                time = word;
                break;
            }
        }

        int hours = 0;
        int minutes = 0;
        String decimalSeconds;

        if (time.equals("0")) { // Only happens if response was of an invalid format
            return 0;
        }

        else if (time.contains(":")) {
            String[] timeParts = time.split(":");
            int mIndex = 0; // Default assumption the time is in minutes, so first part is minutes
            int sIndex = 1; // Default assumption the second part is seconds

            if (timeParts.length > 3) { // Time should never be more than in hours
                return 0;
            }

            else if (timeParts.length == 3) {
                // Time is in hours, so first part is hours, second is minutes, third is seconds
                hours = Integer.parseInt(timeParts[0]);
                mIndex = 1;
                sIndex = 2;
            }

            minutes = Integer.parseInt(timeParts[mIndex]);
            decimalSeconds = timeParts[sIndex];
        }

        else {
            decimalSeconds = time;
        }

        // Convert seconds to milliseconds
        BigDecimal seconds = new BigDecimal(decimalSeconds).setScale(2, RoundingMode.DOWN);
        BigDecimal milliseconds = seconds.multiply(new BigDecimal("1000"));

        // Turn whole time to milliseconds
        long totalMilliseconds = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes) + milliseconds.longValue();
        return (int) totalMilliseconds;
    }

    /**
     * Finds the score of a multiblind attempt.
     * @param multiResult String that MUST be of the form "x/y in (time)" where x and y are integers, which is the result of the multi attempt
     * @return the points of the multi attempt. A negative int indicates a DNF
     */
    public static int getMultiPoints(String multiResult) {
        try {
            String[] resultParts = multiResult.split(" ");
            String[] scoreParts = resultParts[0].split("/");
            int solved = Integer.parseInt(scoreParts[0]);
            if (solved < 2) {
                return -1;
            }
            int attempted = Integer.parseInt(scoreParts[1]);
            int unsolved = attempted - solved;
            return solved - unsolved;
        }
        catch (Exception e) {
            System.err.println("Invalid multi result: " + multiResult);
            return -1;
        }
    }

    public static String formatTime(int ms) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
        long centis = (ms % 1000) / 10;
        if (minutes > 0)
            return String.format("%d:%02d.%02d", minutes, seconds, centis);
        else
            return String.format("%d.%02d", seconds, centis);
    }

    /**
     * To find if an event is multiblind or not.
     * @param eventName the String name that we check
     * @return true if eventName contains multi, false otherwise
     */
    public static boolean isMulti(String eventName) {
        String lowerEventName = eventName.toLowerCase();
        return lowerEventName.contains("multi") || lowerEventName.contains("mbld");
    }
}