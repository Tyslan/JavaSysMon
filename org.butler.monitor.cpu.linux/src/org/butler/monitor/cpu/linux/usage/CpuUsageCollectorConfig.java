package org.butler.monitor.cpu.linux.usage;

import java.util.concurrent.TimeUnit;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Cpu Usage Collector Config")
public @interface CpuUsageCollectorConfig {
	@AttributeDefinition
	long getSampleSpeed();

	@AttributeDefinition
	TimeUnit getSampleTimeUnit();
}
