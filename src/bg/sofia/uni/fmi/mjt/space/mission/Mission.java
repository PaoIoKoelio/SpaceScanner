package bg.sofia.uni.fmi.mjt.space.mission;


import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import java.util.Locale;
import java.util.Optional;

public record Mission(String id, String company, String location, LocalDate date, Detail detail,
                      RocketStatus rocketStatus, Optional<Double> cost, MissionStatus missionStatus) {

    public static final int ROCKET_NAME = 0;
    public static final int PAYLOAD = 1;


    public static Mission of(String line) {
        String[] b=line.split("\"");
        String[] t1 = b[0].split(",");
        String[] t2=b[4].split(",");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd, yyyy").withLocale(Locale.US);
        LocalDate date = LocalDate.parse(b[3], formatter);
        String[] m = t2[1].split("\\|");
        RocketStatus rocketStatus;
        MissionStatus missionStatus;
        if (t2[2].equals("StatusActive")) {
            rocketStatus = RocketStatus.STATUS_ACTIVE;
        } else {
            rocketStatus = RocketStatus.STATUS_RETIRED;
        }
        Optional<Double> cost=Optional.empty();
        if(b.length>=7){
            cost=Optional.of(Double.valueOf(b[5]));
            missionStatus=switch(b[6].split(",")[1]){
                case "Success"->MissionStatus.SUCCESS;
                case "Failure"->MissionStatus.FAILURE;
                case "Partial Failure"->MissionStatus.PARTIAL_FAILURE;
                case "Prelaunch Failure"->MissionStatus.PRELAUNCH_FAILURE;
                default->null;
            };
        }
        else{
            missionStatus=switch(t2[3]){
                case "Success"->MissionStatus.SUCCESS;
                case "Failure"->MissionStatus.FAILURE;
                case "Partial Failure"->MissionStatus.PARTIAL_FAILURE;
                case "Prelaunch Failure"->MissionStatus.PRELAUNCH_FAILURE;
                default->null;
            };
        }
        return new Mission(t1[0], t1[1], b[1], date, new Detail(m[ROCKET_NAME], m[PAYLOAD]),
                rocketStatus, cost, missionStatus);
    }
}

