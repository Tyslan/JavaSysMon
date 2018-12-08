package org.butler.monitor.cpu;

import java.util.Map;

public interface CpuTemperatureListener {
	public void temperatureUpdated(Map<String, Double> temperatures);
}
