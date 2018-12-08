package org.butler.monitor.cpu.linux.info;

import java.util.Set;
import java.util.TreeSet;

import org.butler.monitor.cpu.CpuInfo;

public class LinuxCpuInfo implements CpuInfo {
	private int cpuId;
	private String model;
	private int numberPhysicalCores;
	private Set<Integer> physicalCoreIds;
	private int numberLogicalCores;
	private Set<Integer> logicalCoreIds;

	public LinuxCpuInfo() {
		physicalCoreIds = new TreeSet<>();
		logicalCoreIds = new TreeSet<>();
	}

	public void setCpuId(int cpuId) {
		this.cpuId = cpuId;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setNumberPhysicalCores(int numberPhysicalCores) {
		this.numberPhysicalCores = numberPhysicalCores;
	}

	public void addPhysicalCoreId(int id) {
		this.physicalCoreIds.add(id);
	}

	public void setNumberLogicalCores(int numberLogicalCores) {
		this.numberLogicalCores = numberLogicalCores;
	}

	public void addLogicalCoreId(int id) {
		this.logicalCoreIds.add(id);
	}

	void merge(LinuxCpuInfo merger) {
		if (this.cpuId != merger.cpuId) {
			return;
		}

		physicalCoreIds.addAll(merger.physicalCoreIds);
		logicalCoreIds.addAll(merger.logicalCoreIds);
	}

	@Override
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
	public Set<Integer> getPhysicalCoreIds() {
		return physicalCoreIds;
	}

	@Override
	public int getNumberLogicalCores() {
		return numberLogicalCores;
	}

	@Override
	public Set<Integer> getLogicalCoreIds() {
		return logicalCoreIds;
	}

}
