package com.marksofgracecooldown;

import org.junit.Test;

import static org.junit.Assert.*;

public class CoursesTest {

    @Test
    public void testSeersConditionalTime() {
        // Default should be 44
        assertEquals(44, Courses.SEERS.getOptimalTime(key -> false));

        // When useSeersTeleport toggle is enabled, expect 38
        assertEquals(38, Courses.SEERS.getOptimalTime("useSeersTeleport"::equals));
    }

    @Test
    public void testGetCourseByRegionId() {
        // Test a few known region IDs
        assertEquals(Courses.CANIFIS, Courses.getCourse(13878));
        assertEquals(Courses.DRAYNOR, Courses.getCourse(12338));
        assertEquals(Courses.ARDOUGNE, Courses.getCourse(10547));
        assertEquals(Courses.GNOME, Courses.getCourse(9781));
        assertEquals(Courses.WEREWOLF, Courses.getCourse(14234));

        // Unknown region should return null
        assertNull(Courses.getCourse(99999));
    }

    @Test
    public void testContainsLastObstacle_singleObstacle() {
        // Canifis has one last obstacle: 14897
        assertTrue(Courses.CANIFIS.containsLastObstacle(14897));
        assertFalse(Courses.CANIFIS.containsLastObstacle(14898));
        assertFalse(Courses.CANIFIS.containsLastObstacle(0));
    }

    @Test
    public void testContainsLastObstacle_multipleObstacles_gnome() {
        // Gnome has 2 last obstacles: 23138, 23139
        assertTrue(Courses.GNOME.containsLastObstacle(23138));
        assertTrue(Courses.GNOME.containsLastObstacle(23139));
        assertFalse(Courses.GNOME.containsLastObstacle(23140));
    }

    @Test
    public void testContainsLastObstacle_multipleObstacles_werewolf() {
        // Werewolf has 3 last obstacles: 11644, 11645, 11646
        assertTrue(Courses.WEREWOLF.containsLastObstacle(11644));
        assertTrue(Courses.WEREWOLF.containsLastObstacle(11645));
        assertTrue(Courses.WEREWOLF.containsLastObstacle(11646));
        assertFalse(Courses.WEREWOLF.containsLastObstacle(11647));
    }

    @Test
    public void testOptimalTimeForNonConditionalCourse() {
        // Canifis has no conditionals, should always return 44
        assertEquals(44, Courses.CANIFIS.getOptimalTime(key -> false));
        assertEquals(44, Courses.CANIFIS.getOptimalTime(key -> true));
        assertEquals(44, Courses.CANIFIS.getOptimalTime("useSeersTeleport"::equals));
    }

    @Test
    public void testAllCoursesHaveValidData() {
        for (Courses course : Courses.values()) {
            // Each course should have at least one last obstacle
            assertTrue(course.name() + " should have at least one last obstacle",
                    course.getLastObstacleIds().length > 0);

            // Each course should have a positive optimal time
            assertTrue(course.name() + " should have positive optimal time",
                    course.getOptimalTimeSeconds() > 0);

            // Each course should have a valid region ID
            assertTrue(course.name() + " should have positive region ID",
                    course.getRegionId() > 0);

            // Each course should have at least one end world point
            assertTrue(course.name() + " should have at least one end world point",
                    course.getCourseEndWorldPoints().length > 0);

            // Course lookup by region should return the same course
            assertEquals(course.name() + " region lookup should match",
                    course, Courses.getCourse(course.getRegionId()));
        }
    }
}
