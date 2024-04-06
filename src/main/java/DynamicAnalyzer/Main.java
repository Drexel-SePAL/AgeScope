package DynamicAnalyzer;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Main {
    private static AndroidDriver driver;

    public static void setupDriver() throws URISyntaxException, MalformedURLException {
        var options = new UiAutomator2Options().setPlatformName("Android").setPlatformVersion("13").setAutomationName("uiautomator2").setDeviceName("Pixel_6").setAutoGrantPermissions(true).setApp("/Users/fanfannnmn/Downloads/adult_apk/BCA0806E08712FB24983990B0FEBB649F28502E4C53985AA0F02EC0DFBE45C13.apk");
        driver = new AndroidDriver(new URI("http://127.0.0.1:4723").toURL(), options);
    }

    public static void tearDownDriver() throws InterruptedException {
        Thread.sleep(5000);
        driver.quit();
    }

    public static ArrayList<String> getText(Document doc) {
        var elementsWithText = doc.select("[text], [content-desc]");

        var textList = new ArrayList<String>();
        for (var element : elementsWithText) {
            var text = element.attr("text").trim();
            var contentDesc = element.attr("content-desc").trim();
            if (!text.isEmpty()) {
                textList.add(text);
            }
            if (!contentDesc.isEmpty()) {
                textList.add(contentDesc);
            }
        }

        return textList;
    }

    public static boolean containAgeCheckText(ArrayList<String> list) {
        var pattern = Pattern.compile("adult(s)?( only)?|(over )?[1-2]\\d\\+?|under( )?age|age of [1-2]\\d|af_num_adults");

        for (var x : list) {
            if (pattern.matcher(x).find()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAgeCheck(Document doc) {
        var textList = getText(doc);

        return containAgeCheckText(textList);
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        setupDriver();
        Thread.sleep(10000);

        var mainDoc = Jsoup.parse(driver.getPageSource());
        if (hasAgeCheck(mainDoc)) {
            System.out.println("Contains in mainDoc");
        } else {
            var clickable = mainDoc.select("[clickable=true]");
            System.out.println(clickable);
            
            var clickableElements = driver.findElements(By.xpath("//*[@clickable='true']"));
            for (var element : clickableElements) {
                try {
                    element.click();
                    var doc = Jsoup.parse(driver.getPageSource());
                    if (hasAgeCheck(doc)) {
                        System.out.println("Contains in otherDoc");
                    }
                } catch (Exception e) {
                    System.out.println("Could not click on some elements due to overlay or state change.");
                } finally {
                    driver.navigate().back();
                }
            }
        }

        tearDownDriver();
    }
}
