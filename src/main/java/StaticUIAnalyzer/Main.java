package StaticUIAnalyzer;

import StaticUIAnalyzer.Analyzer.ActivityAnalyzer;

public class Main {


    public static void main(String[] args) {
        var apkPath = "/Users/fanfannnmn/Downloads/android_nsfw/real_name/gui/com.tencent.tmgp.hse_96300.apk";
        var apkfile = new ApkFile(apkPath,
                "/var/folders/md/ltc18by93mlcrhlgrcwt5tq40000gn/T/B637A873-9109-40F7-8E0B-A84601CCA4D6/");
//        apkfile.decodeApk();
        apkfile.prepare();
        if (apkfile.examLayouts()) {
            System.out.println("found check layout file(s): " + apkfile.layoutIds);
            System.out.println("found check layout file(s): " + apkfile.checkLayouts.get("id"));
        }

        var platforms = "/opt/homebrew/share/android-commandlinetools/platforms";
        var soot = new ActivityAnalyzer(apkPath, platforms);
        soot.analyze();

        apkfile.close();
    }
}
