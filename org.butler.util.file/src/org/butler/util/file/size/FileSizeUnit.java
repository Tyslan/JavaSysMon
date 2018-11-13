package org.butler.util.file.size;

public enum FileSizeUnit {
	// BASE
	BYTE(1, "B", "byte"),

	// SI
	KILOBYTE(1000, "kB", "kilobyte"), MEGABYTE(1000 * 1000, "MB", "megabyte"),
	GIGABYTE(1000 * 1000 * 1000, "GB", "gigabyte"), TERABYTE(1000 * 1000 * 1000 * 1000, "TB", "terabyte"),
	PETABYTE(1000 * 1000 * 1000 * 1000 * 1000, "PB", "petabyte"),

	// BINARY
	KIBIBYTE(1024, "KiB", "kibibyte"), MEBIBYTE(1024 * 1024, "MiB", "mebibyte"),
	GIBIBYTE(1024 * 1024 * 1024, "GiB", "gibibyte"), TEBIBYTE(1024 * 1024 * 1024 * 1024, "TiB", "tebibyte"),
	PEBIBYTE(1024 * 1024 * 1024 * 1024 * 1024, "PiB", "pebibyte");

	private long bytes;
	private String abbreviation;
	private String name;

	private FileSizeUnit(long bytes, String abbreviation, String name) {
		this.bytes = bytes;
		this.abbreviation = abbreviation;
		this.name = name;
	}

	public long getBytes() {
		return bytes;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getName() {
		return name;
	}
}
