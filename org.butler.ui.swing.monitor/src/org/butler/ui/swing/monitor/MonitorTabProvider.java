package org.butler.ui.swing.monitor;

import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.butler.ui.swing.core.api.TabProvider;
import org.osgi.service.component.annotations.Component;

@Component
public class MonitorTabProvider implements TabProvider {
	private MonitorPanel monitorPanel;
	private ResourceBundle resourceBundle;
	
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
		if(monitorPanel == null) {
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
	
	public String getLabel(String key) {
		return key;
	}
}
