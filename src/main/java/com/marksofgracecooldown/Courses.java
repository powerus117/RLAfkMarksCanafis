package com.marksofgracecooldown;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Enum representing Marks of Grace courses with their last obstacle IDs,
 * region IDs, and course end world points.
 */
enum Courses {
    // Rooftop courses
    DRAYNOR(11632, 12338, 44, new WorldPoint(3103, 3261, 0)),
    AL_KHARID(14399, 13105, 65, new WorldPoint(3299, 3194, 0)),
    VARROCK(14841, 12853, 66, new WorldPoint(3236, 3417, 0)),
    CANIFIS(14897, 13878, 44, new WorldPoint(3510, 3485, 0)),
    FALADOR(14925, 12084, 59, new WorldPoint(3029, 3332, 0), new WorldPoint(3029, 3333, 0), new WorldPoint(3029, 3334, 0), new WorldPoint(3029, 3335, 0)),
    // SEERS: default optimal time 44, but if the useSeersTeleport toggle is enabled use 38
    SEERS(14931, 10806, 44, ImmutableMap.of("useSeersTeleport", 38), new WorldPoint(2704, 3464, 0)),
    POLLNIVNEACH(14945, 13358, 61, new WorldPoint(3363, 2998, 0)),
    RELLEKA(14994, 10553, 51, new WorldPoint(2653, 3676, 0)),
    ARDOUGNE(15612, 10547, 46, new WorldPoint(2668, 3297, 0)),
    // Other courses
    GNOME(23139, 9781, 34, new WorldPoint(2484, 3437, 0), new WorldPoint(2487, 3437, 0)), // Gnome course has 2 last obstacles but we just use one TODO: figure out a way to handle multiple last obstacles better
    SHAYZIEN_BASIC(42216, 6200, 53, new WorldPoint(1554, 3640, 0)),
    BARBARIAN(1948, 10039, 32, new WorldPoint(2543, 3553, 0)),
    SHAYZIEN_ADVANCED(42221, 5944, 49, new WorldPoint(1522, 3625, 0)),
    APE_ATOLL(16062, 11050, 39, new WorldPoint(2770, 2747, 0)),
    WEREWOLF(11646, 14234, 38, new WorldPoint(3528, 9873, 0));


    private static final Map<Integer, Courses> coursesByRegion;
    @Getter
    private final int lastObstacleId;
    @Getter
    private final int regionId;
    @Getter
    private final int optimalTimeSeconds;
    @Getter
    private final WorldPoint[] courseEndWorldPoints;

    // Map of conditional toggle key -> alternative optimal time
    private final Map<String, Integer> conditionalOptimalTimes;

    // Primary constructor with conditional map
    Courses(int lastObstacleId, int regionId, int optimalTimeSeconds, Map<String, Integer> conditionalOptimalTimes, WorldPoint... courseEndWorldPoints) {
        this.lastObstacleId = lastObstacleId;
        this.optimalTimeSeconds = optimalTimeSeconds;
        this.regionId = regionId;
        this.courseEndWorldPoints = courseEndWorldPoints;
        this.conditionalOptimalTimes = conditionalOptimalTimes == null ? ImmutableMap.of() : conditionalOptimalTimes;
    }

    // Convenience constructor when there are no conditionals
    Courses(int lastObstacleId, int regionId, int optimalTimeSeconds, WorldPoint... courseEndWorldPoints) {
        this(lastObstacleId, regionId, optimalTimeSeconds, ImmutableMap.of(), courseEndWorldPoints);
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

    static {
        ImmutableMap.Builder<Integer, Courses> builder = new ImmutableMap.Builder<>();

        for (Courses course : values()) {
            builder.put(course.regionId, course);
        }

        coursesByRegion = builder.build();
    }
}
