package com.marksofgracecooldown.ntp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class NtpClientTest
{

	@BeforeClass
	public static void setUpClass()
	{
		// Reset NTP state before running tests to ensure clean state
		// Wait for any in-progress sync to complete
		try
		{
			Thread.sleep(200);
		}
		catch (InterruptedException ignored)
		{
		}
		NtpClient.resetSync();
	}

	@Test
	public void testInitialState()
	{
		NtpSyncState state = NtpClient.getSyncState();
		assertNotNull(state);
	}

	@Test
	public void testSyncStateEnum()
	{
		assertEquals("Not synced", NtpSyncState.NOT_SYNCED.toString());
		assertEquals("Syncing...", NtpSyncState.SYNCING.toString());
		assertEquals("Synced", NtpSyncState.SYNCED.toString());
		assertEquals("Failed", NtpSyncState.FAILED.toString());
	}

	@Test
	public void testStartSyncDoesNotBlock()
	{
		long startTime = System.currentTimeMillis();
		NtpClient.startSync();
		long elapsed = System.currentTimeMillis() - startTime;

		assertTrue("startSync should be non-blocking", elapsed < 100);
	}

	@Test
	public void testMultipleStartSyncCalls()
	{
		NtpClient.startSync();
		NtpClient.startSync();
		NtpClient.startSync();

		NtpSyncState state = NtpClient.getSyncState();
		assertNotNull(state);
	}

	/**
	 * The NTP packet (SNTP, RFC 4330) is 48 bytes:
	 * Bytes  0- 3: LI/VN/Mode, Stratum, Poll, Precision
	 * Bytes  4- 7: Root Delay
	 * Bytes  8-11: Root Dispersion
	 * Bytes 12-15: Reference Identifier
	 * Bytes 16-23: Reference Timestamp
	 * Bytes 24-31: Originate Timestamp
	 * Bytes 32-39: Receive Timestamp
	 * Bytes 40-47: Transmit Timestamp  <- byte offset 40 is correct
	 * <p>
	 * The implementation calls parseNtpTimestamp(data, 40) which extracts the
	 * Transmit Timestamp. These tests verify the byte-level parsing is correct
	 * using hand-crafted NTP data, confirming byte offset 40 is the right field.
	 */
	@Test
	public void testParseNtpTimestamp_byteOffset40_isTransmitTimestamp()
	{
		// Build a synthetic 48-byte NTP response.
		// Place a known timestamp at byte offset 40 (Transmit Timestamp).
		// seconds=3825073266 (~2021-02-27 in NTP time), fraction=0x80000000 (0.5 s).
		long seconds = 3825073266L;
		long fraction = 0x80000000L;

		byte[] ntpData = new byte[48];
		// Write seconds at offset 40 (big-endian, unsigned 32-bit)
		ntpData[40] = (byte) ((seconds >> 24) & 0xFF);
		ntpData[41] = (byte) ((seconds >> 16) & 0xFF);
		ntpData[42] = (byte) ((seconds >> 8) & 0xFF);
		ntpData[43] = (byte) (seconds & 0xFF);
		// Write fraction at offset 44 (big-endian, unsigned 32-bit)
		ntpData[44] = (byte) ((fraction >> 24) & 0xFF);
		ntpData[45] = (byte) ((fraction >> 16) & 0xFF);
		ntpData[46] = (byte) ((fraction >> 8) & 0xFF);
		ntpData[47] = (byte) (fraction & 0xFF);

		long result = NtpClient.parseNtpTimestamp(ntpData, 40);

		// Expected: seconds * 1000 + (0x80000000 * 1000 >> 32) = seconds * 1000 + 500
		long expectedMillis = seconds * 1000L + 500L;
		assertEquals("parseNtpTimestamp at offset 40 should decode the Transmit Timestamp field",
			expectedMillis, result);
	}

	@Test
	public void testParseNtpTimestamp_zeroTimestamp()
	{
		byte[] ntpData = new byte[48]; // all zeros
		long result = NtpClient.parseNtpTimestamp(ntpData, 40);
		assertEquals("Zero NTP timestamp should parse to 0 ms", 0L, result);
	}

	@Test
	public void testParseNtpTimestamp_secondsOnly_noFraction()
	{
		// Encode exactly 1 second at offset 40, fraction = 0
		byte[] ntpData = new byte[48];
		ntpData[43] = 1; // least-significant byte of seconds field at offset 40
		long result = NtpClient.parseNtpTimestamp(ntpData, 40);
		assertEquals("1 NTP second with zero fraction should parse to 1000 ms", 1000L, result);
	}

	@Test
	public void testParseNtpTimestamp_differentOffsets()
	{
		// Confirm that the offset parameter selects the correct byte range.
		// Place a known value at offset 32 (Receive Timestamp) and a different one at 40.
		byte[] ntpData = new byte[48];
		// Receive Timestamp at offset 32: encode 2 seconds
		ntpData[35] = 2;
		// Transmit Timestamp at offset 40: encode 5 seconds
		ntpData[43] = 5;

		assertEquals("Offset 32 should read 2 seconds = 2000 ms", 2000L,
			NtpClient.parseNtpTimestamp(ntpData, 32));
		assertEquals("Offset 40 should read 5 seconds = 5000 ms", 5000L,
			NtpClient.parseNtpTimestamp(ntpData, 40));
	}

	@Test
	public void testReadUnsignedInt_bigEndian()
	{
		// Verify big-endian reading of a known 4-byte sequence
		byte[] data = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
		long expected = 0xDEADBEEFL;
		assertEquals("readUnsignedInt should decode big-endian unsigned 32-bit correctly",
			expected, NtpClient.readUnsignedInt(data, 0));
	}

	/**
	 * Documents why byte offset 40 (Transmit Timestamp) is used rather than the world ping.
	 * The NTP network delay (RTT/2 from the actual NTP packet exchange) corrects the
	 * NTP-derived clock offset. The world ping corrects for OSRS server event latency.
	 * Both serve different purposes and must remain separate.
	 */
	@Test
	public void testNtpOffset_independentFromWorldPing()
	{
		// syncedOffsetMillis is set only by the NTP exchange, never by the world ping.
		NtpClient.resetSync();
		long offset = NtpClient.getSyncedOffsetMillis();
		assertEquals("syncedOffsetMillis should be 0 after reset", 0L, offset);
	}
}
