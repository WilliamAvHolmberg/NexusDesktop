
import com.google.common.collect.ObjectArrays;
import com.sun.org.apache.xpath.internal.operations.Bool;
import ui.ProcessLink;
import ui.frmRunningAccounts;

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

	static frmRunningAccounts ui;

	static HashMap<Process, Long> running_processes = new HashMap<>();
	static HashSet<Process> confirmed_running_rocesses = new HashSet<>();
	public static void launchClient(String username, String address) {
		cleanupExistingClients();
		OSType operatingSystem = AccountLauncher.getOperatingSystemType();
		if (ui == null && operatingSystem == OSType.Windows) {
			ui = new ui.frmRunningAccounts();
			ui.setVisible(true);
		}

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
						if (ui != null && username.length() > 0)
							ui.addAccount(p, username);
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
					killProcess(p);
				}
				System.out.println(ex.getMessage());
			}
		}

	}

	static void cleanupExistingClients() {
		running_processes.entrySet().removeIf(entry -> {
			if (entry.getKey() == null || !entry.getKey().isAlive()) {
				try {
					confirmed_running_rocesses.remove(entry.getKey());
					if (ui != null) ui.removeAccount(entry.getKey());
				} catch (Exception ex) {
				}
				return true;
			}
			return false;
		});
		for (Map.Entry<Process, Long> entry : running_processes.entrySet()) {
			if (System.currentTimeMillis() - entry.getValue() > (6 * 60 * 1000)) {// 5 minutes
				if (!confirmed_running_rocesses.contains(entry.getKey())) {
					System.out.println("Killing process due to innactivity");
					killProcess(entry.getKey());
				}
			}
		}
	}

	static Boolean killerExists = null;
	public static void killProcess(Process process){
		if (killerExists == null)
			killerExists = getOperatingSystemType() == OSType.Windows && new File("KillProcess.exe").exists();
		if(killerExists) {
			long pid = ProcessLink.getProcessID(process);
			if (pid != -1) {
				ProcessBuilder killer = new ProcessBuilder("KillProcess.exe", pid + "");
				try {
					killer.start();
					return;
				}catch (IOException ex){}
			}
		}
		process.destroy();
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
		if(running_processes.size() == 0)
			return "./rspeer-launcher.jar";
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		return "-Xmx384m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xss2m -Dsun.java2d.noddraw=true -Xincgc " +
				Paths.get(fw.getDefaultDirectory().toString(), "RSPeer", "cache", "rspeer.jar").toString();
	}

	public static void setOutputStream(Process process) {
		running_processes.put(process, System.currentTimeMillis());
		new Thread(new Runnable() {
			@Override
			public void run() {
				String line = null;
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				int c;
				try {
					while ((line = reader.readLine()) != null) {
						if (line.contains("Failed to download configuration")) {
							killProcess(process);
							return;
						}
						else if (line.contains("CONNECTED TO NEX")) {
//							System.out.println("Confirmed");
							confirmed_running_rocesses.add(process);
						}
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
