package org.butler.monitor.system;

public interface SystemPropertyCollector {
	public String getSystemName();

	public String getOS();

	public String getOSVersion();

	public String getKernelVersion();
}
