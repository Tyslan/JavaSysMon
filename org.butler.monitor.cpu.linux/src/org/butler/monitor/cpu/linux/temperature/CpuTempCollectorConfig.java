package org.butler.monitor.cpu.linux.temperature;

import java.util.concurrent.TimeUnit;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Cpu Temperature Collector Config")
public @interface CpuTempCollectorConfig {
	@AttributeDefinition
	long getSampleSpeed();

	@AttributeDefinition
	TimeUnit getSampleTimeUnit();
}
