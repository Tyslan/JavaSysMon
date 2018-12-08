package org.butler.monitor.cpu;

import java.util.Map;

public interface CpuUsageListener {
	public void cpuUsageUpdated(Map<String, Double> usages);
}
