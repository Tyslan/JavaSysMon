package org.butler.monitor.network.linux;

import java.time.LocalDateTime;

public class RawNetworkData {
	private LocalDateTime timestamp;
	private String networkInterface;
	private Long culminatedSessionDownload;
	private Long culminatedSessionUpload;

	// takes line from /proc/net/dev
	public RawNetworkData(LocalDateTime timestamp, String dataLine) {
		dataLine = dataLine.trim();
		String[] splitted = dataLine.split("\\s+");
		this.networkInterface = getNetworkInterface(splitted);
		this.culminatedSessionDownload = getTotalDownload(splitted);
		this.culminatedSessionUpload = getTotalUpload(splitted);
		this.timestamp = timestamp;
	}

	private String getNetworkInterface(String[] splitted) {
		return splitted[0].replace(":", "");
	}

	private Long getTotalDownload(String[] splitted) {
		return Long.parseLong(splitted[1]);
	}

	private Long getTotalUpload(String[] splitted) {
		return Long.parseLong(splitted[9]);
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public Long getCulminatedSessionDownload() {
		return culminatedSessionDownload;
	}

	public Long getCulminatedSessionUpload() {
		return culminatedSessionUpload;
	}
}
