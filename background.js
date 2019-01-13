var config = {
        mode: "fixed_servers",
        rules: {
          singleProxy: {
            scheme: "proxy_type",
            host: "proxy_host",
            port: parseInt("proxy_port")
          },
          bypassList: ["foobar.com"]
        }
      };

chrome.proxy.settings.set({value: config, scope: "regular"}, function() {});

function callbackFn(details) {
    return {
        authCredentials: {
            username: "proxy_username",
            password: "proxy_password"
        }
    };
}

chrome.webRequest.onAuthRequired.addListener(
            callbackFn,
            {urls: ["<all_urls>"]},
            ['blocking']
);
