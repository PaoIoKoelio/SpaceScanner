package bg.sofia.uni.fmi.mjt.spacetest;

import bg.sofia.uni.fmi.mjt.space.MJTSpaceScanner;
import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Detail;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.CookieHandler;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class MJTSpaceScannerTest {

    private static MJTSpaceScanner mjtSpaceScanner;
    private static MJTSpaceScanner mjtSpaceScanner2;

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int KEY_SIZE_IN_BITS = 128;

    private static Rijndael rijndael;

    private static final Rocket highestRocket = new Rocket("11", "Atlas V 541", Optional.of("https://en.wikipedia.org/wiki/Atlas_V"), Optional.of(62.2));

    private static final Rocket secondHighestRocket = new Rocket("12", "H-IIA 202", Optional.of("https://en.wikipedia.org/wiki/H-IIA"), Optional.of(53.0));

    private static final Mission mission = new Mission("0", "SpaceX", "LC-39A, Kennedy Space Center, Florida, USA", LocalDate.of(2020, 8, 7), new Detail("VLS-1", "Starlink V1 L9 & BlackSky"), RocketStatus.STATUS_ACTIVE, Optional.of(50.0), MissionStatus.SUCCESS);

    private static final Mission failedMission = new Mission("6", "Roscosmos", "Site 31/6, Baikonur Cosmodrome, Kazakhstan", LocalDate.of(2020, 7, 23), new Detail("Tsyklon-4M", "Progress MS-15"), RocketStatus.STATUS_ACTIVE, Optional.of(48.5), MissionStatus.FAILURE);

    private static final Mission secondFailedMission = new Mission("11", "Roscosmos", "Site 31/6, Baikonur Cosmodrome, Kazakhstan", LocalDate.of(2020, 4, 25), new Detail("Tsyklon-3", "Progress MS-14"), RocketStatus.STATUS_ACTIVE, Optional.of(48.5), MissionStatus.FAILURE);
    private static final String rocketss = "\"\",Name,Wiki,Rocket Height\n" +
            "0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m\n" +
            "1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m\n" +
            "2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m\n" +
            "3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m\n" +
            "4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m\n" +
            "5,Vector-H,https://en.wikipedia.org/wiki/Vector-H,18.3 m\n" +
            "6,Vector-R,https://en.wikipedia.org/wiki/Vector-R,13.0 m\n" +
            "7,Vega,https://en.wikipedia.org/wiki/Vega_(rocket),29.9 m\n" +
            "8,Vega C,https://en.wikipedia.org/wiki/Vega_(rocket),35.0 m\n" +
            "9,Vega E,https://en.wikipedia.org/wiki/Vega_(rocket),35.0 m\n" +
            "10,VLS-1,https://en.wikipedia.org/wiki/VLS-1,19.0 m\n" +
            "11,Atlas V 541,https://en.wikipedia.org/wiki/Atlas_V,62.2 m\n" +
            "12,H-IIA 202,https://en.wikipedia.org/wiki/H-IIA,53.0 m\n";
    private static final String missionss = "0,Company Name,Location,Datum,Detail,Status Rocket,\" Rocket\",Status Mission\n" +
            "0,SpaceX,\"LC-39A, Kennedy Space Center, Florida, USA\",\"Fri Aug 07, 2020\",VLS-1 | Starlink V1 L9 & BlackSky,StatusActive,\"50.0 \",Success\n" +
            "1,CASC,\"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China\",\"Thu Aug 06, 2020\",Vanguard | Gaofen-9 04 & Q-SAT,StatusActive,\"29.75 \",Success\n" +
            "2,SpaceX,\"Pad A, Boca Chica, Texas, USA\",\"Tue Aug 04, 2020\",Unha-2 | 150 Meter Hop,StatusActive,,Prelaunch Failure\n" +
            "3,Roscosmos,\"Site 200/39, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 30, 2020\",Vanguard | Ekspress-80 & Ekspress-103,StatusActive,\"65.0 \",Success\n" +
            "4,ULA,\"SLC-41, Cape Canaveral AFS, Florida, USA\",\"Thu Jul 30, 2020\",Atlas V 541 | Perseverance,StatusActive,\"145.0 \",Success\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\",Tsyklon-4M | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1,StatusActive,\"64.68 \",Success\n" +
            "6,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 23, 2020\",Tsyklon-4M | Progress MS-15,StatusActive,\"48.5 \",Failure\n" +
            "7,CASC,\"LC-101, Wenchang Satellite Launch Center, China\",\"Thu Jul 23, 2020\",Vega | Tianwen-1,StatusActive,,Success\n" +
            "8,SpaceX,\"SLC-40, Cape Canaveral AFS, Florida, USA\",\"Mon Jul 20, 2020\",Vector-H | ANASIS-II,StatusActive,\"50.0 \",Success\n" +
            "9,JAXA,\"LA-Y1, Tanegashima Space Center, Japan\",\"Sun Jul 19, 2020\",H-IIA 202 | Hope Mars Mission,StatusActive,\"90.0 \",Success\n" +
            "10,Northrop,\"LP-0B, Wallops Flight Facility, Virginia, USA\",\"Wed Jul 15, 2020\",Tsyklon-3 | NROL-129,StatusActive,\"46.0 \",Prelaunch Failure\n" +
            "11,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Sat Apr 25, 2020\",Tsyklon-3 | Progress MS-14,StatusActive,\"48.5 \",Failure\n";

    private static final String missions2 = "0,Company Name,Location,Datum,Detail,Status Rocket,\" Rocket\",Status Mission\n" +
            "11,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Sat Apr 25, 2020\",Tsyklon-3 | Progress MS-14,StatusActive,\"48.5 \",Partial Failure\n" +
            "10,Northrop,\"LP-0B, Wallops Flight Facility, Virginia, USA\",\"Wed Jul 15, 2020\",Tsyklon-3 | NROL-129,StatusActive,\"46.0 \",Prelaunch Failure\n" +
            "6,Roscosmos,\"Site 31/6, Baikonur Cosmodrome, Kazakhstan\",\"Thu Jul 23, 2020\",Tsyklon-4M | Progress MS-15,StatusActive,\"48.5 \",Failure\n" +
            "5,CASC,\"LC-9, Taiyuan Satellite Launch Center, China\",\"Sat Jul 25, 2020\",Tsyklon-4M | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1,StatusActive,\"64.68 \",Success\n";


    @BeforeAll
    static void setUp() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(KEY_SIZE_IN_BITS);
        SecretKey secretKey = keyGenerator.generateKey();

        rijndael = new Rijndael(secretKey);

        StringReader stringReaderMissions = new StringReader(missionss);
        StringReader stringReaderRockets = new StringReader(rocketss);
        StringReader stringReaderMissions2 = new StringReader(missions2);
        StringReader stringReaderRockets2 = new StringReader(rocketss);
        mjtSpaceScanner = new MJTSpaceScanner(stringReaderMissions, stringReaderRockets, secretKey);
        mjtSpaceScanner2 = new MJTSpaceScanner(stringReaderMissions2, stringReaderRockets2, secretKey);
    }


    @Test
    void testGetAllMissions() {
        Assertions.assertTrue(mjtSpaceScanner.getAllMissions().contains(mission), "The array of missions converted from the String should contain the mission with an id of 0.");
    }

    private boolean isMissionsEqual(Collection<Mission> m1, Collection<Mission> m2) {
        return m1.containsAll(m2) && m2.containsAll(m1);
    }

    @Test
    void testGetAllMissionsWithMissionStatus() {
        Assertions.assertTrue(isMissionsEqual(mjtSpaceScanner.getAllMissions(MissionStatus.FAILURE), List.of(failedMission, secondFailedMission)), "Only 2 missions have mission status failure in the example, so only they should be returned from the function.");
    }

    @Test
    void testGetAllMissionsWithNullMissionStatusThrowsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getAllMissions(null), "When null is passed as the mission status an Illegal Argument Exception should be thrown.");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMission() {
        Assertions.assertEquals(mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(LocalDate.of(2020, 7, 1), LocalDate.of(2020, 7, 29)), "CASC", "CASC should be the company with the most successful missions.");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionWithNullToDate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(null, LocalDate.of(2020, 7, 29)), "If null date is passed an Illegal Argument Exception should be thrown.");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionWithNullFromDate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(LocalDate.of(2020, 7, 29), null), "If null date is passed an Illegal Argument Exception should be thrown.");
    }

    @Test
    void testGetCompanyWithMostSuccessfulMissionWithWrongDates() {
        Assertions.assertThrows(TimeFrameMismatchException.class, () -> mjtSpaceScanner.getCompanyWithMostSuccessfulMissions(LocalDate.of(2020, 7, 29), LocalDate.of(2020, 7, 1)), "When from date is after to date Time frame mismatch exception should be thrown.");
    }

    @Test
    void testGetMissionPerCountry() {
        Assertions.assertTrue(mjtSpaceScanner.getMissionsPerCountry().get("USA").contains(mission), "The list of USA missions should contain the SpaceX mission");
    }

    @Test
    void testTopNLeastExpensiveMissions() {
        Assertions.assertEquals(mjtSpaceScanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE).getFirst().id(), "1", "The least expensive mission should be the mission with id of 1(CASC).");
    }

    @Test
    void testTopNLeastExpensiveMissionsWithNullRocketStatus() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(2, MissionStatus.SUCCESS, null), "When function is called with null rocket status should throw illegal argument exception");
    }

    @Test
    void testTopNLeastExpensiveMissionsWithNullMissionStatus() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(2, null, RocketStatus.STATUS_ACTIVE), "When function is called with null mission status should throw illegal argument exception");
    }

    @Test
    void testTopNLeastExpensiveMissionsWithNegativeNValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(-10, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE), "When n is a negative number an illegal argument exception should be passed.");
    }

    @Test
    void testTopNLeastExpensiveMissionsWithZeroValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNLeastExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE), "When n is 0 an illegal argument exception should be passed.");
    }

    @Test
    void testGetMostDesiredLocationForMissionsPerCompany() {
        Assertions.assertEquals(mjtSpaceScanner.getMostDesiredLocationForMissionsPerCompany().get("Roscosmos"), "Site 31/6, Baikonur Cosmodrome, Kazakhstan", "The most desired location for the Roscosmos company should be Site 31/6, Baikonur Cosmodrome, Kazakhstan");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompany() {
        Assertions.assertEquals(mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.of(2020, 3, 24), LocalDate.of(2020, 10, 27)).get("Roscosmos"), "Site 200/39, Baikonur Cosmodrome, Kazakhstan", "The location with the most successfull missions for Roscosmos should be Site 200/39, Baikonur Cosmodrome, Kazakhstan");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithNullFromDate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(null, LocalDate.of(2020, 10, 27)), "When date is null an illegal argument exception should be thrown.");
    }

    @Test
    void testGetLocationWithMostSuccessfulMissionsPerCompanyWithNullToDate() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.of(2020, 10, 27), null), "When date is null an illegal argument exception should be thrown.");
    }

    @Test
    void testGetAllRockets() {
        Assertions.assertTrue(mjtSpaceScanner.getAllRockets().contains(highestRocket));
    }

    @Test
    void testGetTopNTallestRocketsWithNegativeNValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNTallestRockets(-1), "Should Throw Illegal argument exception when N is a negative number.");
    }

    @Test
    void testGetTopNTallestRocketsWithZeroNValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getTopNTallestRockets(0), "Should Throw Illegal argument exception when N is 0.");
    }

    private boolean isRocketsEqual(Collection<Rocket> r1, Collection<Rocket> r2) {
        return r1.containsAll(r2) && r2.containsAll(r1);
    }

    @Test
    void testGetTopNTallestRockets() {
        Collection<Rocket> highestRockets = new ArrayList<>();
        highestRockets.add(highestRocket);
        highestRockets.add(secondHighestRocket);
        Assertions.assertTrue(isRocketsEqual(mjtSpaceScanner.getTopNTallestRockets(2), highestRockets), "The tallest rockets are Atlas-V 541 and HII-A 202");
    }

    @Test
    void testGetWikiPageForRocket() {
        Assertions.assertEquals(mjtSpaceScanner.getWikiPageForRocket().get("Tsyklon-3"), Optional.of("https://en.wikipedia.org/wiki/Tsyklon-3"), "The page for the Tsyklon-3 is https://en.wikipedia.org/wiki/Tsyklon-3");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissions() {
        Assertions.assertEquals(mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(2, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE), List.of("https://en.wikipedia.org/wiki/Atlas_V", "https://en.wikipedia.org/wiki/H-IIA"), "Should return Atlas-V 541 and H-IIA 202 wiki pages because they are used in the 2 most expensive missions");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNullMissionStatus() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, null, RocketStatus.STATUS_RETIRED), "When Mission Status is null an Illegal argument exception should be thrown.");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNullRocketStatus() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.FAILURE, null), "When Rocket Status is null an Illegal argument exception should be thrown.");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithNegativeValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(-11, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE), "When N is negative an Illegal argument exception should be thrown.");
    }

    @Test
    void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWithZeroValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE), "When N is 0 an Illegal argument exception should be thrown.");
    }

    @Test
    void testSaveMostReliableRocketWithWrongDates() {
        Assertions.assertThrows(TimeFrameMismatchException.class, () -> mjtSpaceScanner.saveMostReliableRocket(new ByteArrayOutputStream(), LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 12)), "The from date is after the to date, so the function should throw a Time Frame Mismatch Exception");
    }

    @Test
    void testSaveMostReliableRocketWithNullOutputStream() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> mjtSpaceScanner.saveMostReliableRocket(null, LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 18)), "When called with null outputstream it should throw Illegal argument exception.");
    }

    @Test
    void testSaveMostReliableRocket() throws CipherException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mjtSpaceScanner.saveMostReliableRocket(outputStream, LocalDate.of(2020, 4, 16), LocalDate.of(2020, 10, 20));
        InputStream encryptedInputStream = new ByteArrayInputStream(outputStream.toByteArray());

        outputStream.reset();

        rijndael.decrypt(encryptedInputStream, outputStream);
        String mostReliableRocket = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(mostReliableRocket, "Vanguard", "There are multiple rockets with the same reliability score, meaning the first rocket with the max reliability score should be saved(Vanguard).");
    }

    @Test
    void testSaveMostReliableRocket2() throws CipherException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mjtSpaceScanner2.saveMostReliableRocket(outputStream, LocalDate.of(2020, 4, 16), LocalDate.of(2020, 10, 20));
        InputStream encryptedInputStream = new ByteArrayInputStream(outputStream.toByteArray());

        outputStream.reset();

        rijndael.decrypt(encryptedInputStream, outputStream);
        String mostReliableRocket = outputStream.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals(mostReliableRocket, "Tsyklon-4M", "Tsyklon-4M has a reliability score of 1.5, meaning it should be the most reliable rocket(uses another set of missions from the other tests).");
    }

    @Test
    void testSaveMostReliableRocketWithInvalidKey() {
        StringReader stringReaderMissions3 = new StringReader(missionss);
        StringReader stringReaderRockets3 = new StringReader(rocketss);
        MJTSpaceScanner mjtSpaceScanner3 = new MJTSpaceScanner(stringReaderMissions3, stringReaderRockets3, new SecretKeySpec(new byte[8], "asdf"));
        OutputStream outputStream = new ByteArrayOutputStream();

        Assertions.assertThrows(CipherException.class, () -> mjtSpaceScanner3.saveMostReliableRocket(outputStream, LocalDate.of(2020, 12, 3), LocalDate.of(2020, 12, 4)), "Should throw cipher exception because asdf is an invalid algorithm");
    }
}