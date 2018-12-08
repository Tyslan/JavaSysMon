package org.butler.monitor.cpu.linux.temperature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.butler.monitor.cpu.CpuTemperatureListener;
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
@Designate(ocd = CpuTempCollectorConfig.class)
public class LinuxCpuTemperatureProvider {
	/*
	 *
	 * https://askubuntu.com/questions/15832/how-do-i-get-the-cpu-temperature#854029
	 *
	 * https://unix.stackexchange.com/questions/418919/how-to-obtain-cat-cpu-core-
	 * temp-from-isa-adapter-debian#418961
	 *
	 */
	private static final String CPU_TEMP_BASE_DIR = "/sys/class/hwmon";
	private static final String HMON_ID_FILE = "name";
	private static final String CPU_TEMP_ID = "coretemp";
	private static final String CPU_TEMP_LABEL_POSTFIX = "label";
	private static final String CPU_TEMP_INPUT_POSTFIX = "input";

	private static Logger logger = LoggerFactory.getLogger(LinuxCpuTemperatureProvider.class);

	private Map<String, Path> temperatureFiles;

	private List<CpuTemperatureListener> listeners = new CopyOnWriteArrayList<>();;
	private ExecutorService listenerUpdater;

	private ScheduledExecutorService collectorService;

	@Activate
	protected void activate(CpuTempCollectorConfig config) {
		mapTemperatureFiles();

		listenerUpdater = CustomExecutors.newSingleThreadExecutor("linux-cpu-temp-listeners");

		collectorService = Executors.newSingleThreadScheduledExecutor();
		collectorService = CustomExecutors.newSingleThreadScheduledExecutor("linux-cpu-temp-collector");
		collectorService.scheduleAtFixedRate(() -> collectData(), 0, config.getSampleSpeed(),
				config.getSampleTimeUnit());
	}

	@Deactivate
	protected void deactivate() {
		CustomExecutors.shutdownAndAwaitTermination(collectorService, 5, TimeUnit.SECONDS);
		CustomExecutors.shutdownAndAwaitTermination(listenerUpdater, 5, TimeUnit.SECONDS);
	}

	private void mapTemperatureFiles() {
		File baseDir = new File(CPU_TEMP_BASE_DIR);
		File[] subDirs = baseDir.listFiles((current, name) -> new File(current, name).isDirectory());
		File cpuTempDir = getCpuTempDir(subDirs);
		mapTemperatureFiles(cpuTempDir);
	}

	private File getCpuTempDir(File[] subDirs) {
		for (File subDir : subDirs) {
			if (isCpuTempDir(subDir)) {
				return subDir;
			}
		}
		return null;
	}

	private boolean isCpuTempDir(File baseDir) {
		File[] files = baseDir
				.listFiles((current, name) -> new File(current, name).isFile() && HMON_ID_FILE.equals(name));
		if (files.length != 1) {
			logger.warn("multiple files with name {} at {}", HMON_ID_FILE, baseDir.getAbsolutePath());
		}
		File idFile = files[0];
		try (Stream<String> stream = Files.lines(idFile.toPath())) {
			long count = stream.filter(lines -> lines.contains(CPU_TEMP_ID)).count();
			return count > 0;
		} catch (IOException e) {
			logger.error("Error reading file {}", idFile.toPath(), e);
		}
		return false;
	}

	private void mapTemperatureFiles(File cpuTempDir) {
		if (cpuTempDir == null) {
			logger.warn("No temperature dir found");
			return;
		}
		File[] labelFiles = cpuTempDir.listFiles(
				(current, name) -> name.endsWith(CPU_TEMP_LABEL_POSTFIX) && new File(current, name).isFile());
		File[] tempFiles = cpuTempDir.listFiles(
				(current, name) -> name.endsWith(CPU_TEMP_INPUT_POSTFIX) && new File(current, name).isFile());
		Map<String, String> filePrefixLabelMap = mapLabelToPrefix(labelFiles);
		Map<String, File> filePrefixFileMap = mapFileToPrefix(tempFiles);
		createTemperatureFilesMap(filePrefixLabelMap, filePrefixFileMap);
	}

	private Map<String, String> mapLabelToPrefix(File[] labelFiles) {
		Map<String, String> prefixLabelMap = new HashMap<>();
		for (File labelFile : labelFiles) {
			StringBuilder sb = new StringBuilder();
			try (Stream<String> stream = Files.lines(labelFile.toPath())) {
				stream.forEach(line -> sb.append(line));
				String label = sb.toString();

				String fileName = labelFile.getName();
				String[] parts = fileName.split("_");
				prefixLabelMap.put(parts[0], label);
			} catch (IOException e) {
				logger.error("Error reading file {}", labelFile.toPath(), e);
			}
		}

		return prefixLabelMap;
	}

	private Map<String, File> mapFileToPrefix(File[] tempFiles) {
		Map<String, File> prefixFileMap = new HashMap<>();
		for (File tempFile : tempFiles) {
			String fileName = tempFile.getName();
			String[] parts = fileName.split("_");
			prefixFileMap.put(parts[0], tempFile);
		}

		return prefixFileMap;
	}

	private void createTemperatureFilesMap(Map<String, String> filePrefixLabelMap,
			Map<String, File> filePrefixFileMap) {
		temperatureFiles = new HashMap<>();
		for (String prefix : filePrefixLabelMap.keySet()) {
			temperatureFiles.put(filePrefixLabelMap.get(prefix), filePrefixFileMap.get(prefix).toPath());
		}
	}

	private void collectData() {
		Map<String, Double> temperatures = new HashMap<>();
		for (Entry<String, Path> entry : temperatureFiles.entrySet()) {
			Double temp = getTemp(entry.getValue());
			if (temp != null) {
				temperatures.put(entry.getKey(), temp);
			}
		}
		updateListeners(temperatures);
	}

	private Double getTemp(Path path) {
		StringBuilder sb = new StringBuilder();
		try (Stream<String> stream = Files.lines(path)) {
			stream.forEach(line -> sb.append(line));
			String value = sb.toString().trim();
			long mCelsiusTemp = Long.parseLong(value);
			return mCelsiusTemp / 1000.0;
		} catch (IOException e) {
			logger.error("Error reading file {}", path, e);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
		return null;
	}

	private void updateListeners(Map<String, Double> temperatures) {
		for (CpuTemperatureListener listener : listeners) {
			listenerUpdater.execute(() -> listener.temperatureUpdated(temperatures));
		}
		logger.debug("CPU: {}°C", temperatures.get("Package id 0"));

	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addCpuTemperatureListener(CpuTemperatureListener listener) {
		listeners.add(listener);
	}

	public void removeCpuTemperatureListener(CpuTemperatureListener listener) {
		listeners.remove(listener);
	}
}
