package com.marksofgracecooldown.ntp;

/**
 * Represents the current state of NTP time synchronization.
 */
public enum NtpSyncState
{
	/**
	 * NTP sync has not been attempted yet
	 */
	NOT_SYNCED("Not synced"),
	/**
	 * NTP sync is currently in progress
	 */
	SYNCING("Syncing..."),
	/**
	 * NTP sync completed successfully
	 */
	SYNCED("Synced"),
	/**
	 * NTP sync failed after all retry attempts
	 */
	FAILED("Failed");

	private final String displayName;

	NtpSyncState(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
