package org.butler.util.file.size;

public class FileSizeFormatter {
	public static String getFormattedSI(double bytes) {
		if (bytes < 1024) {
			return createSpeedString(bytes, FileSizeUnit.BYTE);
		}

		FileSizeUnit newSizeUnit = FileSizeUnit.KILOBYTE;
		double newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.MEGABYTE;
		newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.GIGABYTE;
		newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.TERABYTE;
		newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		return bytes + FileSizeUnit.BYTE.getAbbreviation();
	}

	public static String getFormattedBinary(double bytes) {
		if (bytes < 1024) {
			return createSpeedString(bytes, FileSizeUnit.BYTE);
		}

		FileSizeUnit newSizeUnit = FileSizeUnit.KIBIBYTE;
		double newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.MEBIBYTE;
		newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.GIBIBYTE;
		newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		newSizeUnit = FileSizeUnit.TEBIBYTE;
		newSize = FileSizeConverter.convert(bytes, FileSizeUnit.BYTE, newSizeUnit);
		if (newSize < 1024) {
			return createSpeedString(newSize, newSizeUnit);
		}

		return bytes + FileSizeUnit.BYTE.getAbbreviation();
	}

	public static String createSpeedString(double size, FileSizeUnit sizeUnit) {
		if (sizeUnit == FileSizeUnit.BYTE) {
			return String.format("%.0f %s", size, sizeUnit.getAbbreviation());
		}
		return String.format("%.2f %s", size, sizeUnit.getAbbreviation());
	}
}
