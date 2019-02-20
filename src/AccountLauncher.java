
import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.util.HashSet;
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

	static HashSet<Process> runningprocesses = new HashSet<>();
	public static void launchClient(String address) {
		runningprocesses.removeIf(process -> process == null || !process.isAlive());
//		if(runningprocesses.size() >= 6)
//			return;

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
			Process p = null;
			try {
				//System.out.println(AccountLauncher.getOperatingSystemType());
				switch (AccountLauncher.getOperatingSystemType()) {

				case Windows:
					int i = 1;
					System.out.println("Start windows");
					p = windowsBuilder.start();
					setOutputStream(p);

					break;
				case MacOS:
					//Process p2 = macBuilder.start();
					p = Runtime.getRuntime().exec("java -jar rspeer-launcher.jar " + address);
					setOutputStream(p);
					break;
				case Linux:
					System.out.println("lets go linux");
					p = linuxBuilder.start();
					if(new File(curDir() + "/layout.sh").exists()) {
						try {
							Thread.sleep(1000);
							String[] cmd = new String[]{"/bin/sh", curDir() + "/layout.sh"};
							Process pr = Runtime.getRuntime().exec(cmd);
						}catch (Exception ex) { }
					}
					setOutputStream(p);
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				if(p != null && p.isAlive()) {
					System.out.println("DESTROYYYYYY");
					p.destroy();
				}
				System.out.println(ex.getMessage());
			}
		}

	}

	public static String curDir(){
		try {
			return new File(NexHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
		}catch (URISyntaxException e){
			e.printStackTrace();
		}
		return "";
	}

	public static void setOutputStream(Process process) {

		runningprocesses.add(process);
		new Thread(new Runnable() {
			@Override
			public void run() {
				InputStream is = process.getInputStream();
				int c;
				try {
					while ((c = is.read()) >= 0) {
						//System.out.println(c);
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