package StaticUIAnalyzer;

public class Main {


    public static void main(String[] args) {
        var apkPath = "/Users/fanfannnmn/Downloads/com.lilithgames.xgame.android.cn_820.apk";
        var apkfile = new ApkFile(apkPath,
                "/var/folders/md/ltc18by93mlcrhlgrcwt5tq40000gn/T/B637A873-9109-40F7-8E0B-A84601CCA4D6/");
//        apkfile.decodeApk();
        apkfile.prepare();
        if (apkfile.examLayouts()) {
            System.out.println("found check layout file(s): " + apkfile.layoutIds);
            System.out.println("found check layout file(s): " + apkfile.checkLayouts.get("id"));
        }

        var platforms = "/opt/homebrew/share/android-commandlinetools/platforms";
        var soot = new SootAnalyzer(apkPath, platforms);
        soot.analyze();

        apkfile.close();
    }

}
