package StaticUIAnalyzer;

import StaticUIAnalyzer.Base.SootBase;
import StaticUIAnalyzer.Util.Util;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.*;

public class SootAnalyzer extends SootBase {
    public SootAnalyzer(String apkPath, String platformPath) {
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
                    System.out.println(method.getName() + ": " + idVerifyBehavior(method));
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
                if (!result.getOrDefault("validity", false)) {
                    result.put("validity", checkSubSeq(currMethod, new ArrayList<>(Arrays.asList("0", "17"))));
                }
                if (!result.getOrDefault("dob", false)) {
                    result.put("dob", checkSubSeq(currMethod, new ArrayList<>(Arrays.asList("6", "14"))));
                }
            }
        }

        return result;
    }

    public boolean checkSubSeq(SootMethod sootMethod, ArrayList<String> condition) {
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

                var result = Util.sootValueCompare(exprArgs.getFirst(), condition.getFirst()) && Util.sootValueCompare(exprArgs.getLast(), condition.getLast());

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
