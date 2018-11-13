package org.butler.monitor.network.linux;

import java.time.Duration;
import java.time.LocalDateTime;

import org.butler.monitor.network.NetworkSpeedData;
import org.butler.util.file.size.FileSizeConverter;
import org.butler.util.file.size.FileSizeUnit;

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

	public NetworkSpeedDataImpl(RawNetworkData oldData, RawNetworkData newData) {
		networkInterface = newData.getNetworkInterface();
		durationInMillis = getDuration(oldData.getTimestamp(), newData.getTimestamp());

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

	public String getNetworkInterface() {
		return networkInterface;
	}

	public long getDurationInMillis() {
		return durationInMillis;
	}

	public long getDownloadedBytes() {
		return downloadedBytes;
	}

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
		return convertSpeed(downloadSpeedBytesPerSeconds);
	}

	public String getUploadSpeed() {
		double uploadSpeedBytesPerSeconds = uploadedBytes / durationInMillis * 1000.0;
		return convertSpeed(uploadSpeedBytesPerSeconds);
	}

	public String convertSpeed(double size) {
		if (size < 1024) {
			return createSpeedString(size, FileSizeUnit.BYTE);
		}

		FileSizeUnit newSizeUnit = FileSizeUnit.KIBIBYTE;
		double newSize = FileSizeConverter.convert(size, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.MEBIBYTE;
		newSize = FileSizeConverter.convert(size, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.GIBIBYTE;
		newSize = FileSizeConverter.convert(size, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.TEBIBYTE;
		newSize = FileSizeConverter.convert(size, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		return size + FileSizeUnit.BYTE.getAbbreviation();
	}

	private String createSpeedString(double newSize, FileSizeUnit newSizeUnit) {
		return String.format("%.2f %s/s", newSize, newSizeUnit.getAbbreviation());
	}
}
