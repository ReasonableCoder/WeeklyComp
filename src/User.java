public class User {
    private final String username;
    private final String[] entries;

    User(String username, String[] entries) {
        this.username = username;
        this.entries = entries;
    }

    public String getUsername() {
        return username;
    }

    public String[] getEntries() {
        return entries.clone();
    }

    public String getEntryForEvent(int eventIndex) {
        return entries[eventIndex];
    }

    public boolean participatedInEvent(int eventIndex) {
        String entry = getEntryForEvent(eventIndex);
        boolean isMulti = Utils.isMulti(Main.getEventNames()[eventIndex]);
        if (isMulti) return Utils.getMultiPoints(entry) >= 0;
        return entry != null && !entry.trim().isEmpty() && !(Utils.getMilliseconds(entry) == 0);
    }
}