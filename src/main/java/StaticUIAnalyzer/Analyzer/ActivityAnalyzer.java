package StaticUIAnalyzer.Analyzer;

import StaticUIAnalyzer.Base.SootBase;
import StaticUIAnalyzer.Model.ResultReport;
import StaticUIAnalyzer.Util.Utils;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.options.Options;

import java.util.HashMap;
import java.util.Map;

public class ActivityAnalyzer extends SootBase {
    public ActivityAnalyzer(String apkPath, String platformPath, ResultReport result) {
        super(apkPath, platformPath, result);
    }

    public void analyze() {
        if (Scene.v().getState() == 0) {
            initSootConfig();
        } else {
            addExcludeClass();
            Options.v().set_exclude(excludePackagesList);
        }
        var activities = findClassesByName("android.app.Activity");
        activities.addAll(findClassesByName("android.app.DialogFragment"));
        for (var a : activities) {
//            if (!this.possibleVerificationClass(a)) {
//                continue;
//            }

            for (var method : a.getMethods()) {
                var res = verificationCheck(method);
                if (!verificationCheck(method).isEmpty()) {
                    this.result.activityResult = true;
                    this.result.activity.add(res);
//                    System.out.println(method + ": " + verificationCheck(method));
                }
            }
        }
    }

    public Map<String, Boolean> verificationCheck(SootMethod method) {
        var result = new HashMap<String, Boolean>();
        if (!method.hasActiveBody()) {
            return result;
        }

        var getTextCalled = false;
        for (var unit : method.retrieveActiveBody().getUnits()) {
            var stmt = (Stmt) unit;
            if (!stmt.containsInvokeExpr()) {
                continue;
            }

            var invokeExpr = stmt.getInvokeExpr();
            var currMethod = invokeExpr.getMethod();

            if (invokeExpr.toString().contains("getText()")) {
                getTextCalled = true;
                continue;
            }

            if (getTextCalled) {
                if (invokeExpr.getMethod().getName().startsWith("<java.lang.Object")) {
                    continue;
                }

                if (!result.getOrDefault("length", false)) {
                    result.put("length", checkIdLength(currMethod));
                }

                var subSeq = new HashMap<String, String[]>() {
                    {
                        put("validity", new String[]{"0", "17"});
                        put("dob", new String[]{"6", "14"});
                        put("dob-year", new String[]{"6", "10"});
                        put("dob-month", new String[]{"10", "12"});
                        put("dob-day", new String[]{"12", "14"});
                    }
                };

                for (var p : subSeq.entrySet()) {
                    if (!result.getOrDefault(p.getKey(), false)) {
                        result.put(p.getKey(), checkSubSequence(currMethod, p.getValue()));
                    }
                }
            } else if (currMethod.hasActiveBody()) {
                if (currMethod.getActiveBody().toString().contains("== 18") || currMethod.getActiveBody().toString().contains("!= 18")) {
                    result.put("length", true);
                }
            }
        }

        return result;
    }

    public boolean checkSubSequence(SootMethod sootMethod, String[] condition) {
        if (!sootMethod.hasActiveBody()) {
            return false;
        }

        for (var unit : sootMethod.getActiveBody().getUnits()) {
            var stmt = (Stmt) unit;

            if (!stmt.containsInvokeExpr()) {
                continue;
            }

            var invokeExpr = stmt.getInvokeExpr();
            if (invokeExpr.getMethod().toString().equals("<java.lang.CharSequence: java.lang.CharSequence subSequence(int,int)>")) {
                var exprArgs = invokeExpr.getArgs();

                var result = Utils.sootValueCompare(exprArgs.getFirst(), condition[0]) && Utils.sootValueCompare(exprArgs.getLast(), condition[1]);
                if (!result) {
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    public boolean checkIdLength(SootMethod sootMethod) {
        var lengthCheckExist = false;
        var digitCheckExist = false;
        if (!sootMethod.hasActiveBody()) {
            return false;
        }

        for (var unit : sootMethod.getActiveBody().getUnits()) {
            var stmt = (Stmt) unit;

            if (lengthCheckExist) {
                var boxes = stmt.getUseBoxes();
                digitCheckExist = (boxes.size() == 3) && (boxes.getLast().toString().contains(" == 18"));

                if (digitCheckExist) {
                    return true;
                } else {
                    lengthCheckExist = false;
                }
            }

            if (!stmt.containsInvokeExpr()) {
                continue;
            }

            var invokeExpr = stmt.getInvokeExpr();
            if (invokeExpr.getMethod().toString().equals("<java.lang.CharSequence: int length()>")) {
                lengthCheckExist = true;
            }
        }

        return false;
    }
}
