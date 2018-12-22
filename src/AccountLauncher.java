
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Locale;

public final class AccountLauncher {

	public static String allowOptions = " -allow norender,lowcpu,norandoms ";
	public static enum OSType {
		Windows, MacOS, Linux;
	};
	
	public static String lastName = "";
	public static long lastStartup = 0;

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

	public static void launchClient(String address) {
		System.out.println(address);
		if(!address.equals(lastName) || System.currentTimeMillis() > (lastStartup + 1000 * 120)) {
			lastName = address;
			lastStartup = System.currentTimeMillis();
		System.out.println("Starting");
		ProcessBuilder linuxBuilder = new ProcessBuilder("/bin/bash", "-c",
				"java -jar " + "." + "./rspeer-launcher.jar" + " " + address);

		ProcessBuilder windowsBuilder = new ProcessBuilder("cmd.exe", "/c",
				"java -jar " +  "./rspeer-launcher.jar" + " " + address);
		windowsBuilder.inheritIO();
		ProcessBuilder macBuilder = new ProcessBuilder("osascript", "-e",
				"tell application \"Terminal\" to do script \"java -jar " +  "./rspeer-launcher.jar" + " " + address);

		try {
			System.out.println(AccountLauncher.getOperatingSystemType());
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
				System.out.println("lets go linux");
				Process p3 = linuxBuilder.start();
				setOutputStream(p3);
				break;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println(e1.getMessage());
		}
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