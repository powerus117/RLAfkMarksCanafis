package com.marksofgracecooldown.ntp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * NTP client for synchronizing time with an NTP server.
 * Corrects for users whose system clocks are out of sync,
 * ensuring accurate cooldown calculations.
 */
@Slf4j
public class NtpClient
{
	private static final String[] NTP_HOSTS = {"pool.ntp.org", "time.google.com", "time.cloudflare.com"};
	private static final int NTP_PORT = 123;
	private static final int SOCKET_TIMEOUT_MS = 5000;
	private static final int SYNC_ATTEMPTS = 3;
	private static final int RETRY_DELAY_MS = 2000;

	private static final long MILLIS_PER_SECOND = 1000L;
	private static final long UNIX_OFFSET_SECONDS = 2208988800L;
	private static final long NTP_CYCLE_MILLIS = (Integer.toUnsignedLong(0xffffffff) + 1) * MILLIS_PER_SECOND;
	private static final AtomicReference<Thread> syncThread = new AtomicReference<>(null);
	@Getter
	private static volatile long syncedOffsetMillis = 0;
	@Getter
	private static volatile NtpSyncState syncState = NtpSyncState.NOT_SYNCED;

	/**
	 * Starts an asynchronous NTP time sync if not already syncing.
	 */
	public static void startSync()
	{
		if (syncState == NtpSyncState.SYNCING)
		{
			return;
		}

		Thread newThread = new Thread(NtpClient::syncTime, "NtpClient-Sync");
		newThread.setDaemon(true);

		if (syncThread.compareAndSet(null, newThread))
		{
			newThread.start();
		}
	}

	/**
	 * Resets the sync state to allow a fresh sync attempt.
	 */
	public static void resetSync()
	{
		if (syncState != NtpSyncState.SYNCING)
		{
			syncState = NtpSyncState.NOT_SYNCED;
			syncedOffsetMillis = 0;
		}
	}

	private static void syncTime()
	{
		syncState = NtpSyncState.SYNCING;

		try
		{
			for (String host : NTP_HOSTS)
			{
				if (trySyncWithHost(host))
				{
					return;
				}
			}
			log.warn("NTP sync failed after trying all servers");
			syncState = NtpSyncState.FAILED;
		}
		finally
		{
			syncThread.set(null);
		}
	}

	private static boolean trySyncWithHost(String host)
	{
		for (int attempt = 0; attempt < SYNC_ATTEMPTS; attempt++)
		{
			try
			{
				syncedOffsetMillis = queryNtpServer(host);
				syncState = NtpSyncState.SYNCED;
				log.debug("NTP sync successful with {}: offset = {}ms", host, syncedOffsetMillis);
				return true;
			}
			catch (Exception e)
			{
				log.debug("NTP sync attempt {} with {} failed: {}", attempt + 1, host, e.getMessage());
				sleepBeforeRetry(attempt);
			}
		}
		return false;
	}

	private static void sleepBeforeRetry(int attempt)
	{
		if (attempt < SYNC_ATTEMPTS - 1)
		{
			try
			{
				Thread.sleep(RETRY_DELAY_MS);
			}
			catch (InterruptedException ie)
			{
				Thread.currentThread().interrupt();
			}
		}
	}

	private static long queryNtpServer(String host) throws Exception
	{
		try (DatagramSocket socket = new DatagramSocket())
		{
			socket.setSoTimeout(SOCKET_TIMEOUT_MS);

			byte[] ntpData = new byte[48];
			ntpData[0] = 0x1B; // NTP client request

			DatagramPacket request = new DatagramPacket(ntpData, ntpData.length,
				InetAddress.getByName(host), NTP_PORT);

			long sendTime = System.currentTimeMillis();
			socket.send(request);
			socket.receive(new DatagramPacket(ntpData, ntpData.length));
			long receiveTime = System.currentTimeMillis();

			long serverNtpMillis = parseNtpTimestamp(ntpData, 40);
			long localNtpMillis = Instant.now().plusSeconds(UNIX_OFFSET_SECONDS).toEpochMilli();

			long offset = calculateOffset(serverNtpMillis, localNtpMillis);
			long networkDelay = (receiveTime - sendTime) / 2;

			return offset - networkDelay;
		}
	}

	// Package-private for testing
	static long parseNtpTimestamp(byte[] data, int offset)
	{
		long seconds = readUnsignedInt(data, offset);
		long fraction = readUnsignedInt(data, offset + 4);
		return seconds * MILLIS_PER_SECOND + (fraction * MILLIS_PER_SECOND >> 32);
	}

	// Package-private for testing
	static long readUnsignedInt(byte[] data, int offset)
	{
		return Byte.toUnsignedLong(data[offset]) << 24 |
			Byte.toUnsignedLong(data[offset + 1]) << 16 |
			Byte.toUnsignedLong(data[offset + 2]) << 8 |
			Byte.toUnsignedLong(data[offset + 3]);
	}

	private static long calculateOffset(long serverMillis, long localMillis)
	{
		long difference = serverMillis - localMillis;
		// Handle NTP timestamp rollover (year 2036)
		if (Math.abs(difference) > NTP_CYCLE_MILLIS / 2)
		{
			return difference < 0 ? difference + NTP_CYCLE_MILLIS : difference - NTP_CYCLE_MILLIS;
		}
		return difference;
	}
}

