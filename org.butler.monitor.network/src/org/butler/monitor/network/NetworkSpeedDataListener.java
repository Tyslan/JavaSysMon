package org.butler.monitor.network;

import java.util.Map;

public interface NetworkSpeedDataListener {
	public void speedUpdated(Map<String, NetworkSpeedData> newData);
}
