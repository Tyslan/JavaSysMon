package org.butler.ui.swing.monitor.system;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.butler.monitor.system.SystemPropertyCollector;
import org.butler.ui.swing.monitor.MonitorTabProvider;

public class SystemPanel extends JPanel {
	private static final long serialVersionUID = -3800607884672931099L;

	private MonitorTabProvider monitorTabProvider;

	private JPanel content;

	public SystemPanel(MonitorTabProvider monitorTabProvider) {
		this.monitorTabProvider = monitorTabProvider;
		SystemPropertyCollector spc = monitorTabProvider.getSystemPropertyCollector();

		content = new JPanel();
		content.setLayout(new GridLayout(0, 2));

		content.add(new JLabel(getLabel("system.name")));
		content.add(new JLabel(spc.getSystemName()));

		content.add(new JLabel(getLabel("system.distro.name")));
		content.add(new JLabel(spc.getOS()));

		content.add(new JLabel(getLabel("system.distro.version")));
		content.add(new JLabel(spc.getOSVersion()));

		content.add(new JLabel(getLabel("system.kernel.version")));
		content.add(new JLabel(spc.getKernelVersion()));

		add(content);
	}

	private String getLabel(String key) {
		return monitorTabProvider.getLabel(key);
	}

}
