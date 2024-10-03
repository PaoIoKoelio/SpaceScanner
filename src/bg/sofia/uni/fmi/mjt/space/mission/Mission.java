package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {

    private static final int ROCKET_NAME = 0;
    private static final int PAYLOAD = 1;
    private static final int FOURTH_PART = 4;
    private static final int THIRD_PART = 3;

    private static final int LENGTH_WHEN_COST_IS_KNOWN = 7;
    private static final int COST = 5;
    private static final int MISSION_STATUS_WHEN_COST_IS_KNOWN = 6;
    private static final int MISSION_STATUS_WHEN_COST_IS_NOT_KNOWN = 4;

    private static RocketStatus createRocketStatus(String rocketStatusString) {
        if (rocketStatusString.equals("StatusActive")) {
            return RocketStatus.STATUS_ACTIVE;
        } else {
            return RocketStatus.STATUS_RETIRED;
        }
    }

    private static MissionStatus createMissionStatus(String missionStatusString) {
        return switch (missionStatusString) {
            case "Success" -> MissionStatus.SUCCESS;
            case "Failure" -> MissionStatus.FAILURE;
            case "Partial Failure" -> MissionStatus.PARTIAL_FAILURE;
            case "Prelaunch Failure" -> MissionStatus.PRELAUNCH_FAILURE;
            default -> null;
        };
    }

    public static Mission of(String line) {
        String[] b = line.split("\"");
        String[] t1 = b[0].split(",");
        String[] t2 = b[FOURTH_PART].split(",");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd, uuuu", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(b[THIRD_PART], formatter);
        String[] m = t2[1].split(" \\| ");
        RocketStatus rocketStatus = Mission.createRocketStatus(t2[2]);
        MissionStatus missionStatus;
        Optional<Double> cost = Optional.empty();
        if (b.length >= LENGTH_WHEN_COST_IS_KNOWN) {
            cost = Optional.of(Double.valueOf(b[COST]));
            missionStatus = createMissionStatus(b[MISSION_STATUS_WHEN_COST_IS_KNOWN].split(",")[1]);
        } else {
            missionStatus = createMissionStatus(t2[MISSION_STATUS_WHEN_COST_IS_NOT_KNOWN]);
        }
        return new Mission(t1[0], t1[1], b[1], date, new Detail(m[ROCKET_NAME], m[PAYLOAD]),
                rocketStatus, cost, missionStatus);
    }
}

