package org.butler.ui.swing.monitor;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.butler.monitor.network.NetworkSpeedData;
import org.butler.monitor.network.NetworkSpeedDataListener;
import org.butler.monitor.system.SystemPropertyCollector;
import org.butler.ui.swing.core.api.TabProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class MonitorTabProvider implements TabProvider, NetworkSpeedDataListener {
	private MonitorPanel monitorPanel;
	private ResourceBundle resourceBundle;

	private SystemPropertyCollector systemPropertyCollector;

	@Override
	public String getName() {
		return getLabel("monitor.tab");
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public JPanel getContentPane() {
		if (monitorPanel == null) {
			monitorPanel = new MonitorPanel(this);
		}
		return monitorPanel;
	}

	@Override
	public String getToolTip() {
		return getLabel("monitor.tab");
	}

	@Override
	public Integer getPriority() {
		return 1;
	}

	@Override
	public void speedUpdated(Map<String, NetworkSpeedData> newData) {
		NetworkSpeedData data = newData.get("global");
		monitorPanel.setNetworkspeed(data);
	}

	public String getLabel(String key) {
		if (resourceBundle == null) {
			resourceBundle = ResourceBundle.getBundle("resources/labels");
		}
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException ignore) {
		}
		return key;
	}

	public SystemPropertyCollector getSystemPropertyCollector() {
		return systemPropertyCollector;
	}

	@Reference
	protected void bindSystemPropertyCollector(SystemPropertyCollector systemPropertyCollector) {
		this.systemPropertyCollector = systemPropertyCollector;
	}

	protected void unbindSystemPropertyCollector(SystemPropertyCollector systemPropertyCollector) {
		this.systemPropertyCollector = null;
	}
}
