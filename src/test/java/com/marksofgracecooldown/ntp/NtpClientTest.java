package com.marksofgracecooldown.ntp;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class NtpClientTest {

    @BeforeClass
    public static void setUpClass() {
        // Reset NTP state before running tests to ensure clean state
        // Wait for any in-progress sync to complete
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        NtpClient.resetSync();
    }

    @Test
    public void testInitialState() {
        NtpSyncState state = NtpClient.getSyncState();
        assertNotNull(state);
    }

    @Test
    public void testSyncStateEnum() {
        assertEquals("Not synced", NtpSyncState.NOT_SYNCED.toString());
        assertEquals("Syncing...", NtpSyncState.SYNCING.toString());
        assertEquals("Synced", NtpSyncState.SYNCED.toString());
        assertEquals("Failed", NtpSyncState.FAILED.toString());
    }

    @Test
    public void testStartSyncDoesNotBlock() {
        long startTime = System.currentTimeMillis();
        NtpClient.startSync();
        long elapsed = System.currentTimeMillis() - startTime;

        assertTrue("startSync should be non-blocking", elapsed < 100);
    }

    @Test
    public void testMultipleStartSyncCalls() {
        NtpClient.startSync();
        NtpClient.startSync();
        NtpClient.startSync();

        NtpSyncState state = NtpClient.getSyncState();
        assertNotNull(state);
    }
}

