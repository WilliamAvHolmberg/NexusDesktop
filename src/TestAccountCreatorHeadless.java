import java.net.MalformedURLException;

public class TestAccountCreatorHeadless {
	
	public static void main(String[]args) {
		new TestAccountCreatorHeadless();
	}
	
	public TestAccountCreatorHeadless() {
		AccountCreatorHeadless ach = new AccountCreatorHeadless();
		try {
			ach.createAccount("HobSarz", "HobSarzd@yahoo.com", "ugot00wned2", new PrivateProxy(null, null,"92.32.68.149", "8888"), "oxnetserver.ddns.net:3000/accounts/350/json");
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
