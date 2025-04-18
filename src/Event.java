import java.util.Arrays;
import java.util.List;

public enum Event {
    THREE_BY_THREE("3x3", "3x3x3"),
    TWO_BY_TWO("2x2", "2x2x2"),
    FOUR_BY_FOUR("4x4", "4x4x4"),
    FIVE_BY_FIVE("5x5", "5x5x5"),
    SIX_BY_SIX("6x6", "6x6x6"),
    SEVEN_BY_SEVEN("7x7", "7x7x7"),
    BLINDFOLDED("3BLD", "BLD", "Blindfolded", "3x3 Blindfolded", "3 Blind", "3x3 BLD"),
    FMC("FMC", "Fewest Moves", "Fewest Moves Challenge"),
    OH("3x3 OH", "OH", "3x3OH", "3OH", "One Hand", "One Handed", "3x3 One Handed", "One-Handed", "3x3 One-Handed"),
    CLOCK("Clock", "Clonk"),
    MEGAMINX("Megaminx", "Mega"),
    PYRAMINX("Pyraminx", "Pyra"),
    SKEWB("Skewb", "Skweb", "Skoib", "scube"),
    SQUAN("Square-1", "Squan", "Square 1", "Square1", "Square One", "SQ1", "SQ-1"),
    FOUR_BY_FOUR_BLINDFOLDED("4BLD", "4x4 Blindfolded", "4 Blind", "4x4 BLD"),
    FIVE_BY_FIVE_BLINDFOLDED("5BLD", "5x5 Blindfolded", "5 Blind", "5x5 BLD"),
    MULTIBLIND("Multi", "Multiblind", "multiBLD", "MBLD", "Multi BLind", "Multi BLD", "multi result", "multiblind result", "mbld result", "multiBLD result", "multi blind result");

    private final List<String> aliases;

    Event(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static Event fromName(String name) {
        String cleanName = name.trim().toLowerCase();
        for (Event e : Event.values()) {
            for (String alias : e.aliases) {
                if (alias.toLowerCase().equals(cleanName)) {
                    return e;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return aliases.getFirst(); // default "display name"
    }
}
