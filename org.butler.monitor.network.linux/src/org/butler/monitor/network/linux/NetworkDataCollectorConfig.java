package org.butler.monitor.network.linux;

import java.util.concurrent.TimeUnit;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Network Data Collector Config")
public @interface NetworkDataCollectorConfig {
	@AttributeDefinition
	long getSampleSpeed();

	@AttributeDefinition
	TimeUnit getSampleTimeUnit();
}
