
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.Stack;

public class NexHelper {
	Stack<String> messageQueue;
	long lastLog = 0;
	private String respond = "none";

	public static void main(String[] args) {
		new NexHelper();
	}

	public NexHelper() {
		messageQueue = new Stack<String>();
		// messageQueue.push("account_request");

		Scanner sc = new Scanner(System.in);
		System.out.println("Enter IP");
		String ip = sc.nextLine();
		System.out.println("Enter Port");
		int port = sc.nextInt();

		try {
			Socket socket = new Socket(ip, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			initializeContactToSocket(out, in);

			String nextRequest;

			while (true) {
				System.out.println("Ready to take new instructions...last respond:" + respond);
				if (!messageQueue.isEmpty()) {
					nextRequest = messageQueue.pop();
					String[] parsed = nextRequest.split(":");
					switch (parsed[0]) {
					case "account_request":
						/*
						 * Argument 0 == respond
						 * Argument 1 == 0 equals that we shall ask database for a new account
						 * Argument 1 == 1 equals that we shall use provided details to start a new client
						 */
						if (parsed[1].equals("0")) {
							newAccountRequest(out, in);
						} else if (parsed[1].equals("1")) {
							startAccount(parsed);
						}
						break;
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
		out.println("computer:1:" + getIP() + ":adam_computer");
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

	private void startAccount(String[] respond) {
		String login = respond[2];
		String password = respond[3];
		String proxy = respond[4];
		String world = respond[5];
		String script = respond[6];
		System.out.println(respond);
		AccountLauncher.launchClient("./osbot.jar", script, "wavh", "Lifeosbotbook123", login, password, world, proxy,
				null);
	}
}
