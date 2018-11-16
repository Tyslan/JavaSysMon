package org.butler.monitor.network.linux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

import org.butler.monitor.network.NetworkSpeedData;
import org.butler.monitor.network.NetworkSpeedDataListener;
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
@Designate(ocd = NetworkDataCollectorConfig.class)
public class NetworkDataCollector {
	private static final Logger logger = LoggerFactory.getLogger(NetworkDataCollector.class);

	private Map<String, RawNetworkData> lastCollectedData;

	private List<NetworkSpeedDataListener> networkSpeedListeners = new CopyOnWriteArrayList<>();;
	private ExecutorService listenerUpdater;

	private ScheduledExecutorService executorService;

	@Activate
	protected void activate(NetworkDataCollectorConfig config) {
		init(config.getSampleSpeed(), config.getSampleTimeUnit());
	}

	@Deactivate
	protected void deactivate() {
		CustomExecutors.shutdownAndAwaitTermination(executorService, 5, TimeUnit.SECONDS);
		CustomExecutors.shutdownAndAwaitTermination(listenerUpdater, 5, TimeUnit.SECONDS);
	}

	public void init(long sampleRate, TimeUnit sampleUnit) {
		try {
			this.lastCollectedData = getRawData();
		} catch (IOException e) {
			logger.error("Error during init. Couldn't collect raw data.", e);
		}

		this.listenerUpdater = CustomExecutors.newSingleThreadExecutor("linux-network-speed-listeners");

		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService = CustomExecutors.newSingleThreadScheduledExecutor("linux-network-data-collector");
		executorService.scheduleAtFixedRate(() -> collectData(), 0, sampleRate, sampleUnit);
	}

	public void collectData() {
		try {
			Map<String, RawNetworkData> rawData = getRawData();
			sendSpeedUpdates(rawData);
			lastCollectedData = rawData;
		} catch (IOException e) {
			logger.error("Couldn't collect raw data.", e);
		}
	}

	private static Map<String, RawNetworkData> getRawData() throws IOException {
		LocalDateTime timestamp = LocalDateTime.now();
		try (Stream<String> lines = Files.lines(Paths.get("/proc/net/dev"))) {
			return lines.filter(e -> !e.contains("|")).map(dataLine -> new RawNetworkData(timestamp, dataLine))
					.collect(Collectors.toMap(RawNetworkData::getNetworkInterface, Function.identity()));
		} catch (IOException e) {
			throw e;
		}
	}

	private void sendSpeedUpdates(Map<String, RawNetworkData> newData) {
		if (lastCollectedData == null) {
			return;
		}

		Map<String, NetworkSpeedData> speedData = generateNewNetworkSpeedData(newData);

		for (NetworkSpeedDataListener listener : networkSpeedListeners) {
			listenerUpdater.execute(() -> listener.speedUpdated(speedData));
		}
		logger.debug("{}", speedData.get("global"));
	}

	private Map<String, NetworkSpeedData> generateNewNetworkSpeedData(Map<String, RawNetworkData> newData) {
		Map<String, NetworkSpeedData> speedData = new HashMap<>();

		long durationInMillis = 0l;
		long totalDownloaded = 0l;
		long totalUploaded = 0l;

		for (Entry<String, RawNetworkData> entry : newData.entrySet()) {
			String key = entry.getKey();
			NetworkSpeedData data = new NetworkSpeedDataImpl(lastCollectedData.get(key), entry.getValue());

			if (!key.equals("lo")) {
				durationInMillis = data.getDurationInMillis();
				totalDownloaded += data.getDownloadedBytes();
				totalUploaded += data.getUploadedBytes();
			}
		}
		NetworkSpeedData globalData = new NetworkSpeedDataImpl("global", durationInMillis, totalDownloaded,
				totalUploaded);

		speedData.put(globalData.getNetworkInterface(), globalData);

		return speedData;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addNetworkSpeedListener(NetworkSpeedDataListener listener) {
		networkSpeedListeners.add(listener);
	}

	public void removeNetworkSpeedListener(NetworkSpeedDataListener listener) {
		networkSpeedListeners.remove(listener);
	}
}
