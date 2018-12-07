
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.Stack;

import org.medusa.*;
import org.medusa.Utils.Logger;

import com.anti_captcha.*;



public class NexHelper {
	Stack<String> messageQueue;
	long lastLog = 0;
	private String respond = "none";
	private String computerName;

	public static void main(String[] args) throws IOException, InterruptedException {

		new NexHelper();
	}

	public NexHelper() throws MalformedURLException, InterruptedException {
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
		String ip = "oxnetserver.ddns.net";
		//String ip = "nexus.no-ip.org";
		int port = 43594;
		System.out.println("Please choose which user you want to use:");
		System.out.println("1:William");
		System.out.println("2:Brandon");
		int nameOption = sc.nextInt();
		switch (nameOption) {
		case 1:
			computerName = "William";
			break;
		case 2:
			computerName = "Brandon";
			break;
		case 3:
			computerName = "Suicide";
		case 4:
			computerName = "VPS";
			break;
		default:
			System.out.println("Something went wrong");
			System.exit(1);
			break;
		}
		System.out.println("We are gonna connect with user:" + computerName);
		try {
			Socket socket = new Socket(ip, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			initializeContactToSocket(out, in);

			String nextRequest;

			while (true) {
				if (!messageQueue.isEmpty()) {
					nextRequest = messageQueue.pop();
					String[] parsed = nextRequest.split(":");
					switch (parsed[0]) {
					case "create_account":
						createAccount(parsed);
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
							System.out.println("lets start?");
							startAccount(parsed);
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

		String[] respond = in.readLine().split(":");
		if (respond[0].equals("account_request") && respond[1].equals("1")) {
			startAccount(respond);
		} else {
			System.out.println("No Account available atm. Try again in 5 minutes");
		}
	}
	private void createAccount(String[] respond) throws MalformedURLException, InterruptedException {
		String username = respond[1];
		String login = respond[2];
		String password = respond[3];
		String proxyIP = respond[4];
		String proxyPort = respond[5];
		String proxyUsername = respond[6];
		String proxyPassword = respond[7];
		CreateAccount ca = new CreateAccount();
		ca.createAccount(username, login, password, new Proxy(proxyUsername, proxyPassword, proxyIP, proxyPort));
		URL whatismyip;
		String ip = "";
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			ip = in.readLine(); // you get the IP as a String
			System.out.println(ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.log("we created acc with ip:" + ip + "   should have created with: " + proxyIP );
		//AccThread accThread = new AccThread(username, login, password, new Proxy(proxyUsername, proxyPassword, proxyIP, proxyPort));
//
		//Thread t = new Thread(accThread);
		//t.start();

	}
	private void startAccount(String[] respond) {
		String login = respond[2];
		String password = respond[3];
		String proxyIP = respond[4];
		String proxyPort = respond[5];
		String proxyUsername = respond[6];
		String proxyPassword = respond[7];
		String world = respond[8];
		String script = respond[9];
		String proxy = "-proxy " + proxyIP + ":" + proxyPort + ":" + proxyUsername + ":" + proxyPassword;
		String params = password + "_";
		AccountLauncher.launchClient("./osbot.jar", script, "wavh", "Lifeosbotbook123", login, password, world, proxy,
				params);

	}
}
