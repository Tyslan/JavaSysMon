package org.butler.monitor.cpu.linux.usage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.butler.monitor.cpu.CpuUsageListener;
import org.butler.monitor.cpu.linux.temperature.CpuTempCollectorConfig;
import org.butler.util.threads.CustomExecutors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = CpuUsageCollectorConfig.class)
public class LinuxCpuUsageProvider {
	private static final String CPU_USAGE_FILE = "/proc/stat";

	private static Logger logger = LoggerFactory.getLogger(LinuxCpuUsageProvider.class);

	private List<CpuUsageListener> listeners = new CopyOnWriteArrayList<>();;
	private ExecutorService listenerUpdater;

	private ScheduledExecutorService collectorService;

	@Activate
	protected void activate(CpuTempCollectorConfig config) {
		listenerUpdater = CustomExecutors.newSingleThreadExecutor("linux-cpu-usage-listeners");

		collectorService = Executors.newSingleThreadScheduledExecutor();
		collectorService = CustomExecutors.newSingleThreadScheduledExecutor("linux-cpu-usage-collector");
		collectorService.scheduleAtFixedRate(() -> collectData(), 0, config.getSampleSpeed(),
				config.getSampleTimeUnit());
	}

	/**
	 * https://supportcenter.checkpoint.com/supportcenter/portal?eventSubmit_doGoviewsolutiondetails=&solutionid=sk65143
	 */
	private void collectData() {
		try (Stream<String> lines = Files.lines(Paths.get(CPU_USAGE_FILE))) {
			Map<String, Double> usageMap = new HashMap<>();
			lines.filter(line -> line.startsWith("cpu")).forEach(line -> {
				String[] parts = line.split("\\s+");
				String key = parts[0];

				long user = Long.parseLong(parts[1]);
				long nice = Long.parseLong(parts[2]);
				long system = Long.parseLong(parts[3]);
				long idle = Long.parseLong(parts[4]);
				long iowait = Long.parseLong(parts[5]);
				long irq = Long.parseLong(parts[6]);
				long softirq = Long.parseLong(parts[7]);

				long total = user + nice + system + idle + iowait + irq + softirq;
				double usage = (total - idle) * 1.0 / total;
				usageMap.put(key, usage);
			});
			updateListeners(usageMap);
		} catch (IOException e) {
			logger.error("Couldn't open cpu usage file {}", CPU_USAGE_FILE, e);
		}
	}

	private void updateListeners(Map<String, Double> usages) {
		for (CpuUsageListener listener : listeners) {
			listenerUpdater.execute(() -> listener.cpuUsageUpdated(usages));
		}
		logger.debug("CPU: {}%", String.format("%.2f", usages.get("cpu") * 100));
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addCpuUsageListener(CpuUsageListener listener) {
		listeners.add(listener);
	}

	public void removeCpuUsageListener(CpuUsageListener listener) {
		listeners.remove(listener);
	}
}
