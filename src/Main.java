import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

// TODO: Maybe make a kinch podium??
// TODO: Check to see if a new server record was made by someone who doesn't have any server records to give them the role, and that the old server recorder holder has their role removed
// TODO: Maybe add a "real events" field to use to be sure the wasRecordBroken method actually checks the amount of events we're expecting it to? (ie. see if it was able to convert each event to its enum)
// TODO: Make and use User class, then turn the entries hashmap into an arraylist of the event names (will require a LOT of refactoring)
// TODO: Make the updateRecords method (maybe also the printRecords method) handle the case where more than one person has the record
public class Main {
    private static final String fileName = "stream all i wanted by paramore (Responses) - Form Responses 1" + ".csv";
    private static final int nonEvents = 2;
    private static final String recordsFile = "Reasonable Comp Records - Sheet1.csv";
    private static HashMap<String, String[]> entries;
    private static String[] eventNames;
    private static User[] users;
    public static void main(String[] args) {
        loadEntries();
        if (entries.isEmpty()) {
            System.out.println("Couldn't find any entries bruh fix yo shit.");
            System.exit(0);
        }
        HashMap<String, String[]> rankedEntries = getEventRankings();
        printPodiums(rankedEntries);
        Record[] currentRecords = getRecords();
        Record[] brokenRecords = getBrokenRecords(rankedEntries);
        if (brokenRecords.length > 0) {
            printRecords(brokenRecords);
            updateRecords(brokenRecords, currentRecords);
        }
        printWinners(rankedEntries);
    }

