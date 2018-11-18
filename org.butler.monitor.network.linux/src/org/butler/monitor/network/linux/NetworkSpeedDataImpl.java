package org.butler.monitor.network.linux;

import java.time.Duration;
import java.time.LocalDateTime;

import org.butler.monitor.network.NetworkSpeedData;
import org.butler.util.file.size.FileSizeFormatter;

public class NetworkSpeedDataImpl implements NetworkSpeedData {
	private String networkInterface;
	private long durationInMillis;
	private long downloadedBytes;
	private long uploadedBytes;

	public NetworkSpeedDataImpl(String networkInterface, long durationInMillis, long downloadedBytes,
			long uploadedBytes) {
		this.networkInterface = networkInterface;
		this.durationInMillis = durationInMillis;
		this.downloadedBytes = downloadedBytes;
		this.uploadedBytes = uploadedBytes;
	}

	public NetworkSpeedDataImpl(RawNetworkData oldData, RawNetworkData newData) throws IllegalArgumentException {
		networkInterface = newData.getNetworkInterface();
		durationInMillis = getDuration(oldData.getTimestamp(), newData.getTimestamp());
		if (durationInMillis == 0) {
			throw new IllegalArgumentException("Duration is 0");
		}

		downloadedBytes = calculateDownloadSpeed(oldData.getCulminatedSessionDownload(),
				newData.getCulminatedSessionDownload());
		uploadedBytes = calculateUploadSpeed(oldData.getCulminatedSessionUpload(),
				newData.getCulminatedSessionUpload());
	}

	private long getDuration(LocalDateTime start, LocalDateTime end) {
		Duration duration = Duration.between(start, end);
		return Math.abs(duration.toMillis());
	}

	private long calculateDownloadSpeed(Long start, Long end) {
		return end - start;
	}

	private long calculateUploadSpeed(Long start, Long end) {
		return end - start;
	}

	@Override
	public String getNetworkInterface() {
		return networkInterface;
	}

	@Override
	public long getDurationInMillis() {
		return durationInMillis;
	}

	@Override
	public long getDownloadedBytes() {
		return downloadedBytes;
	}

	@Override
	public long getUploadedBytes() {
		return uploadedBytes;
	}

	@Override
	public String toString() {
		return "NetworkSpeedData [" + LocalDateTime.now() + ": Interface=" + networkInterface + ", Download="
				+ getDownloadSpeed() + ", Upload=" + getUploadSpeed() + "]";
	}

	public String getDownloadSpeed() {
		double downloadSpeedBytesPerSeconds = downloadedBytes / durationInMillis * 1000.0;
		return FileSizeFormatter.getFormattedBinary(downloadSpeedBytesPerSeconds) + "/s";
	}

	public String getUploadSpeed() {
		double uploadSpeedBytesPerSeconds = uploadedBytes / durationInMillis * 1000.0;
		return FileSizeFormatter.getFormattedBinary(uploadSpeedBytesPerSeconds) + "/s";
	}
}
