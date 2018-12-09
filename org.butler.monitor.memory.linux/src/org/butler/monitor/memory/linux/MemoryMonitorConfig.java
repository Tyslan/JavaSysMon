package org.butler.monitor.memory.linux;

import java.util.concurrent.TimeUnit;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Memory Monitor Config")
public @interface MemoryMonitorConfig {
	@AttributeDefinition
	long getSampleSpeed();

	@AttributeDefinition
	TimeUnit getSampleTimeUnit();
}
