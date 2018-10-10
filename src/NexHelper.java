
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class NexHelper {
	public static void main(String[] args) {
		new NexHelper();
	}
	
	public NexHelper() {
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
			out.println("computer:0");
			if(in.readLine().equals("connected:1")) {
				System.out.println("NexHelper has been initialized towards Nexus");
			}else {
				System.out.println("Connection Towards Nexus failed");
			}

			String nextRequest;
			
			while (true) {				
				System.out.println("Ready to take new instructions...(account_request to start new client)");
				nextRequest = sc.nextLine();
				switch(nextRequest) {
				case "account_request":
					newAccountRequest(out, in);
					break;
				default:
					out.println(nextRequest + ":0");
					System.out.println(in.readLine());
					break;
				}
			}
		}catch (Exception e) {
			
		}
	}

	/*
	 * respond index
	 * 0 - Type of respond
	 * 1 - Status (0 == failed, 1 == success)
	 * 2 - login
	 * 3 - password
	 * 4 - proxy
	 */
	private void newAccountRequest(PrintWriter out, BufferedReader in) throws IOException {
		out.println("account_request:0");

		String[] respond = in.readLine().split(":");
		if(respond[0].equals("account_request") && respond[1].equals("1")) {
			String login = respond[2];
			String password = respond[3];
			String proxy = respond[4];
			String world = respond[5];
			String script = respond[6];
			System.out.println(respond);
			AccountLauncher.launchClient("./osbot.jar", script, "wavh", "Lifeosbotbook123", login, password, world, proxy, null);
		}else {
			System.out.println("No Account available atm. Try again in 5 minutes");
		}
	}
}
