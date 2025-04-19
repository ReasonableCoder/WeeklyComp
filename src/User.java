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
        return entries;
    }

    public String getEntryForEvent(int eventIndex) {
        return entries[eventIndex];
    }

    public boolean participatedInEvent(int eventIndex) {
        String entry = getEntryForEvent(eventIndex);
        return entry != null && !entry.trim().isEmpty() && !entry.equals("0");
    }
}