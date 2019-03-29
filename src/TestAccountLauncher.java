

public class TestAccountLauncher {
	
	public static void main(String[]args) {
		new TestAccountLauncher();
	}
	
	public TestAccountLauncher() {
		AccountLauncher.launchClient("test", "http://oxnetserver.ddns.net:3000/accounts/19896/json", true);
	}

}
