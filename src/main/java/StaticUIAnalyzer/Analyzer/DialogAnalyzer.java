package StaticUIAnalyzer.Analyzer;

import StaticUIAnalyzer.Base.SootBase;
import StaticUIAnalyzer.Model.ResultReport;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.options.Options;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DialogAnalyzer extends SootBase {
    public DialogAnalyzer(String apkPath, String platformPath, ResultReport result) {
        super(apkPath, platformPath, result);
    }

    public void analyze() {
        if (Scene.v().getState() == 0) {
            initSootConfig();
        } else {
            addExcludeClass();
            Options.v().set_exclude(excludePackagesList);
        }
        var dialogs = findClassesByName("android.app.Dialog", false);

        for (var a : dialogs) {
            if (!this.possibleVerificationClass(a)) {
                continue;
            }

            for (var method : a.getMethods()) {
                var res = verificationCheck(method);
                if (!res.isEmpty()) {
                    this.result.dialogResult = true;
                    this.result.dialog.putAll(res);
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

        var types = method.getParameterTypes().stream()
                .collect(Collectors.groupingBy(Type::toString, Collectors.counting()));

        if (types.size() == 1 && !this.result.layouts.isEmpty() && types.getOrDefault("java.lang.String", 0L) == 2L) {
            result.put("age", true);
        }

        return result;
    }
}
