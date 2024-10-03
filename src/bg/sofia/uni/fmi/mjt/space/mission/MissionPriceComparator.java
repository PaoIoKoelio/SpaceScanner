package bg.sofia.uni.fmi.mjt.space.mission;

import java.util.Comparator;

public class MissionPriceComparator implements Comparator<Mission> {

    @Override
    public int compare(Mission o1, Mission o2) {
        if (o1.cost().isPresent() && o2.cost().isPresent()) {
            return o1.cost().get().compareTo(o2.cost().get());
        } else if (o1.cost().isPresent()) {
            return -1;
        } else if (o2.cost().isPresent()) {
            return 1;
        } else {
            return 0;
        }
    }
}
