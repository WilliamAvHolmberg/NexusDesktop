package org.medusa.Utils;

public class SessionStorage {

	//Ghetto way of checking if JFrame windows are showing
	public boolean logger = false;
	public boolean proxy = false;
	public boolean credits = false;
	public boolean advanced = false;
	public boolean username = false;
	
	//Should we sleep between each thread start?
	//Disabling this will make all account threads start at the same time, and spam logger (Faster than with sleep)
	public boolean threadSleep = true;
	
	//OSBot Script Launching
	public boolean useScript = false;
	public String osbotUsername;
	public String osbotPassword;
	public String scriptNameID;
	public String scriptParams;
	
	
	public String getScriptParams() {
		if (scriptParams == "") {
			return "medusaaccountcreator";
		}
		System.out.println(scriptParams);
		return scriptParams;
	}
	
	
}
