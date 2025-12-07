package com.marksofgracecooldown;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoursesTest {

    @Test
    public void testSeersConditionalTime() {
        // Default should be 44
        assertEquals(44, Courses.SEERS.getOptimalTime(key -> false));

        // When useSeersTeleport toggle is enabled, expect 38
        assertEquals(38, Courses.SEERS.getOptimalTime("useSeersTeleport"::equals));
    }
}
