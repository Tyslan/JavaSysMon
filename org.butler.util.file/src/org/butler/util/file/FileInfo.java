package org.butler.util.file;

import java.nio.file.Path;

public class FileInfo {
	private long size;
	private Path path;

	public FileInfo(long size, Path path) {
		super();
		this.size = size;
		this.path = path;
	}

	public String getName() {
		return path.getFileName().toString();
	}

	public long getSize() {
		return size;
	}

	public Path getPath() {
		return path;
	}
}
