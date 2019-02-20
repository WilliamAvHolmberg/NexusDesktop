


import java.net.MalformedURLException;
import java.net.URL;

import com.anti_captcha.Api.NoCaptchaProxyless;

public class AntiCaptcha {

    private static String ANTICAPTCHA_KEY = "01bd2a7839593708e019c567485f8c61";
    private String token = null;

    public String solveCaptcha(String RUNESCAPE_URL) throws MalformedURLException, InterruptedException {
        NoCaptchaProxyless api = new NoCaptchaProxyless();
        api.setClientKey(ANTICAPTCHA_KEY);
        api.setWebsiteUrl(new URL(RUNESCAPE_URL));
        api.setWebsiteKey("6Lcsv3oUAAAAAGFhlKrkRb029OHio098bbeyi_Hv");
        System.out.println("Sending Task To AntiCaptcha");

        if (!api.createTask()) {
            System.out.println("Captcha Failure:" + api.getErrorMessage());
        } else if (!api.waitForResult()) {
            System.out.println("Failed To Solve Captcha");
        } else {
            System.out.println("AntiCaptcha Task Complete");
            token = api.getTaskSolution().getGRecaptchaResponse();
        }

        return token;
    }

}