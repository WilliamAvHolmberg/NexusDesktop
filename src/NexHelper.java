
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;



public class NexHelper {
	Stack<String> messageQueue;
	long lastLog = 0;
	private String respond = "none";
	private String computerName;
	private long lastStart = 0;
	//private List<User> users;

	public static void main(String[] args) throws IOException, InterruptedException {

		new NexHelper();
	}

	String readFile(String filename){
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

	public NexHelper() throws MalformedURLException, InterruptedException {
		//TODO IN FUTURE createUsers();
		System.out.println("started NexHelper 6.0 with multi-thread support, fixed acc");
		//CreateAccount ca = new CreateAccount();
		//ca.createAccount("MonkTomte","MonkWilo@gmail.com",  "ugot00wned2", new Proxy("CejurP","Rz7Kpw", "185.201.255.99", "8000"));
		messageQueue = new Stack<String>();
		// messageQueue.push("account_request");
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			String ip = in.readLine(); // you get the IP as a String
			System.out.println(ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner sc = new Scanner(System.in);
		//System.out.println("Enter leee IP");
		//String ip = sc.nextLine();
		//System.out.println("Enter Port");

		String serverData = readFile("server.txt");
		if (serverData == null){
			System.out.println("server.txt was missing. Lets create it");
			serverData = "oxnetserver.ddns.net\r\n1:William\r\n2:Brandon\r\n3:Suicide\r\n4:VPS\r\n5:MINIMAC\r\n6:ACCOUNT\r\n7:BATCH1\r\n8:BATCH2\r\n9:BATCH3\r\n10:BATCH4";
			writeFile("server.txt", serverData);
		}

		String[] lines = serverData.split("\\s*\\r?\\n\\s*");
		String ip = lines[0];

		int port = 43594;
		System.out.println("Please choose which user you want to use:");
		for(int i = 1; i < lines.length; i++){
			System.out.println(lines[i]);
		}
		int nameOption = sc.nextInt();
		computerName = lines[nameOption].split(":")[1];

		System.out.println("We are gonna connect with user:" + computerName);
		System.out.println("Please choose if you wanna use low resources:");
		System.out.println("1:lowcpu no render");
		System.out.println("2:normal");
		System.out.println("3:no interface (extreme)");
		
		int lowResourceOption = sc.nextInt();
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
		
		System.out.println("Please choose your launch interval:");
		int interval = sc.nextInt();
		try {
			Socket socket = new Socket(ip, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			initializeContactToSocket(out, in);

			String nextRequest;

			while (true) {
				if (!messageQueue.isEmpty() && System.currentTimeMillis() > lastStart + interval) {
					lastStart = System.currentTimeMillis();
					nextRequest = messageQueue.pop();
					String[] parsed = nextRequest.split(":");
					switch (parsed[0]) {
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
							startAccount(address);
							break;
							
						}
					default:
						log(out, in);
						break;
					}
				} else {
					log(out, in);
					Thread.sleep(1000);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}



	private void log(PrintWriter out, BufferedReader in) throws InterruptedException, IOException {
		if (System.currentTimeMillis() - lastLog > 5000) { // only log every 5 sec
			out.println("log:0");
			respond = in.readLine();
			// standard message is 'logged:fine'
			// if respond is anything else than logged:fine we can assume it is a new
			// instruction
			if (!respond.equals("logged:fine")) {
				System.out.println("we got a new instructionToQueue:" + respond);
				messageQueue.push(respond);
			}
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
			startAccount(address);
		} else {
			System.out.println("No Account available atm. Try again in 5 minutes");
		}
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
	private void startAccount(String address) {
		
		AccountLauncher.launchClient(address);

	}
}
