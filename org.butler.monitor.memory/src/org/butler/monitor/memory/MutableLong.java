package org.butler.monitor.memory;

public class MutableLong {
	private long value;

	public MutableLong(long value) {
		this.value = value;
	}

	public void minus(long value) {
		this.value -= value;
	}

	public long getLong() {
		return value;
	}
}
