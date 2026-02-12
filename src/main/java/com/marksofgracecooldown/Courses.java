package com.marksofgracecooldown;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Enum representing Marks of Grace courses with their last obstacle IDs,
 * region IDs, and course end world points.
 */
enum Courses {
    // Rooftop courses
    DRAYNOR(new int[]{ObjectID.ROOFTOPS_DRAYNOR_CRATE}, 12338, 44, new WorldPoint(3103, 3261, 0)),
    AL_KHARID(new int[]{ObjectID.ROOFTOPS_KHARID_LEAPDOWN}, 13105, 65, new WorldPoint(3299, 3194, 0)),
    VARROCK(new int[]{ObjectID.ROOFTOPS_VARROCK_FINISH}, 12853, 66, new WorldPoint(3236, 3417, 0)),
    CANIFIS(new int[]{ObjectID.ROOFTOPS_CANIFIS_LEAPDOWN}, 13878, 44, new WorldPoint(3510, 3485, 0)),
    FALADOR(new int[]{ObjectID.ROOFTOPS_FALADOR_EDGE}, 12084, 59, new WorldPoint(3029, 3332, 0), new WorldPoint(3029, 3333, 0), new WorldPoint(3029, 3334, 0), new WorldPoint(3029, 3335, 0)),
    // SEERS: default optimal time 44, but if the useSeersTeleport toggle is enabled use 38
    SEERS(new int[]{ObjectID.ROOFTOPS_SEERS_LEAPDOWN}, 10806, 44, ImmutableMap.of("useSeersTeleport", 38), new WorldPoint(2704, 3464, 0)),
    POLLNIVNEACH(new int[]{ObjectID.ROOFTOPS_POLLNIVNEACH_LINE}, 13358, 61, new WorldPoint(3363, 2998, 0)),
    RELLEKA(new int[]{ObjectID.ROOFTOPS_RELLEKKA_DROPOFF}, 10553, 51, new WorldPoint(2653, 3676, 0)),
    ARDOUGNE(new int[]{ObjectID.ROOFTOPS_ARDY_JUMP_4}, 10547, 46, new WorldPoint(2668, 3297, 0)),
    // Other courses
    GNOME(new int[]{ObjectID.OBSTICAL_PIPE3_1, ObjectID.OBSTICAL_PIPE3_2}, 9781, 34, new WorldPoint(2484, 3437, 0), new WorldPoint(2487, 3437, 0)), // Gnome course has 2 last obstacles
    SHAYZIEN_BASIC(new int[]{ObjectID.SHAYZIEN_AGILITY_LOW_END_JUMP}, 6200, 53, new WorldPoint(1554, 3640, 0)),
    BARBARIAN(new int[]{ObjectID.CASTLECRUMBLY1}, 10039, 32, new WorldPoint(2543, 3553, 0)),
    SHAYZIEN_ADVANCED(new int[]{ObjectID.SHAYZIEN_AGILITY_UP_END_JUMP}, 5944, 49, new WorldPoint(1522, 3625, 0)),
    APE_ATOLL(new int[]{ObjectID._100_ILM_AGILITY_TREE_BASE}, 11050, 39, new WorldPoint(2770, 2747, 0)),
    WEREWOLF(new int[]{ObjectID.WEREWOLF_SLIDE_CENTER, ObjectID.WEREWOLF_SLIDE_SIDE, ObjectID.WEREWOLF_SLIDE_SIDE_MIRROR}, 14234, 38, new WorldPoint(3528, 9873, 0));


    private static final Map<Integer, Courses> coursesByRegion;
    @Getter
    private final int[] lastObstacleIds;
    @Getter
    private final int regionId;
    @Getter
    private final int optimalTimeSeconds;
    @Getter
    private final WorldPoint[] courseEndWorldPoints;

    // Map of conditional toggle key -> alternative optimal time
    private final Map<String, Integer> conditionalOptimalTimes;

    // Primary constructor with conditional map
    Courses(int[] lastObstacleIds, int regionId, int optimalTimeSeconds, Map<String, Integer> conditionalOptimalTimes, WorldPoint... courseEndWorldPoints) {
        // Defensive copy to avoid external mutation
        this.lastObstacleIds = lastObstacleIds == null ? new int[0] : lastObstacleIds.clone();
        this.optimalTimeSeconds = optimalTimeSeconds;
        this.regionId = regionId;
        this.courseEndWorldPoints = courseEndWorldPoints;
        this.conditionalOptimalTimes = conditionalOptimalTimes == null ? ImmutableMap.of() : conditionalOptimalTimes;
    }

    // Convenience constructor when there are no conditionals
    Courses(int[] lastObstacleIds, int regionId, int optimalTimeSeconds, WorldPoint... courseEndWorldPoints) {
        this(lastObstacleIds, regionId, optimalTimeSeconds, ImmutableMap.of(), courseEndWorldPoints);
    }

    static Courses getCourse(int regionId) {
        return coursesByRegion.get(regionId);
    }

    /**
     * Returns the effective optimal time, checking conditionalOptimalTimes in insertion order
     * and returning the first match where toggleEnabled.test(key) is true. If none match,
     * returns the default optimalTimeSeconds.
     */
    int getOptimalTime(Predicate<String> toggleEnabled) {
        for (Map.Entry<String, Integer> e : conditionalOptimalTimes.entrySet()) {
            if (toggleEnabled.test(e.getKey())) {
                return e.getValue();
            }
        }
        return optimalTimeSeconds;
    }

    /**
     * Returns true if the provided obstacle id matches any of this course's last-obstacle IDs.
     */
    boolean containsLastObstacle(int id) {
        for (int o : lastObstacleIds) {
            if (o == id) return true;
        }
        return false;
    }

    /**
     * Checks if this course is enabled in the config.
     */
    boolean isEnabled(MarksOfGraceCDConfig config) {
        switch (this) {
            case DRAYNOR:
                return config.enableDraynor();
            case AL_KHARID:
                return config.enableAlKharid();
            case VARROCK:
                return config.enableVarrock();
            case CANIFIS:
                return config.enableCanifis();
            case FALADOR:
                return config.enableFalador();
            case SEERS:
                return config.enableSeers();
            case POLLNIVNEACH:
                return config.enablePollnivneach();
            case RELLEKA:
                return config.enableRelleka();
            case ARDOUGNE:
                return config.enableArdougne();
            case GNOME:
                return config.enableGnome();
            case SHAYZIEN_BASIC:
                return config.enableShayzienBasic();
            case BARBARIAN:
                return config.enableBarbarian();
            case SHAYZIEN_ADVANCED:
                return config.enableShayzienAdvanced();
            case APE_ATOLL:
                return config.enableApeAtoll();
            case WEREWOLF:
                return config.enableWerewolf();
            default:
                return true;
        }
    }

    static {
        ImmutableMap.Builder<Integer, Courses> builder = new ImmutableMap.Builder<>();

        for (Courses course : values()) {
            builder.put(course.regionId, course);
        }

        coursesByRegion = builder.build();
    }
}
