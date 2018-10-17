
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Locale;

public final class AccountLauncher {

	public static enum OSType {
		Windows, MacOS, Linux;
	};

	protected static OSType detectedOS;

	public static OSType getOperatingSystemType() {
		if (detectedOS == null) {
			String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
				detectedOS = OSType.MacOS;
			} else if (OS.indexOf("win") >= 0) {
				detectedOS = OSType.Windows;
			} else
				detectedOS = OSType.Linux;
		}
		return detectedOS;
	}

	public static void launchClient(String path, String script, String clientUser, String clientPass, String botUser,
			String botPass, String world, String proxy, String params) {
		System.out.println("Starting");
		ProcessBuilder linuxBuilder = new ProcessBuilder("/bin/bash", "-c",
				"java -jar " + path + " -Xmx750m -allow norandoms" + proxy + " -login " + clientUser + ":" + clientPass
						+ " -bot " + botUser + ":" + botPass + ":1234" + " -world " + world + " -script " + script + ":"
						+ params);

		ProcessBuilder windowsBuilder = new ProcessBuilder("cmd.exe", "/c",
				"java -jar " + path + " -Xmx750m -allow norandoms" + proxy + " -login " + clientUser + ":" + clientPass
						+ " -bot " + botUser + ":" + botPass + ":1234" + " -world " + world + " -script " + script + ":"
						+ params);
		windowsBuilder.inheritIO();
		ProcessBuilder macBuilder = new ProcessBuilder("osascript", "-e",
				"tell application \"Terminal\" to do script \"java -jar " + path + " -Xmx750m -allow norandoms" + proxy + " -login " + clientUser + ":" + clientPass
				+ " -bot " + botUser + ":" + botPass + ":1234" + " -world " + world + " -script " + script + ":"
				+ params +  "\"");

		try {
			switch (AccountLauncher.getOperatingSystemType()) {
			case Windows:
				int i = 1;
				System.out.println("Start windows");
				Process p1 = windowsBuilder.start();
				setOutputStream(p1);

				break;
			case MacOS:
				Process p2 = macBuilder.start();

				break;
			case Linux:
				Process p3 = linuxBuilder.start();
				break;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println(e1.getMessage());
		}

	}

	public static void setOutputStream(Process process) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				InputStream is = process.getInputStream();
				int c;
				try {
					while ((c = is.read()) >= 0) {
						System.out.println(c);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();

	}
}

// Your proxy string will have to include " -proxy ip:port", I did it like that
// so it can be optional.