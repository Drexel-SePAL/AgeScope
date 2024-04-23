package CommonUtils;

import java.io.File;

public class FileUtils {
    public static boolean fileExists(String path) {
        var file = new File(path);
        return file.exists() && !file.isDirectory();
    }

    public static boolean dirExists(String path) {
        var file = new File(path);
        return file.exists() && file.isDirectory();
    }
}