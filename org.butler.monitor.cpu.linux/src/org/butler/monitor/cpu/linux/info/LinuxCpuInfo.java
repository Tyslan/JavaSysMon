package org.butler.monitor.cpu.linux.info;

import org.butler.monitor.cpu.CpuInfo;

public class LinuxCpuInfo implements CpuInfo {
	private int cpuId;
	private String model;
	private int numberPhysicalCores;
	private int numberLogicalCores;

	public void setCpuId(int cpuId) {
		this.cpuId = cpuId;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setNumberPhysicalCores(int numberPhysicalCores) {
		this.numberPhysicalCores = numberPhysicalCores;
	}

	public void setNumberLogicalCores(int numberLogicalCores) {
		this.numberLogicalCores = numberLogicalCores;
	}

	public int getCpuId() {
		return cpuId;
	}

	@Override
	public String getModel() {
		return model;
	}

	@Override
	public int getNumberPhysicalCores() {
		return numberPhysicalCores;
	}

	@Override
	public int getNumberLogicalCores() {
		return numberLogicalCores;
	}

}
