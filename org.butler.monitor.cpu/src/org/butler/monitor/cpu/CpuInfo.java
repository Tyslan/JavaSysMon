package org.butler.monitor.cpu;

import java.util.Set;

public interface CpuInfo {
	public int getCpuId();

	public String getModel();

	public int getNumberPhysicalCores();

	public Set<Integer> getPhysicalCoreIds();

	public int getNumberLogicalCores();

	public Set<Integer> getLogicalCoreIds();
}
