package bg.sofia.uni.fmi.mjt.space.rocket;

import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;

import java.util.Collection;
import java.util.Comparator;

public class RocketReliabilityComparator implements Comparator<Rocket> {
    private Collection<Mission> missions;

    public RocketReliabilityComparator(Collection<Mission> missions) {
        this.missions=missions;
    }

    @Override
    public int compare(Rocket o1, Rocket o2) {
        double suc1 = missions.stream()
                .filter(mission -> mission.detail().rocketName().equals(o1.name()))
                .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS)).count();
        double suc2 = missions.stream()
                .filter(mission -> mission.detail().rocketName().equals(o2.name()))
                .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS)).count();
        double fail1=missions.stream()
                .filter(mission -> mission.detail().rocketName().equals(o1.name()))
                .filter(mission -> !mission.missionStatus().equals(MissionStatus.SUCCESS)).count();
        double fail2=missions.stream()
                .filter(mission -> mission.detail().rocketName().equals(o2.name()))
                .filter(mission -> !mission.missionStatus().equals(MissionStatus.SUCCESS)).count();
        double rel1=(2*suc1+fail1)/(suc1+fail1);
        double rel2=(2*suc2+fail2)/(suc2+fail2);
        return Double.compare(rel1, rel2);
    }
}
