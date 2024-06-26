package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    public static final int ID=0;
    public static final int NAME=1;
    public static final int WIKI=2;
    public static final int HEIGHT=3;

    public static Rocket of(String line){
        String[] t=line.split(",");
        Optional<String> wiki=Optional.empty();
        Optional<Double> height=Optional.empty();
        if(!t[WIKI].isEmpty()){
            wiki=Optional.of(t[WIKI]);
        }
        if(!t[HEIGHT].isEmpty()) {
            height=Optional.of(Double.parseDouble(t[HEIGHT]));
        }
        return new Rocket(t[ID], t[NAME], wiki, height);
    }
}

