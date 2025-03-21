package StaticUIAnalyzer;

import CommonUtils.FileUtils;
import StaticUIAnalyzer.Analyzer.ActivityAnalyzer;
import StaticUIAnalyzer.Analyzer.AllActivityAnalyzer;
import StaticUIAnalyzer.Analyzer.DialogAnalyzer;
import StaticUIAnalyzer.Model.ResultReport;
import StaticUIAnalyzer.Util.ApkFile;
import StaticUIAnalyzer.Util.Utils;
import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
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
        var sdkPath = options.get("sdkPath");
        var outputPath = options.get("outputPath");
        var existResultPath = options.get("existResult");
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
        System.out.println("[main] apk list: " + apkList.size());
        Collections.shuffle(apkList);

        // check output file
        var outputFilePath = outputPath + "/" + FilenameUtils.getBaseName(indexPath).split("\\.")[0] + "_result.txt";

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

        // Linux
        // # mkdir -p /mnt/ramdisk
        // # chown -R user:user /mnt/ramdisk
        // # nano /etc/fstab
        //     tmpfs /mnt/ramdisk tmpfs nodev,nosuid,noexec,nodiratime,size=8192M 0 0
        // # /mnt/ramdisk/

        // macOS
        // $ diskutil erasevolume HFS+ 'ramdisk' `hdiutil attach -nobrowse -nomount ram://16777216`
        // $ /Volumes/ramdisk/
        var os = System.getProperty("os.name").toLowerCase();
        var ramdiskLocation = os.contains("mac") ? "/Volumes/ramdisk/" : "/mnt/ramdisk/";

        System.out.println("[main] ramdisk: " + ramdiskLocation);
        for (var apkFilePath : apkList) {
            var startTime = System.nanoTime();
            var filename = FilenameUtils.getBaseName(apkFilePath).replace(".apk", "");
            if (processed.contains(filename) || !FileUtils.fileExists(apkFilePath)) {
                continue;
            }
            System.out.println("[processing] " + filename);
            var result = new ResultReport();
            result.packageSha256 = filename;

            var apkfile = new ApkFile(apkFilePath, ramdiskLocation);

            apkfile.decodeApk();
            apkfile.prepare();
            if (apkfile.examLayouts()) {
                result.layoutIds.addAll(apkfile.layoutIds);
                result.layouts = apkfile.checkLayouts;

                var counter = 0;
                for (var l : result.layouts.entrySet()) {
                    for (var m : l.getValue().entrySet()) {
                        counter += m.getValue().length();
                    }
                }
                result.layoutResult = counter > 0;
//                System.out.println("found check layout file(s): " + apkfile.layoutIds);
//                System.out.println("found check layout file(s): " + apkfile.checkLayouts.get("id"));
            }

            var activity = new ActivityAnalyzer(apkFilePath, sdkPath, result);
            activity.analyze();
            var dialog = new DialogAnalyzer(apkFilePath, sdkPath, result);
            dialog.analyze();

            var allActivity = new AllActivityAnalyzer(apkFilePath, sdkPath, result);
            allActivity.analyze();

//            if (result.activity.isEmpty() && result.dialog.isEmpty()) {
//                var allActivity = new AllActivityAnalyzer(apkFilePath, sdkPath, result);
//                allActivity.analyze();
//            }

            result.timeInSecond = (System.nanoTime() - startTime) / 1_000_000_000.0;

            try {
                fileWriter.write(gson.toJson(result) + "\n");
                fileWriter.flush();
            } catch (Exception ignore) {
            }

            apkfile.close();
        }

        try {
            fileWriter.close();
        } catch (Exception ignore) {
        }
    }

    private static CommandLine cliParser(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").build());
        options.addOption(Option.builder("p").longOpt("platform").argName("sdkPath").hasArg().build());
        options.addOption(Option.builder("i").longOpt("index").argName("indexPath").hasArg().build());
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

        if (!cli.hasOption("p")) {
            System.err.println("[cli parser] err: missing required option: -p, --platform");
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

        // check apk path
        var indexPath = cli.getOptionValue("i");
        if (!FileUtils.fileExists(indexPath)) {
            System.err.println("[cli parser] err: invalid apk path");
            helpMessage();
            System.exit(1);
        }

        var parseOptionResult = new HashMap<String, String>();

        // check platform path
        if (cli.hasOption("p")) {
            String sdkPath = cli.getOptionValue("p");
            if (!FileUtils.dirExists(sdkPath)) {
                System.err.println("[cli parser] err: invalid Android SDK platform path");
                helpMessage();
                System.exit(1);
            }
            parseOptionResult.put("sdkPath", sdkPath);
        }

        if (cli.hasOption("o")) {
            parseOptionResult.put("outputPath", cli.getOptionValue("o"));
        }

        if (cli.hasOption("e")) {
            parseOptionResult.put("existResult", cli.getOptionValue("e"));
        }

        parseOptionResult.put("indexPath", indexPath);

        return parseOptionResult;
    }

    private static void helpMessage() {
        System.out.println("usage: StaticUIAnalyzer [OPTIONS]\n");
        System.out.println("Options:");
        System.out.println("  -h, --help                   show this help message and exit program");
        System.out.println("  -i, --index    <indexPath>   index for input apks");
        System.out.println("  -e, --exist    <existResultPath>   exist result for input apks");
        System.out.println("  -p, --platform <sdkPath>     Android SDK platform path");
        System.out.println("  -o, --output   <outputPath>  Report output path");
    }
}
