package com.marksofgracecooldown;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.Map;

enum Courses {
    GNOME(23139, 9781, new WorldPoint(2484, 3437, 0), new WorldPoint(2487, 3437, 0)), // Gnome course has 2 last obstacles but we just use one
    SHAYZIEN_BASIC(42216, 6200, new WorldPoint(1554, 3640, 0)),
    DRAYNOR(11632, 12338, new WorldPoint(3103, 3261, 0)),
    AL_KHARID(14399, 13105, new WorldPoint(3299, 3194, 0)),
    VARROCK(14841, 12853, new WorldPoint(3236, 3417, 0)),
    BARBARIAN(1948, 10039, new WorldPoint(2543, 3553, 0)),
    CANIFIS(14897, 13878, new WorldPoint(3510, 3485, 0)),
    APE_ATOLL(16062, 11050, new WorldPoint(2770, 2747, 0)),
    SHAYZIEN_ADVANCED(42221, 5944, new WorldPoint(1522, 3625, 0)),
    FALADOR(14925, 12084, new WorldPoint(3029, 3332, 0), new WorldPoint(3029, 3333, 0), new WorldPoint(3029, 3334, 0), new WorldPoint(3029, 3335, 0)),
    WEREWOLF(11646, 14234, new WorldPoint(3528, 9873, 0)),
    SEERS(14931, 10806, new WorldPoint(2704, 3464, 0)),
    POLLNIVNEACH(14945, 13358, new WorldPoint(3363, 2998, 0)),
    RELLEKA(14994, 10553, new WorldPoint(2653, 3676, 0)),
    ARDOUGNE(15612, 10547, new WorldPoint(2668, 3297, 0));

    private static final Map<Integer, Courses> coursesByRegion;
    @Getter
    private final int lastObstacleId;
    @Getter
    private final int regionId;
    @Getter
    private final WorldPoint[] courseEndWorldPoints;

    Courses(int lastObstacleId, int regionId, WorldPoint... courseEndWorldPoints) {
        this.lastObstacleId = lastObstacleId;
        this.regionId = regionId;
        this.courseEndWorldPoints = courseEndWorldPoints;
    }

    static Courses getCourse(int regionId) {
        return coursesByRegion.get(regionId);
    }

    static {
        ImmutableMap.Builder<Integer, Courses> builder = new ImmutableMap.Builder<>();

        for (Courses course : values()) {
            builder.put(course.regionId, course);
        }

        coursesByRegion = builder.build();
    }
}
