package org.butler.monitor.cpu;

import java.util.Map;

public interface CpuInfoProvider {
	public Map<Integer, CpuInfo> getCpuInfo();
}
