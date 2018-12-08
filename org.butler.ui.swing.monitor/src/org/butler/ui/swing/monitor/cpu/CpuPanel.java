package org.butler.ui.swing.monitor.cpu;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.butler.monitor.cpu.CpuInfo;
import org.butler.monitor.cpu.CpuInfoProvider;
import org.butler.ui.swing.monitor.MonitorTabProvider;

public class CpuPanel extends JPanel {
	private static final long serialVersionUID = -3081366282900071611L;
	private static final String TEMPERATURE_PREFIX = "Package id ";
	private static final String USAGE_PREFIX = "cpu";

	private MonitorTabProvider monitorTabProvider;
	private Map<Integer, CpuInfo> cpuInfos;

	private Map<Integer, JLabel> cpuTempLabelMapping;
	private Map<Integer, JLabel> cpuUsageLabelMapping;

	private JPanel content;

	public CpuPanel(MonitorTabProvider monitorTabProvider) {
		this.monitorTabProvider = monitorTabProvider;
		cpuTempLabelMapping = new HashMap<>();
		cpuUsageLabelMapping = new HashMap<>();

		CpuInfoProvider cip = monitorTabProvider.getCpuInfoProvider();
		cpuInfos = cip.getCpuInfo();
		int cols = cpuInfos.size() * 2;

		content = new JPanel();
		content.setLayout(new GridLayout(0, cols));

		for (Entry<Integer, CpuInfo> entry : cpuInfos.entrySet()) {
			content.add(new JLabel(getLabel("cpu.name")));
			content.add(new JLabel(entry.getValue().getModel()));
		}

		for (Entry<Integer, CpuInfo> entry : cpuInfos.entrySet()) {
			content.add(new JLabel(getLabel("cpu.cores.physical")));
			content.add(new JLabel(entry.getValue().getNumberPhysicalCores() + ""));
		}

		for (Entry<Integer, CpuInfo> entry : cpuInfos.entrySet()) {
			content.add(new JLabel(getLabel("cpu.cores.logical")));
			content.add(new JLabel(entry.getValue().getNumberLogicalCores() + ""));
		}

		for (Entry<Integer, CpuInfo> entry : cpuInfos.entrySet()) {
			content.add(new JLabel(getLabel("cpu.temperature")));
			JLabel label = new JLabel(getLabel("cpu.initializing"));
			cpuTempLabelMapping.put(entry.getKey(), label);
			content.add(label);
		}

		for (Entry<Integer, CpuInfo> entry : cpuInfos.entrySet()) {
			content.add(new JLabel(getLabel("cpu.usage")));
			JLabel label = new JLabel(getLabel("cpu.initializing"));
			cpuUsageLabelMapping.put(entry.getKey(), label);
			content.add(label);
		}

		add(content);
	}

	public void temperatureUpdated(Map<String, Double> temperatures) {
		for (Entry<Integer, JLabel> entry : cpuTempLabelMapping.entrySet()) {
			int cpuId = entry.getKey();
			String key = TEMPERATURE_PREFIX + cpuId;

			Double temp = temperatures.get(key);

			if (temp != null) {
				JLabel label = entry.getValue();
				label.setText(String.format("%.0f°C", temp));
			}
		}
	}

	public void cpuUsageUpdated(Map<String, Double> usages) {
		for (Entry<Integer, JLabel> entry : cpuUsageLabelMapping.entrySet()) {
			int cpuId = entry.getKey();
			CpuInfo cpuInfo = cpuInfos.get(cpuId);

			Double usage = 0.0;
			for (Integer logicalCore : cpuInfo.getLogicalCoreIds()) {
				String key = USAGE_PREFIX + logicalCore;
				usage += usages.get(key);
			}

			usage /= cpuInfo.getLogicalCoreIds().size();
			usage *= 100;

			if (usage != null) {
				JLabel label = entry.getValue();
				label.setText(String.format("%.2f%%", usage));
			}
		}
	}

	private String getLabel(String key) {
		return monitorTabProvider.getLabel(key);
	}
}
