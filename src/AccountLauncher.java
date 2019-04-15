
import com.google.common.collect.ObjectArrays;
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
	public static boolean firstRun = true;
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

	// static frmRunningAccounts ui;

	//static HashMap<Process, Long> running_processes = new HashMap<>();
	//static HashSet<Process> confirmed_running_rocesses = new HashSet<>();

	public static void launchClient(String username, String address) {
		OSType operatingSystem = AccountLauncher.getOperatingSystemType();
		// if (ui == null && operatingSystem == OSType.Windows) {
		// ui = new ui.frmRunningAccounts();
		// ui.setVisible(true);
		// }

		System.out.println(address);
		if (!address.equals(lastName) || System.currentTimeMillis() > (lastStartup + 1000 * 120)) {
			lastName = address;
			lastStartup = System.currentTimeMillis();
			System.out.println("Starting");
			String jar = getRSPeerJar();
			ProcessBuilder linuxBuilder = new ProcessBuilder("/bin/bash", "-c",
					"java -jar " + jar + " -qsArgs " + address);

			ArrayList<String> args = new ArrayList(Arrays.asList(new String[] { "java", "-jar" }));
			args.addAll(Arrays.asList(jar.split(" ")));
			args.add("-qsArgs");
			args.add(address);
			ProcessBuilder windowsBuilder = new ProcessBuilder(args);

			ProcessBuilder macBuilder = new ProcessBuilder("osascript", "-e",
					"tell application \"Terminal\" to do script \"java -jar " + "./rspeer-launcher.jar" + " "
							+ address);
			Process p = null;
			try {
				// System.out.println(AccountLauncher.getOperatingSystemType());
				switch (operatingSystem) {

				case Windows:
					int i = 1;
					System.out.println("Start windows");
					p = windowsBuilder.start();
					// if (ui != null && username.length() > 0)
					// ui.addAccount(p, username);
					break;
				case MacOS:
					// Process p2 = macBuilder.start();
					p = Runtime.getRuntime().exec("java -jar rspeer-launcher.jar " + address);
					break;
				case Linux:
					System.out.println("lets go linux");
					p = linuxBuilder.start();
					if (new File(curDir() + "/layout.sh").exists()) {
						try {
							Thread.sleep(1000);
							String[] cmd = new String[] { "/bin/sh", curDir() + "/layout.sh" };
							Process pr = Runtime.getRuntime().exec(cmd);
						} catch (Exception ex) {
						}
					}
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

	/*static void cleanupExistingClients() {
		running_processes.entrySet().removeIf(entry -> {
			if (entry.getKey() == null || !entry.getKey().isAlive()) {
				try {
					// if (ui != null) ui.removeAccount(entry.getKey());
					// confirmed_running_rocesses.remove(entry.getKey());
				} catch (Exception ex) {
				}
				return true;
			}
			return false;
		});
		for (Map.Entry<Process, Long> entry : running_processes.entrySet()) {
			if (System.currentTimeMillis() - entry.getValue() > (8 * 60 * 1000)) {// 5 minutes
				if (!confirmed_running_rocesses.contains(entry.getKey())) {
					System.out.println("Killing process due to innactivity");
					entry.getKey().destroy();
				}
			}
		}
		}
		*/
	

	
	public static String curDir(){
		try {
			return new File(NexHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
		}catch (URISyntaxException e){
			e.printStackTrace();
		}
		return "";
	}

	public static String getRSPeerJar(){
		if(firstRun) {
			firstRun = false;
			return "./rspeer-launcher.jar";
		}
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		return "-Xmx384m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xss2m -Dsun.java2d.noddraw=true -Xincgc " +
				Paths.get(fw.getDefaultDirectory().toString(), "RSPeer", "cache", "rspeer.jar").toString();
	}


}

// Your proxy string will have to include " -proxy ip:port", I did it like that
// so it can be optional.
