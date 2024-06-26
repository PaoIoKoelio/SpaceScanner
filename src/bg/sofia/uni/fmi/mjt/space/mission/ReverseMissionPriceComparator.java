package bg.sofia.uni.fmi.mjt.space.mission;

import java.util.Comparator;

public class ReverseMissionPriceComparator implements Comparator<Mission> {
    @Override
    public int compare(Mission o1, Mission o2) {
        return -Double.compare(o1.cost().get(),o2.cost().get());
    }
}

