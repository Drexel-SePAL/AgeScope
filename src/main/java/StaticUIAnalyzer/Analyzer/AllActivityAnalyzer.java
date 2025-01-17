package StaticUIAnalyzer.Analyzer;

import CommonUtils.CommonString;
import StaticUIAnalyzer.Base.SootBase;
import StaticUIAnalyzer.Model.ResultReport;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

public class AllActivityAnalyzer extends SootBase {
    public AllActivityAnalyzer(String apkPath, String platformPath, ResultReport result) {
        super(apkPath, platformPath, result);
    }

    public void analyze() {
        if (Scene.v().getState() == 0) {
            initSootConfig();
        } else {
            addExcludeClass();
            Options.v().set_exclude(excludePackagesList);
        }
        var activities = findClassesByName("android.app.Activity", true);

        for (var a : activities) {
            for (var method : a.getMethods()) {
                var res = verificationCheck(method);
                res.remove("");
                if (!res.isEmpty()) {
                    this.result.activityResult = true;
                    this.result.activity.putAll(res);
//                    System.out.println(method.getName() + ": " + idBehavior);
                }
            }
        }
    }

    public Map<String, Boolean> verificationCheck(SootMethod method) {
        var result = new HashMap<String, Boolean>();
        if (!method.hasActiveBody()) {
            return result;
        }

        var activeBody = method.getActiveBody().toString().replace("\n\n", "\n").split("\n");
        var detectMapStr = new String[]{"legal_?name\\b", "first_?name\\b", "last_?name\\b", "full_?name\\b", "address(_?1)?\\b", "address(_?2)?\\b", "address(_?3)?\\b", "city\\b", "province\\b", "zip_?code\\b", "country_?code\\b", "dob\\b", "date_?of_?birth\\b", "age\\b", "shengri\\b"};
        var detectMap = new Pattern[detectMapStr.length];
        for (int i = 0; i < detectMapStr.length; i++) {
            detectMap[i] = Pattern.compile(detectMapStr[i]);
        }

        for (var line : activeBody) {
            var l = line.toLowerCase();
            if (!l.contains("getstring")) {
                continue;
            }

            var resList = new HashSet<String>();
            for (var p : detectMap) {
                var matcher = p.matcher(l);
                while (matcher.find() && !(l.contains("package") || l.contains("storage"))) {
                    resList.add(matcher.group());
                }
            }

            var matcher = CommonString.pattern.matcher(l);
            while (matcher.find()) {
                resList.add(matcher.group());
            }

            for (var r : resList) {
                result.put(String.format("_%s", r), true);
            }
        }

        return result;
    }
}
