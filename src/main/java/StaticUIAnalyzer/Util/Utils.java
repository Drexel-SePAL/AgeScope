package StaticUIAnalyzer.Util;

import StaticUIAnalyzer.Model.ResultReport;
import com.google.gson.Gson;
import soot.Value;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class Utils {
    public static boolean sootValueCompare(Value sootVal, String val) {
        return sootVal.toString().equals(val);
    }

    public static HashSet<String> skipStaticProcessedList(String filePath) {
        var exist = new HashSet<String>();
        var file = new File(filePath);
        if (!file.exists()) {
            return exist;
        }

        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        var gson = new Gson();
        while (scanner.hasNextLine()) {
            var resLine = gson.fromJson(scanner.nextLine(), ResultReport.class);
            exist.add(resLine.packageName);
        }

        return exist;
    }
}
