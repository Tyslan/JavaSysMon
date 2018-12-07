package org.butler.util.os.linux;

import java.io.IOException;
import java.io.InputStream;

public class LinuxCommandExecutor {

	public static InputStream runCommand(String command, boolean waitForResponse)
			throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
		pb.redirectErrorStream(true);

		Process p = pb.start();
		InputStream s = p.getInputStream();

		if (!waitForResponse) {
			return s;
		}

		p.waitFor();

		return s;
	}
}
