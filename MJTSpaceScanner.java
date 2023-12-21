package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionPriceComparator;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketHeightComparator;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {
    Collection<Mission> missions;
    Collection<Rocket> rockets;
    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        try (var reader = new BufferedReader(missionsReader);
             var reader2=new BufferedReader(rocketsReader)) {
            missions = reader.lines().skip(1).map(Mission::of).toList();
            rockets = reader2.lines().skip(1).map(Rocket::of).toList();
        }
        catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from the file", e);
        }
    }

    @Override
    public Collection<Mission> getAllMissions() {
        return missions;
    }

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        return missions.stream().filter(mission->mission.missionStatus()
                .equals(missionStatus)).collect(Collectors.toSet());
    }

    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        Map<String, List<Mission>> companies=missions.stream()
                .filter(mission->(mission.date().isAfter(from)&&mission.date().isBefore(to)))
                .collect(Collectors.groupingBy(Mission::company, Mission::missionStatus))
    }

    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {

    }

    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        return missions.stream().filter(mission->mission.missionStatus().equals(missionStatus))
                .filter(mission->mission.rocketStatus().equals(rocketStatus))
                .sorted(new MissionPriceComparator()).limit(n).toList();
    }

    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        return null;
    }

    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        Map<String, Long> rocketsAndSuccessfulMissions=missions.stream()
                .filter(mission->mission.date().isBefore(to)&&mission.date().isAfter(from))
                .filter(mission->mission.missionStatus().equals(MissionStatus.SUCCESS))
                .collect(Collectors.groupingBy(Mission::location, Collectors.counting()));
        return rocketsAndSuccessfulMissions.entrySet().stream().max()
    }

    @Override
    public Collection<Rocket> getAllRockets() {
        return rockets;
    }

    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        return rockets.stream().filter(rocket->rocket.height().isPresent())
                .sorted(new RocketHeightComparator()).limit(n).toList();
    }

    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return rockets.stream().filter(rocket->rocket.wiki().isPresent()).collect(Collectors.toMap(Rocket::name, Rocket::wiki));
    }

    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        return rockets.stream().filter()
    }

    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        return rockets.stream().
    }
}
