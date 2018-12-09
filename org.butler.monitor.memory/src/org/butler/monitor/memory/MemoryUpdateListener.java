package org.butler.monitor.memory;

import java.util.Map;

public interface MemoryUpdateListener {
	public void memoryUsageUpdated(Map<MemoryType, Long> memoryUsage);
}
