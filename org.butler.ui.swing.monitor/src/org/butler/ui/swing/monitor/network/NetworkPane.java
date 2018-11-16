package org.butler.ui.swing.monitor.network;

import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.JPanel;

import org.butler.monitor.network.NetworkSpeedData;
import org.butler.ui.swing.monitor.MonitorTabProvider;
import org.butler.util.file.size.FileSizeFormatter;

public class NetworkPane extends JPanel {
	private static final long serialVersionUID = 4471565144046975101L;

	private MonitorTabProvider provider;
	private JPanel content;
	private Label downspeed;
	private Label upspeed;

	public NetworkPane(MonitorTabProvider provider) {
		this.provider = provider;
		createNetworkPanel();
	}

	private void createNetworkPanel() {
		content = new JPanel();
		content.setLayout(new GridLayout(0, 2));

		content.add(new Label(getLabel("network.speed.down")));
		content.add(new Label(getLabel("network.speed.down")));

		downspeed = new Label(getLabel("network.speed.initializing"));
		upspeed = new Label(getLabel("network.speed.initializing"));
		content.add(downspeed);
		content.add(upspeed);

		add(content);
	}

	public void setSpeedData(NetworkSpeedData data) {
		setDownloadSpeed(data);
		setUploadSpeed(data);
	}

	private void setDownloadSpeed(NetworkSpeedData data) {
		String speed = getSpeedString(data.getDownloadedBytes(), data.getDurationInMillis());
		downspeed.setText(speed);
	}

	private void setUploadSpeed(NetworkSpeedData data) {
		String speed = getSpeedString(data.getUploadedBytes(), data.getDurationInMillis());
		upspeed.setText(speed);
	}

	private String getSpeedString(long bytes, long durationInMillis) {
		double speedBytesPerSeconds = bytes / durationInMillis * 1000.0;
		return FileSizeFormatter.getFormattedBinary(speedBytesPerSeconds) + "/s";
	}

	private String getLabel(String key) {
		return provider.getLabel(key);
	}
}
