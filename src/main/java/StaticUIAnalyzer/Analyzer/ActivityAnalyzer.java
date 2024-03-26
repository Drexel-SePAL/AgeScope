package StaticUIAnalyzer.Analyzer;

import StaticUIAnalyzer.Base.SootBase;
import StaticUIAnalyzer.Util.Util;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ActivityAnalyzer extends SootBase {
    public ActivityAnalyzer(String apkPath, String platformPath) {
        super(apkPath, platformPath);
    }

    public void analyze() {
        this.initSootConfig();
        var activities = findActivityClass();

        for (var a : activities) {
            if (!isIdVerifyActivity(a)) {
                continue;
            }

            for (var method : a.getMethods()) {
                var idBehavior = idVerifyBehavior(method);
                if (!idBehavior.isEmpty()) {
                    System.out.println(method + ": " + idVerifyBehavior(method));
                }
            }
        }
    }

    public boolean isIdVerifyActivity(SootClass a) {
        var editTextCount = 0;
        for (var f : a.getFields()) {
            if (f.getSubSignature().contains("android.widget.EditText")) {
                editTextCount++;
            }
        }

        return editTextCount >= 2;
    }

    public Map<String, Boolean> idVerifyBehavior(SootMethod method) {
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

            if (stmt.getInvokeExpr().toString().contains("getText()")) {
                getTextCalled = true;
                continue;
            }

            if (getTextCalled) {
                var invokeExpr = stmt.getInvokeExpr();
                if (invokeExpr.getMethod().getName().startsWith("<java.lang.Object")) {
                    continue;
                }

                var currMethod = invokeExpr.getMethod();
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
                        result.put(p.getKey(), checkSubSeq(currMethod, p.getValue()));
                    }
                }
            }
        }

        return result;
    }

    public boolean checkSubSeq(SootMethod sootMethod, String[] condition) {
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

                var result = Util.sootValueCompare(exprArgs.getFirst(), condition[0]) && Util.sootValueCompare(exprArgs.getLast(), condition[1]);
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

    public HashSet<SootClass> findActivityClass() {
        var activitiesClass = new HashSet<SootClass>();
        var apkClasses = Scene.v().getClasses().stream().filter(s -> !(s.getName().startsWith("java.")) &&
//                        !(s.getName().startsWith("android.")) &&
//                        !(s.getName().startsWith("androidx.")) &&
                !(s.getName().startsWith("com.android.")) && !(s.getName().startsWith("javax.")) && !(s.getName().startsWith("android.support.")) && !(s.getName().startsWith("sun.")) && !(s.getName().startsWith("com.google."))).toList();

        for (var cls : apkClasses) {
            if (activitiesClass.contains(cls)) {
                continue;
            }

            if (isExtendedFromActivity(cls)) {
                activitiesClass.add(cls);
            }
        }

        return activitiesClass;
    }

    public boolean isExtendedFromActivity(SootClass sootClass) {
        if (sootClass.getName().equals("android.app.Activity")) {
            return true;
        }

        if (sootClass.hasSuperclass()) {
            return isExtendedFromActivity(sootClass.getSuperclass());
        }

        return false;
    }
}
