package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {
    public static final int ID = 0;
    public static final int COMPANY = 1;
    public static final int LOCATION = 2;
    public static final int DATE = 3;
    public static final int DETAIL = 4;
    public static final int ROCKET_STATUS = 5;
    public static final int COST = 6;
    public static final int MISSION_STATUS = 7;
    public static final int ROCKET_NAME = 0;
    public static final int PAYLOAD = 1;


    public static Mission of(String line) {
        String[] t = line.split(",");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM, uuuu");
        LocalDate date = LocalDate.parse(t[DATE], formatter);
        String[] m = t[DETAIL].split("\\|");
        RocketStatus rocketStatus;
        if (t[ROCKET_STATUS].equals("StatusActive")) {
            rocketStatus = RocketStatus.STATUS_ACTIVE;
        } else {
            rocketStatus = RocketStatus.STATUS_RETIRED;
        }
        MissionStatus missionStatus=switch(t[MISSION_STATUS]){
            case "Success"->MissionStatus.SUCCESS;
            case "Failure"->MissionStatus.FAILURE;
            case "Partial Failure"->MissionStatus.PARTIAL_FAILURE;
            case "Prelaunch Failure"->MissionStatus.PRELAUNCH_FAILURE;
            default->null;
        };
        Optional<Double> cost=Optional.empty();
        if(!t[COST].isEmpty()){
            cost=Optional.of(Double.valueOf(t[COST]));
        }
        return new Mission(t[ID], t[COMPANY], t[LOCATION], date, new Detail(m[ROCKET_NAME], m[PAYLOAD]),
                rocketStatus, cost, missionStatus);
    }
}
