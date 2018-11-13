package org.butler.monitor.network;

public interface NetworkSpeedData {
	/**
	 *
	 * @return name of the network interface
	 */
	public String getNetworkInterface();

	/**
	 *
	 * @return duration of the sample interval
	 */
	public long getDurationInMillis();

	/**
	 *
	 * @return total downloaded bytes during the sample interval
	 */
	public long getDownloadedBytes();

	/**
	 *
	 * @return total uploaded bytes during the sample interval
	 */
	public long getUploadedBytes();
}
