package bg.sofia.uni.fmi.mjt.space.mission;

import java.util.Comparator;

public class MissionPriceComparator implements Comparator<Mission> {

    @Override
    public int compare(Mission o1, Mission o2) {
        return o1.cost().get().compareTo(o2.cost().get());
    }
}
