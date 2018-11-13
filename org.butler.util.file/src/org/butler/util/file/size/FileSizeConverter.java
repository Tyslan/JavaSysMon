package org.butler.util.file.size;

public class FileSizeConverter {
	public static double convert(double size, FileSizeUnit origUnit, FileSizeUnit newUnit) {
		double origInBytes = size * origUnit.getBytes();
		return origInBytes / newUnit.getBytes();
	}
}
