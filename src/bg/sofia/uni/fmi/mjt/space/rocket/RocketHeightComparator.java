package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Comparator;

public class RocketHeightComparator implements Comparator<Rocket> {
    @Override
    public int compare(Rocket o1, Rocket o2) {
        if (o1.height().isPresent() && o2.height().isPresent()) {
            return -Double.compare(o1.height().get(), o2.height().get());
        } else if (o1.height().isPresent()) {
            return 1;
        } else if (o2.height().isPresent()) {
            return -1;
        } else {
            return 0;
        }
    }
}
