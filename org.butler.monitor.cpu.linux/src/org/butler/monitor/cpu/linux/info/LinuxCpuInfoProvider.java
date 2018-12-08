package org.butler.monitor.cpu.linux.info;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.butler.monitor.cpu.CpuInfo;
import org.butler.monitor.cpu.CpuInfoProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LinuxCpuInfoProvider implements CpuInfoProvider {
	private static final String CPU_ID_STRING = "physical id";
	private static final String MODEL_STRING = "model name";
	private static final String PHYSICAL_CORES_STRING = "cpu cores";
	private static final String PHYSICAL_CORE_ID_STRING = "core id";
	private static final String LOGICAL_CORES_STRING = "siblings";
	private static final String LOGICAL_CORE_ID_STRING = "processor";

	private static final String CPU_INFO_FILE = "/proc/cpuinfo";

	private static Logger logger = LoggerFactory.getLogger(LinuxCpuInfoProvider.class);

	private Map<Integer, CpuInfo> cpuInfoMap;

	@Activate
	protected void activate() {
		cpuInfoMap = new HashMap<>();
		parseCpuInfoFile();
	}

	private void parseCpuInfoFile() {
		LinuxCpuInfo cpuInfo = new LinuxCpuInfo();
		try (Stream<String> lines = Files.lines(Paths.get(CPU_INFO_FILE))) {
			lines.forEach(line -> {
				if (line.isEmpty()) {
					flush(cpuInfo);
				}

				String[] parts = line.split(":");
				String key = parts[0].trim();
				String value = null;

				if (parts.length > 1) {
					value = parts[1].trim();
				}

				switch (key) {
				case CPU_ID_STRING:
					cpuInfo.setCpuId(Integer.parseInt(value));
					break;
				case MODEL_STRING:
					cpuInfo.setModel(value);
					break;
				case PHYSICAL_CORES_STRING:
					cpuInfo.setNumberPhysicalCores(Integer.parseInt(value));
					break;
				case PHYSICAL_CORE_ID_STRING:
					cpuInfo.addPhysicalCoreId(Integer.parseInt(value));
					break;
				case LOGICAL_CORES_STRING:
					cpuInfo.setNumberLogicalCores(Integer.parseInt(value));
					break;
				case LOGICAL_CORE_ID_STRING:
					cpuInfo.addLogicalCoreId(Integer.parseInt(value));
					break;
				default:
					break;
				}
			});

		} catch (IOException e) {
			logger.error("Couldn't collect system properties", e);
		}
	}

	private void flush(LinuxCpuInfo cpuInfo) {
		int cpuId = cpuInfo.getCpuId();
		if (cpuInfoMap.containsKey(cpuId)) {
			LinuxCpuInfo info = (LinuxCpuInfo) cpuInfoMap.get(cpuId);
			cpuInfo.merge(info);

		}

		cpuInfoMap.put(cpuId, cpuInfo);
	}

	@Override
	public Map<Integer, CpuInfo> getCpuInfo() {
		return cpuInfoMap;
	}
}
