package org.butler.ui.swing.monitor.network;

import java.awt.GridLayout;
import java.awt.Label;

import javax.swing.Box;
import javax.swing.BoxLayout;
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

	private NetworkSpeedPane downSpeedPane;
	private NetworkSpeedPane upSpeedPane;

	public NetworkPane(MonitorTabProvider provider) {
		this.provider = provider;
		createNetworkPanel();
	}

	private void createNetworkPanel() {
		content = new JPanel();
		content.setLayout(new GridLayout(0, 2));

		content.add(createDownSpeedLabelPanel());
		content.add(createUpSpeedLabelPanel());

		downSpeedPane = new NetworkSpeedPane("Down", 60);
		upSpeedPane = new NetworkSpeedPane("Up", 60);
		content.add(downSpeedPane);
		content.add(upSpeedPane);

		add(content);
	}

	private JPanel createDownSpeedLabelPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new Label(getLabel("network.speed.down")));
		panel.add(Box.createHorizontalGlue());
		downspeed = new Label(getLabel("network.speed.initializing"));
		panel.add(downspeed);
		return panel;
	}

	private JPanel createUpSpeedLabelPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new Label(getLabel("network.speed.up")));
		panel.add(Box.createHorizontalGlue());
		upspeed = new Label(getLabel("network.speed.initializing"));
		panel.add(upspeed);
		return panel;
	}

	public void setSpeedData(NetworkSpeedData data) {
		double downSpeed = getBytesPerSecond(data.getDownloadedBytes(), data.getDurationInMillis());
		double upSpeed = getBytesPerSecond(data.getUploadedBytes(), data.getDurationInMillis());

		setDownloadSpeed(downSpeed);
		setUploadSpeed(upSpeed);

		downSpeedPane.setSpeedData(downSpeed);
		upSpeedPane.setSpeedData(upSpeed);

		revalidate();
		repaint();
	}

	private void setDownloadSpeed(double bps) {
		String speed = getSpeedString(bps);
		downspeed.setText(speed);
	}

	private void setUploadSpeed(double bps) {
		String speed = getSpeedString(bps);
		upspeed.setText(speed);
	}

	private String getSpeedString(double bps) {
		return FileSizeFormatter.getFormattedBinary(bps) + "/s";
	}

	private double getBytesPerSecond(long bytes, long durationInMillis) {
		return bytes / durationInMillis * 1000.0;
	}

	private String getLabel(String key) {
		return provider.getLabel(key);
	}
}
