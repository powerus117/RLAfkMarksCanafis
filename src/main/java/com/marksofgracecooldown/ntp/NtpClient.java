package com.marksofgracecooldown.ntp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NtpClient
{
	private static final Logger log = LoggerFactory.getLogger(NtpClient.class);
	private static final String NTP_HOST_ADDRESS = "pool.ntp.org";
	private static final int SYNC_ATTEMPTS = 5;

	private static final long MILLISECONDS_PER_SECOND = 1000L;
	private static final long UNIX_OFFSET_SECONDS = 2208988800L;
	private static final long CYCLE_TIME_SECONDS = Integer.toUnsignedLong(0xffffffff) + 1; // 2^32
	private static final long CYCLE_TIME_MILLIS = CYCLE_TIME_SECONDS * MILLISECONDS_PER_SECOND;
	private static final long HALF_CYCLE_TIME_MILLIS = CYCLE_TIME_MILLIS / 2;

	public static long SyncedOffsetMillis = 0;
	public static NtpSyncState SyncState = NtpSyncState.NOT_SYNCED;

	public static void startSync()
	{
		if (SyncState == NtpSyncState.SYNCING)
			return;

		new Thread(NtpClient::syncTime).start();
	}

	private static void syncTime()
	{
		SyncState = NtpSyncState.SYNCING;

		for (int i = 0; i < SYNC_ATTEMPTS; i++)
		{
			try (DatagramSocket socket = new DatagramSocket())
			{
				socket.setSoTimeout(5000);
				InetAddress address = InetAddress.getByName(NTP_HOST_ADDRESS);

				byte[] ntpData = new byte[48];
				ntpData[0] = 0x1B; // LeapIndicator = 0 (no warning), VersionNum = 3 (IPv4 only), Mode = 3 (Client Mode)

				DatagramPacket packet = new DatagramPacket(ntpData, ntpData.length, address, 123);

				// Use stopwatch to estimate ping
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();

				socket.send(packet);
				packet = new DatagramPacket(ntpData, ntpData.length);
				socket.receive(packet);
				socket.close();

				stopWatch.stop();
				Instant now = Instant.now();

				// Reading 32 bit ntp parts (seconds since 1900 and fraction of a second)
				long intPart =
						Byte.toUnsignedLong(ntpData[40]) << 24 |
						Byte.toUnsignedLong(ntpData[41]) << 16 |
						Byte.toUnsignedLong(ntpData[42]) << 8 |
						Byte.toUnsignedLong(ntpData[43]);
				long fractPart =
						Byte.toUnsignedLong(ntpData[44]) << 24 |
						Byte.toUnsignedLong(ntpData[45]) << 16 |
						Byte.toUnsignedLong(ntpData[46]) << 8 |
						Byte.toUnsignedLong(ntpData[47]);

				long localNtpMillis = now.plusSeconds(UNIX_OFFSET_SECONDS).toEpochMilli();
				long serverNtpMillis = intPart * MILLISECONDS_PER_SECOND + (fractPart * MILLISECONDS_PER_SECOND >> 32);

				long difference = serverNtpMillis - localNtpMillis;
				long offset = difference;

				// Adjust for 2036 ntp roll over problem, if people are somehow still using it then
				if (Math.abs(difference) > HALF_CYCLE_TIME_MILLIS)
					offset = difference < 0 ? difference + CYCLE_TIME_MILLIS : difference - CYCLE_TIME_MILLIS;

				long pingOffset = (stopWatch.getTime(TimeUnit.MILLISECONDS) / 2);

				SyncedOffsetMillis = offset - pingOffset;
				SyncState = NtpSyncState.SYNCED;
				break; // Break when we had a successful sync, only retry on fails
			}
			catch(Exception e)
			{
				log.info("Failed to sync ntp time with exception: {}", e.getMessage());

				if (i >= SYNC_ATTEMPTS - 1)
				{
					SyncState = NtpSyncState.FAILED;
					break;
				}

				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException ignored)
				{
				}
			}
		}
	}
}
