import java.util.ArrayList;

public class CompetitionEvent {
    private final String eventName;
    private final int eventIndex;
    private final User[] users;
    private final User[] rankedUsers;

    CompetitionEvent(String eventName, int eventIndex, User[] Users) {
        this.eventName = eventName;
        this.eventIndex = eventIndex;
        this.users = Users;
        this.rankedUsers = rankUsers();
    }

    public String getEventName() {
        return eventName;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public User[] getRankedUsers() {
        return rankedUsers.clone();
    }

    /**
     * Ranks the array of Users by result.
     * @return an array of Users from best to worst
     */
    private User[] rankUsers() {
        ArrayList<User> rankedUsers = new ArrayList<>();

        for (User currentUser : users) {
            if (!currentUser.participatedInEvent(eventIndex)) continue;

            boolean inserted = false;
            for (int i = 0; i < rankedUsers.size(); i++) {
                User rankedUser = rankedUsers.get(i);
                if (compareUsers(currentUser, rankedUser) >= 0) {
                    rankedUsers.add(i, currentUser);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) {
                rankedUsers.add(currentUser); // Add to end if it's the slowest time
            }
        }
        return rankedUsers.toArray(new User[0]);
    }

    /**
     * Retrieves the User(s) in first place. Most of the time will be a single User, but could be multiple in the case of a tie.
     * @return the Array of Users that won
     */
    public User[] getWinners() {
        if (rankedUsers.length == 0) return new User[0];
        ArrayList<User> winners = new ArrayList<>();
        User first = rankedUsers[0];
        winners.add(first);

        for  (int i = 1; i < rankedUsers.length; i++) {
            User current = rankedUsers[i];
            if (compareUsers(first, current) == 0) {
                winners.add(current);
            }
            else {
                break;
            }
        }
        return winners.toArray(new User[0]);
    }

    /**
     * Figures out the podium for this event, using real-world logic for placements in case of ties.
     * @return an array of arrays of Users, where index 0 is all first place Users, index 1 is second place, etc
     */
    // TODO: Make a isSingle() method in Utils, use it here to check for singles of the average events for tie breakers??
    public User[][] getPodium() {
        ArrayList<User[]> podiumList = new ArrayList<>();
        int place = 0;
        int index = 0;

        while (index < rankedUsers.length && place < 3) {
            ArrayList<User> tieGroup = new ArrayList<>();
            User base = rankedUsers[index];
            tieGroup.add(base);
            index++;

            while (index < rankedUsers.length && compareUsers(base, rankedUsers[index]) == 0) {
                tieGroup.add(rankedUsers[index]);
                index++;
            }

            podiumList.add(tieGroup.toArray(new User[0]));
            place += tieGroup.size(); // Advance the "place" count by how many filled this one
        }

        return podiumList.toArray(new User[0][]);
    }

    /**
     * Compares two users to determine which one had the better result for this event. ONLY TO BE USED FOR USERS WHO HAVE PARTICIPATED IN SAID EVENT.
     * @param first the first User to compare
     * @param second the second User to compare
     * @return an int greater than 0, equal to 0 or smaller than 0 depending on if the first user's result is better than the second, equal to the second, or worse than the second
     */
    private int compareUsers(User first, User second) {
        String firstResult = first.getEntryForEvent(eventIndex);
        String secondResult = second.getEntryForEvent(eventIndex);
        boolean isMulti = Utils.isMulti(eventName);

        if (isMulti) {
            int firstPoints = Utils.getMultiPoints(firstResult);
            int secondPoints = Utils.getMultiPoints(secondResult);
            if (firstPoints != secondPoints) return Integer.compare(firstPoints, secondPoints);
        }

        int firstMS = Utils.getMilliseconds(firstResult);
        int secondMS = Utils.getMilliseconds(secondResult);
        return Integer.compare(secondMS, firstMS);
    }
}