    /**
     * Reads the response csv file, gets the array of eventNames and the array of Users.
     */
    public static void loadEntries() {
        ArrayList<User> usernames = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            String[] elements;
            boolean firstLine = true;

            while ((elements = reader.readNext()) != null) {
                if (firstLine) {
                    eventNames = Arrays.copyOfRange(elements, 2, elements.length - nonEvents);
                    firstLine = false;
                    continue;
                }

                String username = elements[1];
                String[] responses = Arrays.copyOfRange(elements, 2, elements.length - nonEvents);
                usernames.add(new User(username, responses));
            }
            users = usernames.toArray(new User[0]);
        } catch (IOException e) {
            System.out.println("Couldn't read file '" + fileName + "'. You probably forgot to put it in the project directory dumbass.");
        } catch (CsvValidationException e) {
            System.out.println("Something is wrong with the CSV file.");
        }
    }

    /**
     * Converts a String of time into an int of the number of milliseconds.
     * @param response the String to be converted to ms
     * @return an int of the ms of the response
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


    public static HashMap<String, String[]> getEventRankings() {
        LinkedHashMap<String, String[]> eventRankings = new LinkedHashMap<>();
        for (int eventIndex = 0; eventIndex < entries.get("Events").length; eventIndex++) {
            String eventName = entries.get("Events")[eventIndex];
            ArrayList<String> ranking = new ArrayList<>();
            String eventNameLower = eventName.toLowerCase();
            boolean isMulti = eventNameLower.contains("multi") || eventNameLower.contains("mbld");
            for (String currentUser : entries.keySet()) {
                if (currentUser.equals("Events")) continue;
                String currentResponse = entries.get(currentUser)[eventIndex];
                if (isMulti) {
                    int currentPoints = getMultiPoints(currentResponse);
                    if (currentPoints < 0) continue; // if they got a DNF
                    boolean inserted = false;
                    for (int i = 0; i < ranking.size(); i++) {
                        String rankedUser = ranking.get(i);
                        String rankedResponse = entries.get(rankedUser)[eventIndex];
                        int rankedPoints = getMultiPoints(rankedResponse);
                        if (currentPoints > rankedPoints) {
                            ranking.add(i, currentUser);
                            inserted = true;
                            break;
                        }
                        else if (currentPoints == rankedPoints) {
                            // Compare time if points tie
                            int currentMS = getMilliseconds(currentResponse);
                            int rankedMS = getMilliseconds(rankedResponse);
                            if (currentMS <= rankedMS) {
                                ranking.add(i, currentUser);
                                inserted = true;
                                break;
                            }
                        }
                    }
                    if (!inserted) {
                        ranking.add(currentUser); // Add to end if it's the slowest time
                    }
                } else{
                    int currentMS = getMilliseconds(currentResponse);
                    if (currentMS == 0) continue;
                    boolean inserted = false;
                    for (int i = 0; i < ranking.size(); i++) {
                        String rankedUser = ranking.get(i);
                        int rankedMS = getMilliseconds(entries.get(rankedUser)[eventIndex]);
                        if (currentMS <= rankedMS) {
                            ranking.add(i, currentUser);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) {
                        ranking.add(currentUser); // Add to end if it's the slowest time
                    }
                }
            }
            if (ranking.isEmpty()) continue;
            eventRankings.put(eventName, ranking.toArray(String[]::new));
        }
        return eventRankings;
    }


    // TODO: (might not be possible) add conditional so that if podium is more than 3 for an average, compare the tier's singles to declare the actual podium like in comps
    // TODO: Above might be possible if I make a method for cleaning strings and finding the RecordType for mean or average
    public static void printPodiums(HashMap<String, String[]> rankings) {
        String[] eventNames = entries.get("Events");
        for (String eventName : eventNames) {
            String[] rankedUsers = rankings.get(eventName);
            if (rankedUsers == null || rankedUsers.length == 0) continue;
            int eventIndex = Arrays.asList(eventNames).indexOf(eventName);
            boolean isMulti = eventName.toLowerCase().contains("multi") || eventName.toLowerCase().contains("mbld");
            System.out.println(eventName + ":");
            int rank = 1;
            int placeCount = 0;
            int tieCount = 1;
            String previousResult = null;
            for (String user : rankedUsers) {
                String result = entries.get(user)[eventIndex];
                boolean isTie = false;
                if (previousResult != null) {
                    if (isMulti) {
                        int prevPoints = Main.getMultiPoints(previousResult);
                        int currPoints = Main.getMultiPoints(result);
                        int prevTime = getMilliseconds(previousResult);
                        int currTime = getMilliseconds(result);
                        isTie = (prevPoints == currPoints) && (prevTime == currTime);
                    } else {
                        int prevTime = getMilliseconds(previousResult);
                        int currTime = getMilliseconds(result);
                        isTie = prevTime == currTime;
                    }
                    if (!isTie) {
                        rank += tieCount;
                        tieCount = 1;
                    } else {
                        tieCount++;
                    }
                }
                // Print
                System.out.printf("%d. %s %s%n", rank, user, isMulti ? result : formatTime(getMilliseconds(result)));
                previousResult = result;
                placeCount++;
                if (placeCount >= 3) break;
            }
            System.out.println(); // Blank line between events
        }
    }


    public static void printWinners(HashMap<String, String[]> rankings) {
        Set<String> winners = new HashSet<>();
        String[] eventNames = entries.get("Events");

        for (Map.Entry<String, String[]> entry : rankings.entrySet()) {
            String[] rankedUsers = entry.getValue();
            String key = entry.getKey();
            boolean isMulti = key.toLowerCase().contains("multi") || key.toLowerCase().contains("mbld");
            int eventIndex = Arrays.asList(eventNames).indexOf(key);

            String firstResponse = entries.get(rankedUsers[0])[eventIndex];
            int firstTime = getMilliseconds(firstResponse);
            int firstPoints = isMulti ? getMultiPoints(firstResponse) : 0;

            for (String user : rankedUsers) {
                String userResponse = entries.get(user)[eventIndex];
                int userTime = getMilliseconds(userResponse);
                int userPoints = isMulti ? getMultiPoints(userResponse) : 0;

                if (userTime == firstTime && userPoints == firstPoints) {
                    winners.add(user);
                } else {
                    break;
                }
            }
        }
        System.out.println("Winners: " + String.join(", ", winners));
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

    // TODO: Make this able to handle multiple people breaking a record at the same time
    public static Record[] getBrokenRecords(HashMap<String, String[]> rankedEntries) {
        ArrayList<Record> brokenRecords = new ArrayList<>();
        Record[] records = getRecords();
        String[] singleStrings = {"single", "bo1", "best", "best single", "attempt"};
        String[] averageStrings = {"average", "average of 5", "ao5", "avg", "avg of 5"};
        String[] meanStrings = {"mean", "mean of 3", "mo3"};
        String[] allNoiseWords = Stream.concat(Stream.concat(Arrays.stream(singleStrings), Arrays.stream(averageStrings)), Arrays.stream(meanStrings)).toArray(String[]::new);
        String regex = "(?i)\\b(" + String.join("|", allNoiseWords) + ")\\b";
        List<String> allEvents = Arrays.asList(entries.get("Events"));
        for (String event : rankedEntries.keySet()) {
            // Hopefully get rid of everything in the event name except for the actual Event
            String baseEvent = event.replaceAll(regex, "").replace(":", "").strip().replaceAll("\\s{2,}", " ");
            Event realEvent = Event.fromName(baseEvent);
            if (realEvent == null) continue;
            RecordType type;
            String eventLower = event.toLowerCase();
            if (Arrays.stream(singleStrings).anyMatch(eventLower::contains)) {
                type = RecordType.SINGLE;
            }
            else if (Arrays.stream(averageStrings).anyMatch(eventLower::contains)) {
                type = RecordType.AVERAGE;
            }
            else if (Arrays.stream(meanStrings).anyMatch(eventLower::contains)) {
                type = RecordType.MEAN;
            }
            else {
                type = null;
            }
            String user = rankedEntries.get(event)[0];
            int eventIndex = allEvents.indexOf(event);
            String response = entries.get(user)[eventIndex];
            Record potentialRecord;
            boolean isMulti = realEvent == Event.MULTIBLIND;
            if (isMulti) {
                potentialRecord = new Record(response, user);
            }
            else {
                if (type == null) continue;
                int userMS = getMilliseconds(response);
                potentialRecord = new Record(realEvent, type, userMS, user);
            }
            for (Record record : records) {
                if (potentialRecord.compareTo(record) > 0) {
                    brokenRecords.add(potentialRecord);
                    break;
                }
            }
        }
        return brokenRecords.toArray(Record[]::new);
    }

    /**
     * Retrieves the current records in the csv file.
     * @return an array of Record objects for each server record
     */
    private static Record[] getRecords() {
        ArrayList<Record> records = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(recordsFile))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[0].isEmpty() || line[0].equals("Reasonable Weekly Competition Records")) continue;
                String event = line[0].replace(":", "");
                Event realEvent = Event.fromName(event);
                RecordType type;
                int time;
                String recordHolder;
                if (event.equals("Multi")) {
                    String result = line[1];
                    recordHolder = line[2];
                    Record currentRecord = new Record(result, recordHolder); // Multi-specific Record
                    records.add(currentRecord);
                    break; // Always the last event, so end here
                }
                else {
                    // Loop twice for both single and average of each event
                    for (int i = 0; i < 2; i++) {
                        line = reader.readNext();
                        String typeStr = line[0].replace(":", "");
                        type = RecordType.valueOf(typeStr.toUpperCase());
                        time = getMilliseconds(line[1]);
                        recordHolder = line[2];
                        Record currentRecord = new Record(realEvent, type, time, recordHolder);
                        records.add(currentRecord);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't read file '" + recordsFile + "'. You probably forgot to put it in the project directory dumbass.");
        } catch (CsvValidationException e) {
            System.out.println("Something is wrong with the CSV file.");
        }
        return records.toArray(Record[]::new);
    }


    public static void printRecords(Record[] brokenRecords) {
        int recordNum = brokenRecords.length;
        if (recordNum == 1) {
            System.out.println("**WE HAVE A NEW SERVER RECORD!**");
        }
        else {
            System.out.println("**WE HAVE " + recordNum + " NEW SERVER RECORDS!**");
        }
        for (Record record : brokenRecords) {
            System.out.println(record.toString());
        }
    }


    public static void updateRecords(Record[] newRecords, Record[] oldRecords) {
        // First, update the old records with the new ones
        for (Record newRecord : newRecords) {
            Event newEvent = newRecord.getEvent();
            RecordType newType = newRecord.getRecordType();
            for (int index = 0; index < oldRecords.length; index++) {
                Record oldRecord = oldRecords[index];
                Event oldEvent = oldRecord.getEvent();
                RecordType oldType = oldRecord.getRecordType();
                if (newEvent == oldEvent && newType == oldType) {
                    oldRecords[index] = newRecord;
                }
            }
        }
        // Then, write everything to the csv file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("TestFile.csv"))) {
            writer.write("Reasonable Weekly Competition Records,,");
            writer.newLine();
            for (Record record : oldRecords) {
                Event event = record.getEvent();
                String recordHolder = record.getRecordHolder();
                if (event == Event.MULTIBLIND) {
                    writer.write(",,");
                    writer.newLine();
                    writer.write(event + ":," + record.getMultiResult() + "," + recordHolder);
                    break; // Multi is always the last event
                }
                String time = formatTime(record.getTime());
                if (time.equals("0.00")) time = "";
                RecordType type = record.getRecordType();
                if (type == RecordType.SINGLE) {
                    writer.write(",,");
                    writer.newLine();
                    writer.write(event + ":,,");
                    writer.newLine();
                }
                writer.write(type + ":," + time + "," + recordHolder);
                writer.newLine();
            }
        }
        catch (IOException e) {
            System.out.println("Couldn't write records to file '" + recordsFile + "'.");
        }
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
            return -1;
        }
    }
}