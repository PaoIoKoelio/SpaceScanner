package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Comparator;

public class RocketHeightComparator implements Comparator<Rocket> {
    @Override
    public int compare(Rocket o1, Rocket o2) {
        return Double.compare(o1.height().get(), o2.height().get());
    }
}
