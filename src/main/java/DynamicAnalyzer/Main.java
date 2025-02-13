package DynamicAnalyzer;

import CommonUtils.CommonString;
import CommonUtils.FileUtils;
import DynamicAnalyzer.Model.ResultReport;
import DynamicAnalyzer.Util.Utils;
import com.google.gson.Gson;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class Main {
    private static AndroidDriver driver;

    public static void setupDriver(String deviceUdid, String platformVersion, String apkPath) throws URISyntaxException, MalformedURLException {
        var options = new UiAutomator2Options().setPlatformName("Android").setPlatformVersion(platformVersion).setAutomationName("uiautomator2").setUdid(deviceUdid).setAutoGrantPermissions(true).setApp(apkPath).setFullReset(true);
        driver = new AndroidDriver(new URI("http://127.0.0.1:4723").toURL(), options);
    }

    public static void tearDownDriver() throws InterruptedException {
        driver.quit();
    }

    public static HashSet<String> containAgeCheckText(ArrayList<String> list) {
        var res = new HashSet<String>();
        for (var x : list) {
            var matcher = CommonString.pattern.matcher(x);
            var resList = new ArrayList<String>();
            while (matcher.find()) {
                resList.add(matcher.group());
            }

            if (!resList.isEmpty()) {
                res.add(String.join(", ", resList));
            }
        }

        return res;
    }

    public static HashSet<String> hasAgeCheck(Document doc) {
//        var elementsWithText = doc.select("[text], [content-desc]");

        var textList = new ArrayList<String>();

        for (var element : doc.toString().split("\n")) {
            textList.add(element.strip());
        }

//        for (var element : elementsWithText) {
//            var text = element.attr("text").trim();
//            var contentDesc = element.attr("content-desc").trim();
//            if (!text.isEmpty()) {
//                textList.add(text);
//            }
//            if (!contentDesc.isEmpty()) {
//                textList.add(contentDesc);
//            }
//        }

        return containAgeCheckText(textList);
    }

    public static HashSet<String> check(String apkPath) {
        var mainDoc = Jsoup.parse(driver.getPageSource());
        var result = new HashSet<String>();
        var res = hasAgeCheck(mainDoc);
        if (!res.isEmpty()) {
            result = res;
            System.out.println("Contains in mainDoc");
        } else {
            var clickableElements = driver.findElements(By.xpath("//*[@clickable='true']"));
            for (var element : clickableElements) {
                try {
                    element.click();
                    var doc = Jsoup.parse(driver.getPageSource());
                    res = hasAgeCheck(doc);
                    if (!res.isEmpty()) {
                        result = res;
                        System.out.println("Contains in otherDoc");
                    }

                    driver.navigate().back();
                } catch (Exception e) {
                    System.out.println("Could not click on some elements due to overlay or state change.");
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CommandLine cli = null;
        // try to parse cli args
        try {
            cli = cliParser(args);
        } catch (ParseException e) {
            // e.printStackTrace();
            System.exit(1);
        }

        var options = parseOptions(cli);
        var indexPath = options.get("indexPath");
        System.out.println("[main] index path: " + indexPath);
        var deviceUdid = options.get("deviceUdid");
        var platformVersion = options.get("platformVersion");
        var outputPath = options.get("outputPath");
        var existResultPath = options.get("existResult");
        System.out.println("[main] existResult: " + existResultPath);
        var gson = new Gson();
        List<String> tempList = new ArrayList<>();

        // check input file
        if (FileUtils.fileExists(indexPath)) {
            try (var br = new BufferedReader(new FileReader(indexPath))) {
                tempList = br.lines().toList();
            } catch (Exception ignore) {
            }
        }
        var apkList = new ArrayList<>(tempList);
        Collections.shuffle(apkList);

        var outputFilePath = outputPath + "/" + FilenameUtils.getBaseName(indexPath).split("\\.")[0] + "_exec_result.txt";
        var processed = Utils.skipStaticProcessedList(outputFilePath);
        if (existResultPath != null) {
            processed.addAll(Utils.skipStaticProcessedList(existResultPath));
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(outputFilePath, true);
        } catch (IOException e) {
            System.err.println("[main] Error writing to file: " + outputFilePath);
            System.exit(1);
        }

        var execution = 0;
        for (var apkFilePath : apkList) {
            var filename = FilenameUtils.getBaseName(apkFilePath).replace(".apk", "");
            if (processed.contains(filename)) {
                continue;
            }
            execution++;
            if (execution % 5 == 0) {
                var pb = new ProcessBuilder("adb", "-s", deviceUdid, "emu", "avd", "snapshot", "load", "init");
                var pc = pb.start();
                pc.waitFor();
                Thread.sleep(3000);
            }
            var result = new ResultReport();

            result.packageSha256 = filename;
            try {
                setupDriver(deviceUdid, platformVersion, apkFilePath);
                Thread.sleep(3000);
                result.matchedLines = check(apkFilePath);
                result.stringMatched = !result.matchedLines.isEmpty();
                tearDownDriver();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                fileWriter.write(gson.toJson(result) + "\n");
                fileWriter.flush();
                continue;
            }

            fileWriter.write(gson.toJson(result) + "\n");
            fileWriter.flush();
        }

        fileWriter.close();
    }

    private static CommandLine cliParser(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").build());
        options.addOption(Option.builder("v").longOpt("platformVersion").argName("platformVersion").hasArg().build());
        options.addOption(Option.builder("i").longOpt("index").argName("indexPath").hasArg().build());
        options.addOption(Option.builder("u").longOpt("deviceUdid").argName("deviceUdid").hasArg().build());
        options.addOption(Option.builder("o").longOpt("outputDir").argName("outputPath").hasArg().build());
        options.addOption(Option.builder("e").longOpt("existResult").argName("existResult").hasArg().build());

        DefaultParser parser = new DefaultParser();

        CommandLine cli;
        try {
            cli = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("[cli parser] err: " + e.getLocalizedMessage());
            helpMessage();
            throw e;
        }

        return cli;
    }

    private static HashMap<String, String> parseOptions(CommandLine cli) {
        if (cli.hasOption("h")) {
            helpMessage();
            System.exit(0);
        }

        if (!cli.hasOption("v")) {
            System.err.println("[cli parser] err: missing required option: -v, --platformVersion");
            helpMessage();
            System.exit(1);
        }

        if (!cli.hasOption("i")) {
            System.err.println("[cli parser] err: missing required option: -i, --index");
            helpMessage();
            System.exit(1);
        }

        if (!cli.hasOption("o")) {
            System.err.println("[cli parser] err: missing required option: -o, --outputDir");
            helpMessage();
            System.exit(1);
        }

        if (!cli.hasOption("u")) {
            System.err.println("[cli parser] err: missing required option: -o, --deviceUdid");
            helpMessage();
            System.exit(1);
        }

        // check apk path
        var indexPath = cli.getOptionValue("i");
        if (!FileUtils.fileExists(indexPath)) {
            System.err.println("[cli parser] err: invalid index path");
            helpMessage();
            System.exit(1);
        }

        var parseOptionResult = new HashMap<String, String>();

        if (cli.hasOption("v")) {
            parseOptionResult.put("platformVersion", cli.getOptionValue("v"));
        }

        if (cli.hasOption("o")) {
            parseOptionResult.put("outputPath", cli.getOptionValue("o"));
        }

        if (cli.hasOption("u")) {
            parseOptionResult.put("deviceUdid", cli.getOptionValue("u"));
        }

        if (cli.hasOption("e")) {
            parseOptionResult.put("existResult", cli.getOptionValue("e"));
        }

        parseOptionResult.put("indexPath", indexPath);

        return parseOptionResult;
    }

    private static void helpMessage() {
        System.out.println("usage: DynamicUIAnalyzer [OPTIONS]\n");
        System.out.println("Options:");
        System.out.println("  -h, --help                                  show this help message and exit program");
        System.out.println("  -i, --index           <indexPath>           index for input apks");
        System.out.println("  -v, --platformVersion <platformVersion>     Android SDK platform version");
        System.out.println("  -o, --output          <outputPath>          Report output path");
        System.out.println("  -u, --deviceUdid      <deviceUdid>          Android device UDID");
        System.out.println("  -e, --exist    <existResultPath>   exist result for input apks");
    }
}
