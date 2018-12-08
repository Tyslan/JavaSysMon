package org.butler.monitor.cpu.linux.usage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.butler.monitor.cpu.CpuUsageListener;
import org.butler.monitor.cpu.linux.temperature.CpuTempCollectorConfig;
import org.butler.util.threads.CustomExecutors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
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

	private Map<String, RawCpuUsage> lastCollectedData;

	private List<CpuUsageListener> listeners = new CopyOnWriteArrayList<>();;
	private ExecutorService listenerUpdater;

	private ScheduledExecutorService collectorService;

	@Activate
	protected void activate(CpuTempCollectorConfig config) {
		try {
			lastCollectedData = getRawData();
		} catch (IOException e) {
			logger.error("Init Error: Couldn't open cpu usage file {}", CPU_USAGE_FILE, e);
		}

		listenerUpdater = CustomExecutors.newSingleThreadExecutor("linux-cpu-usage-listeners");

		collectorService = Executors.newSingleThreadScheduledExecutor();
		collectorService = CustomExecutors.newSingleThreadScheduledExecutor("linux-cpu-usage-collector");
		collectorService.scheduleAtFixedRate(() -> collectData(), 0, config.getSampleSpeed(),
				config.getSampleTimeUnit());
	}

	@Deactivate
	protected void deactivate() {
		CustomExecutors.shutdownAndAwaitTermination(collectorService, 5, TimeUnit.SECONDS);
		CustomExecutors.shutdownAndAwaitTermination(listenerUpdater, 5, TimeUnit.SECONDS);
	}

	/**
	 * https://supportcenter.checkpoint.com/supportcenter/portal?eventSubmit_doGoviewsolutiondetails=&solutionid=sk65143
	 */
	private void collectData() {
		try {
			Map<String, RawCpuUsage> raw = getRawData();
			if (lastCollectedData == null) {
				lastCollectedData = raw;
			} else {
				Map<String, Double> usages = calculateUsages(raw);
				updateListeners(usages);
				lastCollectedData = raw;
			}
		} catch (IOException e) {
			logger.error("Couldn't open cpu usage file {}", CPU_USAGE_FILE, e);
		}

	}

	private Map<String, RawCpuUsage> getRawData() throws IOException {
		try (Stream<String> lines = Files.lines(Paths.get(CPU_USAGE_FILE))) {
//			Map<String, Double> usageMap = new HashMap<>();
//			lines.filter(line -> line.startsWith("cpu")).forEach(line -> {
//				String[] parts = line.split("\\s+");
//				String key = parts[0];
//
//				long user = Long.parseLong(parts[1]);
//				long nice = Long.parseLong(parts[2]);
//				long system = Long.parseLong(parts[3]);
//				long idle = Long.parseLong(parts[4]);
//				long iowait = Long.parseLong(parts[5]);
//				long irq = Long.parseLong(parts[6]);
//				long softirq = Long.parseLong(parts[7]);
//
//				long total = user + nice + system + idle + iowait + irq + softirq;
//				double usage = (total - idle) * 1.0 / total;
//				usageMap.put(key, usage);
//			});
			return lines.filter(line -> line.startsWith("cpu")).map(line -> {
				String[] parts = line.split("\\s+");
				String key = parts[0];

				long user = Long.parseLong(parts[1]);
				long nice = Long.parseLong(parts[2]);
				long system = Long.parseLong(parts[3]);
				long idle = Long.parseLong(parts[4]);
				long iowait = Long.parseLong(parts[5]);
				long irq = Long.parseLong(parts[6]);
				long softirq = Long.parseLong(parts[7]);
				return new RawCpuUsage(key, user, nice, system, idle, iowait, irq, softirq);
			}).collect(Collectors.toMap(RawCpuUsage::getKey, Function.identity()));
		} catch (IOException e) {
			throw e;
		}
	}

	private Map<String, Double> calculateUsages(Map<String, RawCpuUsage> raw) {
		Map<String, Double> usages = new HashMap<>();
		for (Entry<String, RawCpuUsage> entry : raw.entrySet()) {
			double usage = calculateUsage(entry);
			usages.put(entry.getKey(), usage);
		}
		return usages;
	}

	private double calculateUsage(Entry<String, RawCpuUsage> entry) {
		RawCpuUsage oldData = lastCollectedData.get(entry.getKey());
		RawCpuUsage newData = entry.getValue();

		long totalDiff = newData.getTotalTime() - oldData.getTotalTime();
		long idleDiff = newData.getIdleTime() - oldData.getIdleTime();

		return (totalDiff - idleDiff) * 1.0 / totalDiff;
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
