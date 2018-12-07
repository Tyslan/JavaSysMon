package org.butler.monitor.system.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.butler.monitor.system.SystemPropertyCollector;
import org.butler.util.os.linux.LinuxCommandExecutor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LinuxSystemPropertyCollector implements SystemPropertyCollector {
	private static String SYSTEM_NAME_COMMAND = "uname -n";
	private static String KERNEL_VERSION_COMMAND = "uname -r";

	private static String DISTRO_NAME_KEY = "NAME";
	private static String DISTRO_VERSION_KEY = "VERSION";

	private static Logger logger = LoggerFactory.getLogger(LinuxSystemPropertyCollector.class);

	private String systemName;
	private String osName;
	private String osVersion;
	private String kernelVersion;

	@Activate
	protected void activate() {
		systemName = determineSystemName();
		kernelVersion = determineKernelVersion();

		Map<String, String> distroProperties = collectDistroProperties();
		osName = distroProperties.get(DISTRO_NAME_KEY);
		osVersion = distroProperties.get(DISTRO_VERSION_KEY);
	}

	private String determineSystemName() {
		InputStream is;
		try {
			is = LinuxCommandExecutor.runCommand(SYSTEM_NAME_COMMAND, true);
			return toString(is);
		} catch (IOException | InterruptedException e) {
			logger.error("Hostname couldn't be determined", e);
			return "UNKNOWN";
		}
	}

	private Map<String, String> collectDistroProperties() {
		try (Stream<String> lines = Files.lines(Paths.get("/etc/os-release"))) {
			return lines.collect(Collectors.toMap(this::getPropertyName, this::getPropertyValue));
		} catch (IOException e) {
			logger.error("Couldn't collect system properties", e);
			return new HashMap<>();
		}
	}

	private String getPropertyName(String line) {
		String[] parts = line.split("=");
		return parts[0];
	}

	private String getPropertyValue(String line) {
		String[] parts = line.split("=");
		String value = parts[1];
		return value.replaceAll("\"", "");

	}

	private String determineKernelVersion() {
		InputStream is;
		try {
			is = LinuxCommandExecutor.runCommand(KERNEL_VERSION_COMMAND, true);
			return toString(is);
		} catch (IOException | InterruptedException e) {
			logger.error("Kernel version couldn't be determined", e);
			return "UNKNOWN";
		}
	}

	private String toString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		return br.lines().collect(Collectors.joining("\n"));
	}

	@Override
	public String getSystemName() {
		return systemName;
	}

	@Override
	public String getOS() {
		return osName;
	}

	@Override
	public String getOSVersion() {
		return osVersion;
	}

	@Override
	public String getKernelVersion() {
		return kernelVersion;
	}

}
