package DynamicAnalyzer;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    private static AndroidDriver driver;

    public static void setupDriver() throws URISyntaxException, MalformedURLException {
        var options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setPlatformVersion("13")
                .setAutomationName("uiautomator2")
                .setDeviceName("Pixel_6")
                .setAutoGrantPermissions(true)
                .setApp("/Users/fanfannnmn/Downloads/adult_apk/BCA0806E08712FB24983990B0FEBB649F28502E4C53985AA0F02EC0DFBE45C13.apk");
        driver = new AndroidDriver(
                new URI("http://127.0.0.1:4723").toURL(), options
        );
    }

    public static void tearDownDriver() throws InterruptedException {
        Thread.sleep(5000);
        driver.quit();
    }

    public static void getTextByClassName(String className) {
        var els = driver.findElements(AppiumBy.className(className));
        for (var el : els) {
            var text = el.getText().strip();
            if (!text.isEmpty() && !text.equals("null")) {
                System.out.println(el.getText());
            }
        }
    }

    public static void getAttrByClassName(String className) {
        var els = driver.findElements(AppiumBy.className(className));
        for (var el : els) {
            var attr = el.getAttribute("content-desc").strip();
            if (!attr.isEmpty() && !attr.equals("null")) {
                System.out.println(attr);
            }
        }
    }

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, InterruptedException {
        setupDriver();
        Thread.sleep(10000);

        getTextByClassName("android.widget.TextView");

        getAttrByClassName("android.widget.CheckBox");
        getAttrByClassName("android.view.View");

        tearDownDriver();
    }
}
