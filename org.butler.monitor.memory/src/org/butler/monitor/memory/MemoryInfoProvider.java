package org.butler.monitor.memory;

public interface MemoryInfoProvider {
	public long getTotalRam();

	public long getTotalSwap();
}
