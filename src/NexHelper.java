
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.medusa.Utils.Logger;
import org.nex.unlocker.proxy.SocksProxy;

import com.twocaptcha.api.ProxyType;

import javax.management.*;


public class NexHelper implements Runnable {
	public static Stack<String> messageQueue;
	public static boolean UNLOCK_IS_READY = true;
	long lastLog = 0;
	private String respond = "none";
	public static String computerName;
	private long lastStart = 0;
	//private List<User> users;

	static String readFile(String filename){
		String content = null;
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(filename));
			content = scanner.useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		} finally {
			if(scanner != null)
				scanner.close();
		}
		return content;
	}
	private static void writeFile(String filename, String data) {
		try {
			Files.write(Paths.get(filename), data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Integer tryParse(Object obj) {
		Integer retVal;
		try {
			retVal = Integer.parseInt((String) obj);
		} catch (NumberFormatException nfe) {
			retVal = 0; // or null if that is your preference
		}
		return retVal;
	}

	public static String LOCAL_IP = null;
	public static void main(String[] args) throws IOException, InterruptedException {

		File[] files = new File(AccountLauncher.curDir()).listFiles((dir1, name) -> name.endsWith(".log"));
		for (File file : files)
			file.delete();

		//TODO IN FUTURE createUsers();
		System.out.println("started NexHelper 9.0 with multi-thread support, multiple host support");
		messageQueue = new Stack<String>();
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			LOCAL_IP = in.readLine(); // you get the IP as a String
			System.out.println("My IP: " + LOCAL_IP.trim());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner sc = new Scanner(System.in);

		if(System.getProperty("testfirefox", null) != null){
			try {
				PrivateProxy proxy = new PrivateProxy("NHTzq7", "mxxdZy", "195.158.193.81", "8000");
				if(!proxy.setSystemProxy())
					return;
				//AccountCreator.postForm(null, "", "", "", proxy, "http://ipchicken.com/");
			}catch (Exception ex){ ex.printStackTrace(); }
			return;
		}

		String dir = AccountLauncher.curDir();
		System.out.println(dir);

		String serverData = readFile("server.txt");
		if (serverData == null){
			System.out.println("server.txt was missing. Lets create it");
			serverData = "oxnetserver.ddns.net\r\n1:William\r\n2:Brandon\r\n3:Suicide\r\n4:VPS\r\n5:MINIMAC\r\n6:ACCOUNT\r\n7:BATCH1\r\n8:BATCH2\r\n9:BATCH3\r\n10:BATCH4r\\n10:BATCH5";
			writeFile("server.txt", serverData);
		}

		String[] lines = serverData.split("\\s*\\r?\\n\\s*");
		System.out.println("\r\nPlease choose which ip you want to use:");
		for(int i = 1; i < lines.length; i++){
			System.out.println(lines[i]);
		}
			int ipOption = sc.nextInt();
			String ip = lines[ipOption].split(":")[1];

		int port = 43594;
		System.out.println("\r\nPlease choose which computer you want to use:");
		for(int i = 1; i < lines.length; i++){
			System.out.println(lines[i]);
		}

		String computerArg = System.getProperty("computer", null);
		if(computerArg != null){//You can call -computer=1 OR -computer=William
			for (int i = 1; i < lines.length; i++){
				String[] parts = lines[i].split(":");
				if(parts.length == 2 && (parts[0] == computerArg || parts[1].equalsIgnoreCase(computerArg)))
					computerName = parts[1];
			}
		}
		if(computerName == null) {
			int nameOption = sc.nextInt();
			computerName = lines[nameOption].split(":")[1];
		}else{
			System.out.println(computerName);
		}

		System.out.println("\r\nWe are gonna connect with user:" + computerName);
		System.out.println("Please choose if you wanna use low resources:");
		System.out.println("1:lowcpu no render");
		System.out.println("2:normal");
		System.out.println("3:no interface (extreme)");

		Integer lowResourceOption = null;
		String resourcesStr = System.getProperty("resources", null);
		if(resourcesStr != null)
			lowResourceOption = tryParse(resourcesStr);
		if (lowResourceOption == null || lowResourceOption < 1 || lowResourceOption > 3)
			lowResourceOption = sc.nextInt();
		else
			System.out.println(lowResourceOption);
		switch (lowResourceOption) {
			case 1:
				AccountLauncher.allowOptions = " -allow norender,lowcpu,norandoms ";
				break;
			case 2:
				AccountLauncher.allowOptions = " -allow norandoms ";
				break;
			case 3:
				AccountLauncher.allowOptions = " -allow nointerface,norender,lowcpu,norandoms ";
				break;
			default:
				System.out.println("Something went wrong");
				System.exit(1);
				break;
		}

		System.out.println("\r\nPlease choose your launch interval:");
		Integer interval = null;
		String intervalStr = System.getProperty("interval", null);
		if (intervalStr != null)
			interval = tryParse(intervalStr);
		if (interval == null || interval < 0 || interval > 10000000)
			interval = sc.nextInt();
		else
			System.out.println(interval + "\r\n");

		if(System.getProperties().containsKey("watchdog")) {
			System.out.println("Beginning Watchdog...");
			NexWatchdog.begin(computerName, lowResourceOption, interval);
			return;
		}

		boolean dieOnFail = System.getProperties().containsKey("dieonfail");

		long lastGotMessage = 0;
		int minute = 60 * 1000;
		while (true) {
			try {
				NexHelper nexHelper = new NexHelper(ip, port, interval);
				Thread thread = new Thread(nexHelper, "NexHelper");
				thread.start();
				while (thread.isAlive()) {
					try {
						thread.join(minute);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					lastGotMessage = nexHelper.lastGotMessage;
					long now = System.currentTimeMillis();
					if (now - nexHelper.lastGotMessage > minute) {//If we crash or disconnect, then we have connected within a minute. If we fail to connect, then nah.
						thread.interrupt();
						if(dieOnFail)
							System.exit(1);
					}
				}
				if (System.currentTimeMillis() - lastGotMessage > minute) {
					if(dieOnFail) {
						System.exit(1);
					}
				}
			} catch (Exception ex){ ex.printStackTrace(); }
		}
	}

	public String ip;
	public int port;
	int launchInterval;
	public NexHelper(String ip, int port, int launchInterval) {
		this.ip = ip;
		this.port = port;
		this.launchInterval = launchInterval;
	}

	public long lastSentMessage = 0;
	public long lastGotMessage = 0;

	public static double getProcessCpuLoad() throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException {
		MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
		ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });

		if (list.isEmpty()) return 100;

		Attribute att = (Attribute)list.get(0);
		Double value  = (Double)att.getValue();

		// usually takes a couple of seconds before we get real values
		if (value == -1.0) return 100;
		// returns a percentage value with 1 decimal point precision
		return ((int)(value * 1000) / 10.0);
	}

	@Override
	public void run() {
		try {
			System.out.println("Connecting...");
			Socket socket = new Socket(ip, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			initializeContactToSocket(out, in);

			String nextRequest;
			//AccountCreator.createIPCooldownMessage("50.237.102.215", 300);
			while (true) {
				int tmp_interval = launchInterval;
				if(getProcessCpuLoad() > 80) {
					Logger.log("HIGH CPU LOAD... wait 10 sec");
					tmp_interval = 30000;

				}else if (getProcessCpuLoad() > 60) {
					tmp_interval = 15000;
				}
				if (!messageQueue.isEmpty() && System.currentTimeMillis() > lastStart + tmp_interval) {
					lastStart = System.currentTimeMillis();
					nextRequest = getMessage(messageQueue);
					String[] parsed = nextRequest.split(":");
					Logger.log(nextRequest);
					Logger.log(parsed[0]);
					switch (parsed[0]) {
						case "unlocked_account":
							sendUnlockedAcc(parsed, out, in);
							Logger.log("SENT ACC UNLOCKED MESS");
							break;
						case "unlock_cooldown":
							sendUnlockCooldown(parsed, out, in);
							Logger.log("SENT UNLOCK COOLDOWN MESS");

							break;
						case "ip_cooldown":
							sendIPCooldown(parsed, out, in);
							Logger.log("SENT IP COOLDOWN MESS");
							break;
						case "unlock_account":
							unlockAccount(parsed, nextRequest);
							break;
						case "create_account":
							createAccount(parsed, nextRequest);
							break;
						case "account_request":
							/*
							 * Argument 0 == respond Argument 1 == 0 equals that we shall ask database for a
							 * new account Argument 1 == 1 equals that we shall use provided details to
							 * start a new client
							 */
							if (parsed[1].equals("0")) {
								newAccountRequest(out, in);
								break;
							} else if (parsed[1].equals("1")) {
								String address = nextRequest.substring(nextRequest.indexOf("http"), nextRequest.length());
								startAccount(parsed[2], address);
								break;

							}
						default:
							log(out, in);
							break;
					}
				}
				Logger.log("LOOP");
				log(out, in);
				TimeUnit.SECONDS.sleep(1);
			}
		}
		catch (SocketTimeoutException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		catch (SocketException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Retrying in 5 secs...");
		try {
			Thread.sleep(5000);
		}catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		Logger.log("NEX HELPER IS OUT");
	}


	private String getMessage(Stack<String> messageQueue) {
		for(String message: messageQueue) {
			if(message.contains("unlock")) {
				String respond = message.toString();
				messageQueue.remove(message);
				return respond;
			}
		}
		return messageQueue.pop();
	}
	private void log(PrintWriter out, BufferedReader in) throws InterruptedException, IOException {
		if (System.currentTimeMillis() - lastLog > 5000) { // only log every 5 sec
			lastSentMessage = System.currentTimeMillis();
			out.println("log:0");
			respond = in.readLine();
			// standard message is 'logged:fine'
			// if respond is anything else than logged:fine we can assume it is a new
			// instruction
			if (respond != null && !respond.equals("logged:fine")) {
				System.out.println("we got a new instructionToQueue:" + respond);
				messageQueue.push(respond);
			}
			if(respond != null)
				lastGotMessage = System.currentTimeMillis();
			lastLog = System.currentTimeMillis();
		}
	}

	private void initializeContactToSocket(PrintWriter out, BufferedReader in) throws IOException {
		out.println("computer:1:" + getIP() + ":" + computerName);
		if (in.readLine().equals("connected:1")) {
			System.out.println("NexHelper has been initialized towards Nexus");
		} else {
			System.out.println("Connection Towards Nexus failed");
		}
	}

	public String getIP() {
		URL url;
		try {
			url = new URL("http://checkip.amazonaws.com/");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			return br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "not_found";

	}

	/*
	 * respond index 0 - Type of respond 1 - Status (0 == failed, 1 == success) 2 -
	 * login 3 - password 4 - proxy
	 */
	private void newAccountRequest(PrintWriter out, BufferedReader in) throws IOException {
		out.println("account_request:0");
		String res = in.readLine();
		String[] respond = res.split(":");
		if (respond[0].equals("account_request") && respond[1].equals("1")) {
			String address = res.substring(res.indexOf("http"), res.length());
			startAccount(respond[2], address);
		} else {
			System.out.println("No Account available atm. Try again in 5 minutes");
		}
	}

	private void sendUnlockedAcc(String[] accInfo, PrintWriter out, BufferedReader in) throws IOException {

		String email = accInfo[1];
		Logger.log(email);
		String newPassword = accInfo[2];
		out.println("unlocked_account:" + email + ":" + newPassword + ":");
		String res = in.readLine();
		System.out.println("Successfully gave information about updated acc");
	}

	private void sendIPCooldown(String[] ipInfo, PrintWriter out, BufferedReader in) throws IOException {

		String ip = ipInfo[1];
		Logger.log(ip);
		String cooldown = ipInfo[2];
		out.println("ip_cooldown:" + ip + ":" + cooldown);
		String res = in.readLine();
		System.out.println("Successfully gave information about bad ip");
	}
	private void sendUnlockCooldown(String[] ipInfo, PrintWriter out, BufferedReader in) throws IOException {

		String ip = ipInfo[1];
		Logger.log(ip);
		String cooldown = ipInfo[2];
		out.println("unlock_cooldown:" + ip + ":" + cooldown);
		String res = in.readLine();
		System.out.println("Successfully gave information about bad ip");
	}
	private void createAccount(String[] respond, String res) throws MalformedURLException, InterruptedException {
		String username = respond[1];
		String login = respond[2];
		String password = respond[3];
		String proxyIP = respond[4];
		String proxyPort = respond[5];
		String proxyUsername = respond[6];
		String proxyPassword = respond[7];
		String address = res.substring(res.indexOf("http"), res.length());
		AccThread accThread = new AccThread(username, login, password, new PrivateProxy(proxyUsername, proxyPassword, proxyIP, proxyPort), address);
		Thread thread = new Thread(accThread);
		thread.start();
		System.out.println("Started new create acc thread");
	}
	private void unlockAccount(String[] respond, String res) throws MalformedURLException, InterruptedException {
		String username = respond[1];
		String login = respond[2];
		String password = respond[3];
		String proxyIP = respond[4];
		String proxyPort = respond[5];
		String proxyUsername = respond[6];
		String proxyPassword = respond[7];
		String address = res.substring(res.indexOf("http"), res.length());
		SocksProxy proxy = new SocksProxy(proxyUsername, proxyPassword, proxyIP, proxyPort, ProxyType.SOCKS5);
		RecoverThread accThread = new RecoverThread(username, login, password, new PrivateProxy(proxyUsername, proxyPassword, proxyIP, proxyPort), address);
		Thread thread = new Thread(accThread);
		thread.start();
		System.out.println("Started new recover thread");
	}
	private void startAccount(String username, String address) {

		AccountLauncher.launchClient(username, address);

	}
}
