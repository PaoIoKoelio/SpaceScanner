package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionPriceComparator;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.mission.ReverseMissionPriceComparator;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketHeightComparator;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketReliabilityComparator;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {
    private Rijndael rijndael;
    private Collection<Mission> missions;
    private Collection<Rocket> rockets;

    public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
        rijndael = new Rijndael(secretKey);
        try (var missReader = new BufferedReader(missionsReader);
             var rockReader = new BufferedReader(rocketsReader)) {
            missions = missReader.lines().skip(1).map(Mission::of).toList();
            rockets = rockReader.lines().skip(1).map(Rocket::of).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from the file", e);
        }
    }

    @Override
    public Collection<Mission> getAllMissions() {
        return missions;
    }

    @Override
    public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
        if (missionStatus == null) {
            throw new IllegalArgumentException();
        }
        return missions.stream().filter(mission -> mission.missionStatus()
                .equals(missionStatus)).collect(Collectors.toList());
    }

    @Override
    public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
        if (to == null || from == null) {
            throw new IllegalArgumentException("Null argument.");
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("Wrong dates.");
        }
        return missions.stream()
                .filter(mission -> (mission.date().isAfter(from) && mission.date().isBefore(to)))
                .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS))
                .collect(Collectors.groupingBy(Mission::company, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)  // Extract the key (String) from the Map.Entry if present
                .orElse("");             // Return an empty string if the Optional is empty
    }

    @Override
    public Map<String, Collection<Mission>> getMissionsPerCountry() {
        Map<String, List<Mission>> stringCollectionMap = missions.stream().collect(
                Collectors.groupingBy(mission -> {
                    String[] t = mission.location().split(", ");
                    return t[t.length - 1];
                }));
        Map<String, Collection<Mission>> missionsForCountry = new LinkedHashMap<>();
        for (String s : stringCollectionMap.keySet()) {
            Collection<Mission> missions1 = new ArrayList<>(stringCollectionMap.get(s));
            missionsForCountry.put(s, missions1);
        }
        return missionsForCountry;
    }

    @Override
    public List<Mission> getTopNLeastExpensiveMissions(int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (missionStatus == null || rocketStatus == null || n <= 0) {
            throw new IllegalArgumentException();
        }
        return missions.stream().filter(mission -> mission.missionStatus().equals(missionStatus))
                .filter(mission -> mission.rocketStatus().equals(rocketStatus))
                .filter(mission -> mission.cost().isPresent())
                .sorted(new MissionPriceComparator())
                .limit(n)
                .toList();
    }

    @Override
    public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
        Map<String, String> locationForCompany = new LinkedHashMap<>();
        Map<String, List<Mission>> missionsForCompany = missions.stream()
                .collect(Collectors.groupingBy(Mission::company));
        for (String s : missionsForCompany.keySet()) {
            Map<String, Long> m = missionsForCompany.get(s).stream()
                    .collect(Collectors.groupingBy(Mission::location, Collectors.counting()));
            locationForCompany.put(s, m.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(""));
        }
        return locationForCompany;
    }

    @Override
    public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
        if (to == null || from == null) {
            throw new IllegalArgumentException();
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("Wrong dates");
        }
        Map<String, List<Mission>> locationForCompany = missions.stream()
                .filter(mission -> mission.date().isBefore(to) && mission.date().isAfter(from))
                .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS))
                .collect(Collectors.groupingBy(Mission::company));
        Map<String, String> rocketsAndSuccessfulMissions = new LinkedHashMap<>();
        for (String s : locationForCompany.keySet()) {
            Map<String, Long> m = locationForCompany.get(s).stream()
                    .collect(Collectors.groupingBy(Mission::location, Collectors.counting()));
            rocketsAndSuccessfulMissions.put(s, m.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                    .orElse(""));
        }
        return rocketsAndSuccessfulMissions;
    }

    @Override
    public Collection<Rocket> getAllRockets() {
        return rockets;
    }

    @Override
    public List<Rocket> getTopNTallestRockets(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        return rockets.stream().filter(rocket -> rocket.height().isPresent())
                .sorted(new RocketHeightComparator()).limit(n).toList();
    }

    @Override
    public Map<String, Optional<String>> getWikiPageForRocket() {
        return rockets.stream().filter(rocket -> rocket.wiki().isPresent())
                .collect(Collectors.toMap(Rocket::name, Rocket::wiki));
    }

    @Override
    public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(
            int n, MissionStatus missionStatus, RocketStatus rocketStatus) {
        if (n <= 0 || missionStatus == null || rocketStatus == null) {
            throw new IllegalArgumentException();
        }
        List<String> rocketsUsedInMostExpensiveMissions = missions.stream()
                .filter(mission -> mission.missionStatus().equals(missionStatus)
                        && mission.rocketStatus().equals(rocketStatus))
                .filter(mission -> mission.cost().isPresent())
                .sorted(new ReverseMissionPriceComparator())
                .limit(n).map(mission -> mission.detail().rocketName()).toList();

        return rocketsUsedInMostExpensiveMissions.stream()
                .map(name -> rockets.stream()
                        .filter(rocket -> rocket.name().equals(name))
                        .findFirst()
                        .flatMap(Rocket::wiki)
                        .orElse("Unknown link"))
                .toList();
    }

    @Override
    public void saveMostReliableRocket(OutputStream outputStream, LocalDate from, LocalDate to) throws CipherException {
        if (outputStream == null || to == null || from == null) {
            throw new IllegalArgumentException();
        }
        if (to.isBefore(from)) {
            throw new TimeFrameMismatchException("Wrong dates");
        }
        Collection<Mission> filteredMissions = missions.stream()
                .filter(mission -> mission.date().isAfter(from)
                        && mission.date().isBefore(to)).toList();
        String rocketName = rockets.stream()
                .max(new RocketReliabilityComparator(filteredMissions))
                .map(Rocket::name)
                .orElse("Unknown");
        rijndael.encrypt(new ByteArrayInputStream(rocketName.getBytes()), outputStream);
    }
}