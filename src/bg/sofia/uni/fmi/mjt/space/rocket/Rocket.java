package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    private static final int ID = 0;
    private static final int NAME = 1;
    private static final int WIKI = 2;
    private static final int HEIGHT = 3;
    private static final String COMMA = ",";

    public static Rocket of(String line) {
        String[] t = line.split(COMMA);
        Optional<String> wiki = Optional.empty();
        Optional<Double> height = Optional.empty();
        if (!t[WIKI].isEmpty()) {
            wiki = Optional.of(t[WIKI]);
        }
        if (!t[HEIGHT].isEmpty()) {
            height = Optional.of(Double.parseDouble(t[HEIGHT].split(" ")[0]));
        }
        return new Rocket(t[ID], t[NAME], wiki, height);
    }
}

