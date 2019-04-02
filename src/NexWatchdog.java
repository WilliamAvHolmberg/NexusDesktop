import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class NexWatchdog {
    public static void begin(String computerName, int lowResourceOption, int interval){

        String bash = "bash.exe";
        if(AccountLauncher.getOperatingSystemType() == AccountLauncher.OSType.Linux)
            bash = "/bin/bash";

        //ProcessBuilder rails_server = new ProcessBuilder(bash, "-i", "-c", "rails server");
        //rails_server.inheritIO();

        // java -jar -Xmx1024M -Dcomputer=SERVER -Dresources=1 -Dinterval=8000 "C:\oxnet\NexusDesktop.jar"
        ProcessBuilder nexus_jar = new ProcessBuilder("java", "-jar", "-Xmx1024M",
                "-Dcomputer=" + computerName,
                "-Dresources=" + lowResourceOption,
                "-Dinterval=" + interval,
                "-Ddieonfail=true",
                "NexusDesktop.jar");
        nexus_jar.inheritIO();

        Process rails_server_proc = null;
        //ProcessBuilder[] nexus_rb = new ProcessBuilder[3];
        Process[] nexus_rb_proc = new Process[3];
        Process nexus_jar_proc = null;
        int attempt = 0;
        while (true) {
            try {
                if(nexus_jar_proc != null && !nexus_jar_proc.isAlive()){
                    attempt++;
                    if(attempt >= 2) {
                        System.out.println("Killing Ruby");
                        for(int i = 0; i < nexus_rb_proc.length; i++)
                            if(nexus_rb_proc[i] != null) nexus_rb_proc[i].destroy();
                        sleep(3000);
                    }
                }

//                if (rails_server_proc == null || !rails_server_proc.isAlive()) {
//                    System.out.println("Starting Rails Server");
//                    rails_server_proc = rails_server.start();
//                    sleep(5000);
//                }

                boolean wait = false;
                for(int i = 0; i < nexus_rb_proc.length; i++) {
                    Process process = nexus_rb_proc[i];
                    if (process == null || !process.isAlive()) {

                        ProcessBuilder pb;
                        pb = new ProcessBuilder(bash, "-i", "-c", "ruby app/nexus.rb -port " + i);
                        pb.inheritIO();

                        System.out.println("Starting Nexus Ruby " + i);
                        process = pb.start();
                        nexus_rb_proc[i] = process;
                        attempt = 0;
                        wait = true;
                        sleep(1000);
                    }
                }
                if (wait)
                    sleep(12000);

                if (nexus_jar_proc == null || !nexus_jar_proc.isAlive()) {
                    System.out.println("Starting Nexus Helper");
                    nexus_jar_proc = nexus_jar.start();
                }
            }
            catch (Exception ex) {
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
