import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.util.stream.Stream;

// TODO: Maybe make a kinch podium??
// TODO: Check to see if a new server record was made by someone who doesn't have any server records to give them the role, and that the old server recorder holder has their role removed
// TODO: Maybe add a "real events" field to use to be sure the wasRecordBroken method actually checks the amount of events we're expecting it to? (ie. see if it was able to convert each event to its enum)
// TODO: Make the updateRecords method (maybe also the printRecords method) handle the case where more than one person has the record
// TODO: Maybe add RecordType field to EventRanking to be able to rank on single if an average tied
public class Main {
    private static final String fileName = "stream all i wanted by paramore (Responses) - Form Responses 1.csv";
    private static final String recordsFile = "Reasonable Comp Records - Sheet1.csv";
    private static final int nonEvents = 2;
    private static String[] eventNames;
    private static User[] users;
    private static CompetitionEvent[] competitionEvents;

    public static void main(String[] args) {
        loadEntries();
        competitionEvents = getCompetitionEvents();
        printPodiums();

        Record[] currentRecords = getRecords();
        Record[] brokenRecords = getBrokenRecords();
        if (brokenRecords.length > 0) {
            printRecords(brokenRecords);
            updateRecords(brokenRecords, currentRecords);
        }
        printWinners();
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
     * For each event, gets the array of users who have competed and makes a CompetitionEvent object for it.
     * @return an Array of each CompetitionEvent for this comp
     */
    public static CompetitionEvent[] getCompetitionEvents() {
        ArrayList<CompetitionEvent> competitionEvents = new ArrayList<>();

        for (int eventIndex = 0; eventIndex < eventNames.length; eventIndex++) {
            String eventName = eventNames[eventIndex];
            ArrayList<User> usersInEvent = new ArrayList<>();

            for (User user : users) {
                if (user.participatedInEvent(eventIndex)) usersInEvent.add(user);
            }

            competitionEvents.add(new CompetitionEvent(eventName, eventIndex, usersInEvent.toArray(new User[0])));
        }

        return competitionEvents.toArray(new CompetitionEvent[0]);
    }

    // TODO: make some formatTime method for multi results maybe?
    public static void printPodiums() {
        for (CompetitionEvent competitionEvent : competitionEvents) {
            User[][] podium = competitionEvent.getPodium();
            if (podium.length == 0) continue; // Skip if no one made it

            String eventName = competitionEvent.getEventName();
            boolean isMulti = Utils.isMulti(eventName);
            System.out.println(eventName);

            int placement = 1;
            for (User[] userGroup : podium) {

                for (User user : userGroup) {
                    String entry = user.getEntryForEvent(competitionEvent.getEventIndex());
                    String result = isMulti ? entry : Utils.formatTime(Utils.getMilliseconds(entry));
                    System.out.println(placement + ". " + user.getUsername() + " " + result);
                }

                placement += userGroup.length;
                if (placement > 3) break;
            }

            System.out.println(); // Blank line between events
        }
    }

    /**
     * Prints the winners of this comp, used for setting roles.
     */
    public static void printWinners() {
        Set<String> winners = new HashSet<>();

        for (CompetitionEvent competitionEvent : competitionEvents) {
            for (User winner : competitionEvent.getWinners()) {
                winners.add(winner.getUsername());
            }
        }
        System.out.println("Winners: " + String.join(", ", winners));
    }

    // TODO: Make this able to handle multiple people breaking a record at the same time
    public static Record[] getBrokenRecords() {
        ArrayList<Record> brokenRecords = new ArrayList<>();
        Record[] records = getRecords();
        String[] singleStrings = {"single", "bo1", "best", "best single", "attempt"};
        String[] averageStrings = {"average", "average of 5", "ao5", "avg", "avg of 5"};
        String[] meanStrings = {"mean", "mean of 3", "mo3"};
        String[] allNoiseWords = Stream.concat(Stream.concat(Arrays.stream(singleStrings), Arrays.stream(averageStrings)), Arrays.stream(meanStrings)).toArray(String[]::new);
        String regex = "(?i)\\b(" + String.join("|", allNoiseWords) + ")\\b";

        for (CompetitionEvent competitionEvent : competitionEvents) {
            String eventName = competitionEvent.getEventName();
            // Hopefully get rid of everything in the event name except for the actual Event
            String baseEvent = eventName.replaceAll(regex, "").replace(":", "").strip().replaceAll("\\s{2,}", " ");
            Event realEvent = Event.fromName(baseEvent);
            if (realEvent == null) continue;

            RecordType type;
            String eventLower = eventName.toLowerCase();
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

            int eventIndex = competitionEvent.getEventIndex();
            User[] winners = competitionEvent.getWinners();
            if (winners.length == 0) continue;

            // Since all winners of an event tie, only check the first to see if the record was broken
            User winner = winners[0];
            String response = winner.getEntryForEvent(eventIndex);
            String username = winner.getUsername();
            Record potentialRecord;

            boolean isMulti = realEvent == Event.MULTIBLIND;
            if (isMulti) {
                potentialRecord = new Record(response, username);
            }
            else {
                if (type == null) continue;
                int userMS = Utils.getMilliseconds(response);
                potentialRecord = new Record(realEvent, type, userMS, username);
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
                        time = Utils.getMilliseconds(line[1]);
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
        System.out.println(); // New line for space between records and winners
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
                String time = Utils.formatTime(record.getTime());
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

    public static String[] getEventNames() {
        return eventNames.clone();
    }
}