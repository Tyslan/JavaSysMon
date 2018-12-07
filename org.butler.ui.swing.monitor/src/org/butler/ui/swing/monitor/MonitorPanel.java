package org.butler.ui.swing.monitor;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.butler.monitor.network.NetworkSpeedData;
import org.butler.ui.swing.monitor.network.NetworkPane;
import org.butler.ui.swing.monitor.system.SystemPanel;

public class MonitorPanel extends JPanel {
	private static final long serialVersionUID = 2784436787404916858L;

	private MonitorTabProvider provider;

	private JPanel contentPane;
	private NetworkPane networkPane;
	private SystemPanel systemPanel;

	public MonitorPanel(MonitorTabProvider monitorTabProvider) {
		this.provider = monitorTabProvider;
		this.setLayout(new BorderLayout());
		createTitle();
		createContentPane();
	}

	private void createTitle() {
		JPanel titlePane = new JPanel();
		titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.X_AXIS));

		JLabel title = new JLabel(getLabel("monitor.system"));
		title.setFont(new Font("Sans Serif", Font.BOLD, 18));

		titlePane.add(Box.createHorizontalGlue());
		titlePane.add(title);
		titlePane.add(Box.createHorizontalGlue());
		add(titlePane, BorderLayout.NORTH);
	}

	private void createContentPane() {
		contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		networkPane = new NetworkPane(provider);
		systemPanel = new SystemPanel(provider);

		contentPane.add(systemPanel);
		contentPane.add(networkPane);
		add(contentPane);

	}

	private String getLabel(String key) {
		return provider.getLabel(key);
	}

	public void setNetworkspeed(NetworkSpeedData data) {
		networkPane.setSpeedData(data);
	}
}
