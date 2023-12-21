package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    public static final int ID=0;
    public static final int NAME=0;
    public static final int WIKI=0;
    public static final int HEIGHT=0;

    public static Rocket of(String line){
        String[] t=line.split(",");
        Optional<String> wik=Optional.empty();
        Optional<Double> heigh=Optional.empty();
        if(!t[WIKI].isEmpty()){
            wik=Optional.of(t[WIKI]);
        }
        if(!t[HEIGHT].isEmpty()) {
            heigh=Optional.of(Double.parseDouble(t[HEIGHT]));
        }
            return new Rocket(t[ID], t[NAME], wik,heigh);
    }
}
