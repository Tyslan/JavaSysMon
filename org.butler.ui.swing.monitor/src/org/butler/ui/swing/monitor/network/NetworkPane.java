package org.butler.ui.swing.monitor.network;

import javax.swing.JPanel;

import org.butler.ui.swing.monitor.MonitorTabProvider;

public class NetworkPane extends JPanel {
	private MonitorTabProvider provider;

	public NetworkPane(MonitorTabProvider provider) {
		this.provider = provider;
	}

	private String getLabel(String key) {
		return provider.getLabel(key);
	}
}
