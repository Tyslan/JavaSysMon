package org.butler.monitor.cpu;

public interface CpuInfo {
	public String getModel();

	public int getNumberPhysicalCores();

	public int getNumberLogicalCores();
}
