package org.butler.monitor.cpu.linux.usage;

public class RawCpuUsage {
	private String key;

	private long userTime;
	private long niceTime;
	private long systemTime;
	private long idleTime;
	private long ioWaitTime;
	private long irqTime;
	private long softIrqTime;

	public RawCpuUsage(String key, long userTime, long niceTime, long systemTime, long idleTime, long ioWaitTime,
			long irqTime, long softIrqTime) {
		super();
		this.key = key;
		this.userTime = userTime;
		this.niceTime = niceTime;
		this.systemTime = systemTime;
		this.idleTime = idleTime;
		this.ioWaitTime = ioWaitTime;
		this.irqTime = irqTime;
		this.softIrqTime = softIrqTime;
	}

	public String getKey() {
		return key;
	}

	public long getTotalTime() {
		return userTime + niceTime + systemTime + idleTime + ioWaitTime + irqTime + softIrqTime;
	}

	public long getIdleTime() {
		return idleTime;
	}
}