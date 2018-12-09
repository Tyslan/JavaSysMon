package org.butler.monitor.memory.linux;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.butler.monitor.memory.MemoryInfoProvider;
import org.butler.monitor.memory.MemoryType;
import org.butler.monitor.memory.MemoryUpdateListener;
import org.butler.monitor.memory.MutableLong;
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

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true)
@Designate(ocd = MemoryMonitorConfig.class)
public class LinuxMemoryMonitor implements MemoryInfoProvider {
	private static final String MEMORY_FILE = "/proc/meminfo";
	private static final String TOTAL_MEMORY_KEY = "MemTotal";
	private static final String FREE_MEMORY_KEY = "MemFree";
	private static final String AVAILABLE_MEMORY_KEY = "MemAvailable";
	private static final String TOTAL_SWAP_KEY = "SwapTotal";
	private static final String CACHED_SWAP_KEY = "SwapCached";

	private static Logger logger = LoggerFactory.getLogger(LinuxMemoryMonitor.class);

	private List<MemoryUpdateListener> listeners = new CopyOnWriteArrayList<>();;
	private ExecutorService listenerUpdater;

	private ScheduledExecutorService collectorService;

	private long totalRam;
	private long totalSwap;

	@Activate
	protected void activate(MemoryMonitorConfig config) {
		initializeAttributes();

		listenerUpdater = CustomExecutors.newSingleThreadExecutor("linux-memory-listeners");

		collectorService = Executors.newSingleThreadScheduledExecutor();
		collectorService = CustomExecutors.newSingleThreadScheduledExecutor("linux-memory-collector");
		collectorService.scheduleAtFixedRate(() -> collectData(), 0, config.getSampleSpeed(),
				config.getSampleTimeUnit());
	}

	@Deactivate
	protected void deactivate() {
		CustomExecutors.shutdownAndAwaitTermination(collectorService, 5, TimeUnit.SECONDS);
		CustomExecutors.shutdownAndAwaitTermination(listenerUpdater, 5, TimeUnit.SECONDS);
	}

	private void initializeAttributes() {
		try (Stream<String> stream = Files.lines(Paths.get(MEMORY_FILE))) {
			stream.filter(line -> line.contains(TOTAL_MEMORY_KEY) || line.contains(TOTAL_SWAP_KEY)).forEach(line -> {
				String[] parts = line.split("\\s+");
				String key = parts[0].replace(":", "");
				long value = Long.parseLong(parts[1]) * 1000;

				switch (key) {
				case TOTAL_MEMORY_KEY:
					totalRam = value;
					break;
				case TOTAL_SWAP_KEY:
					totalSwap = value;
					break;
				}
			});
		} catch (IOException e) {
			logger.error("Error reading file {}", MEMORY_FILE, e);
		}
	}

	private void collectData() {
		Map<MemoryType, Long> usage = getUsedMemory();
		updateListeners(usage);
	}

	private Map<MemoryType, Long> getUsedMemory() {
		try (Stream<String> stream = Files.lines(Paths.get(MEMORY_FILE))) {
			Map<MemoryType, Long> usedMemory = new HashMap<>();
			final MutableLong usedRam = new MutableLong(totalRam);
			stream
					// Filter useful lines
					.filter(line -> line.contains(FREE_MEMORY_KEY) || line.contains(AVAILABLE_MEMORY_KEY)
							|| line.contains(CACHED_SWAP_KEY))
					// Process useful lines
					.forEach(line -> {
						String[] parts = line.split("\\s+");
						String key = parts[0].replace(":", "");
						long value = Long.parseLong(parts[1]) * 1000;

						switch (key) {
						case FREE_MEMORY_KEY:
						case AVAILABLE_MEMORY_KEY:
							usedRam.minus(value);
							break;
						case CACHED_SWAP_KEY:
							usedMemory.put(MemoryType.SWAP, totalSwap - value);
							break;
						}
					});
			usedMemory.put(MemoryType.RAM, usedRam.getLong());
			return usedMemory;
		} catch (IOException e) {
			logger.error("Error reading file {}", MEMORY_FILE, e);
			return new HashMap<>();
		}
	}

	private void updateListeners(Map<MemoryType, Long> usage) {
		for (MemoryUpdateListener listener : listeners) {
			listenerUpdater.execute(() -> listener.memoryUsageUpdated(usage));
		}
		logger.debug("RAM: {}/{}", usage.get(MemoryType.RAM), totalRam);
		logger.debug("SWAP: {}/{}", usage.get(MemoryType.SWAP), totalSwap);
	}

	@Override
	public long getTotalRam() {
		return totalRam;
	}

	@Override
	public long getTotalSwap() {
		return totalSwap;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addMemoryUpdateListener(MemoryUpdateListener listener) {
		listeners.add(listener);
	}

	public void removeMemoryUpdateListener(MemoryUpdateListener listener) {
		listeners.remove(listener);
	}
}
