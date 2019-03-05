
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

	static HashMap<Process, Long> runningprocesses = new HashMap<>();
	public static void launchClient(String address) {
		cleanupExistingClients();

		System.out.println(address);
		if(!address.equals(lastName) || System.currentTimeMillis() > (lastStartup + 1000 * 120)) {
			lastName = address;
			lastStartup = System.currentTimeMillis();
			System.out.println("Starting");
			ProcessBuilder linuxBuilder = new ProcessBuilder("/bin/bash", "-c",
					"java -jar " + "." + "./rspeer-launcher.jar" + " " + address);

			ProcessBuilder windowsBuilder = new ProcessBuilder("cmd.exe", "/c",
					"java -jar " + getRSPeerJar() + " -qsArgs " + address);
			windowsBuilder.inheritIO();

			ProcessBuilder macBuilder = new ProcessBuilder("osascript", "-e",
					"tell application \"Terminal\" to do script \"java -jar " + "./rspeer-launcher.jar" + " " + address);
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
						if (new File(curDir() + "/layout.sh").exists()) {
							try {
								Thread.sleep(1000);
								String[] cmd = new String[]{"/bin/sh", curDir() + "/layout.sh"};
								Process pr = Runtime.getRuntime().exec(cmd);
							} catch (Exception ex) {
							}
						}
						setOutputStream(p);
						break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				if (p != null && p.isAlive()) {
					System.out.println("DESTROYYYYYY");
					p.destroy();
				}
				System.out.println(ex.getMessage());
			}
		}

	}
	static void cleanupExistingClients(){
		runningprocesses.entrySet().removeIf(entry ->
						entry.getKey() == null ||
						!entry.getKey().isAlive());
//		for (Map.Entry<Process, Long> entry : runningprocesses.entrySet()) {
//			if (System.currentTimeMillis() - entry.getValue() > (10 * 60 * 1000)){//10 minutes
//				System.out.println("Killing process due to innactivity");
//				entry.getKey().destroy();
//			}
//		}
	}

	public static String curDir(){
		try {
			return new File(NexHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
		}catch (URISyntaxException e){
			e.printStackTrace();
		}
		return "";
	}
	public static String getRSPeerJar(){
		if(runningprocesses.size() == 0)
			return "./rspeer-launcher.jar";
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		return "-Xmx384m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xss2m -Dsun.java2d.noddraw=true -Xincgc " +
				Paths.get(fw.getDefaultDirectory().toString(), "RSPeer", "cache", "rspeer.jar").toString();
	}

	public static void setOutputStream(Process process) {

		runningprocesses.put(process, System.currentTimeMillis());
		new Thread(new Runnable() {
			@Override
			public void run() {
				InputStream is = process.getInputStream();
				int c;
				try {
					while ((c = is.read()) >= 0) {
						runningprocesses.put(process, System.currentTimeMillis());
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