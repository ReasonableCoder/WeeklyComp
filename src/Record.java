public class Record implements Comparable<Record> {

    private final Event event;
    private final int time;
    private final String recordHolder;
    private final RecordType type;
    private final String multiResult;

    Record(Event event, RecordType type, int time, String recordHolder) {
        this.event = event;
        this.type = type;
        this.time = time;
        this.recordHolder = recordHolder;
        this.multiResult = null; // Not applicable unless multi
    }

    /**
     * Specific constructor ONLY for Multi.
     * @param multiResult String of the result, in the format (solved)/(attempted) in (time)
     * @param recordHolder String name of the user who set the record
     */
    Record(String multiResult, String recordHolder) {
        this.event = Event.MULTIBLIND;
        this.type = RecordType.MULTI;
        this.multiResult = multiResult;
        String[] resultParts = multiResult.split(" ");
        this.time = Main.getMilliseconds(resultParts[2]);
        this.recordHolder = recordHolder;
    }

    public Event getEvent() {
        return event;
    }

    public int getTime() {
        return time;
    }

    public String getRecordHolder() {
        return recordHolder;
    }

    public RecordType getRecordType() {
        return type;
    }

    public String getMultiResult() {
        return multiResult;
    }

    @Override
    public int compareTo(Record other) {
        if (this.event != other.getEvent() || this.type != other.getRecordType()) {
            return -1;
        }

        if (this.event == Event.MULTIBLIND) {
            // Compare Multi-Blind results
            int pointComparison = Integer.compare(Main.getMultiPoints(this.multiResult), Main.getMultiPoints(other.getMultiResult()));
            if (pointComparison == 0) {
                // If points are the same, the faster time wins
                return Integer.compare(other.getTime(), this.time);
            }
            return pointComparison;
        }

        // For all other events, lower time is better
        return Integer.compare(other.getTime(), this.time);
    }


    @Override
    public String toString() {
        if (type == RecordType.MULTI) {
            return event.toString() + " result of " + multiResult + " by " + recordHolder;
        }
        return event.toString() + " " + type.toString() + " of " + Main.formatTime(time) + " by " + recordHolder;
    }
}
