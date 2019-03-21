import java.io.IOException;

public class NexWatchdog {
    public static void begin(String computerName, int lowResourceOption, int interval){
        String bash = "bash.exe";
        if(AccountLauncher.getOperatingSystemType() == AccountLauncher.OSType.Linux)
            bash = "/bin/bash";
        ProcessBuilder rails_server = new ProcessBuilder(bash, "-i", "-c", "rails server");
        //rails_server.inheritIO();
        ProcessBuilder nexus_rb = new ProcessBuilder(bash, "-i", "-c", "ruby app/nexus.rb");
        nexus_rb.inheritIO();
        // java -jar -Xmx1024M -Dcomputer=SERVER -Dresources=1 -Dinterval=8000 "C:\oxnet\NexusDesktop.jar"
        ProcessBuilder nexus_jar = new ProcessBuilder("java", "-jar", "-Xmx1024M",
                "-Dcomputer=" + computerName,
                "-Dresources=" + lowResourceOption,
                "-Dinterval=" + interval,
                "-Ddieonfail=true",
                "NexusDesktop.jar");
        nexus_jar.inheritIO();

        Process rails_server_proc = null;
        Process nexus_rb_proc = null;
        Process nexus_jar_proc = null;
        while (true) {
            try {
                if(nexus_jar_proc != null && !nexus_jar_proc.isAlive()){
                    System.out.println("Killing Ruby");
                    nexus_rb_proc.destroy();
                    sleep(3000);
                }

//                if (rails_server_proc == null || !rails_server_proc.isAlive()) {
//                    System.out.println("Starting Rails Server");
//                    rails_server_proc = rails_server.start();
//                    sleep(5000);
//                }

                if (nexus_rb_proc == null || !nexus_rb_proc.isAlive()) {
                    System.out.println("Starting Nexus Ruby");
                    nexus_rb_proc = nexus_rb.start();
                    sleep(12000);
                }

                if (nexus_jar_proc == null || !nexus_jar_proc.isAlive()) {
                    System.out.println("Starting Nexus Helper");
                    nexus_jar_proc = nexus_jar.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sleep(1000);
        }
    }

    static void sleep(int millis){
        try {
            Thread.sleep(millis);
        }catch (Exception ex){}
    }
    public static void killProc(String...processes){
        if (AccountLauncher.getOperatingSystemType() == AccountLauncher.OSType.Windows){
            Runtime rt = Runtime.getRuntime();
            for(String process : processes) {
                try {
                    rt.exec("taskkill /F /IM " + process);
                }catch (IOException e) {}
            }
        } else {
            //rt.exec("kill -9 " + ....);
        }
    }
}
